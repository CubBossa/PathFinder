package de.cubbossa.pathfinder

import de.cubbossa.disposables.Disposer
import de.cubbossa.pathfinder.dump.DumpWriter
import de.cubbossa.pathfinder.dump.DumpWriterImpl
import de.cubbossa.pathfinder.event.EventDispatcher
import de.cubbossa.pathfinder.group.ModifierRegistry
import de.cubbossa.pathfinder.messages.Messages
import de.cubbossa.pathfinder.migration.Migrator
import de.cubbossa.pathfinder.misc.Keyed
import de.cubbossa.pathfinder.misc.NamespacedKey
import de.cubbossa.pathfinder.misc.Vector
import de.cubbossa.pathfinder.misc.World
import de.cubbossa.pathfinder.node.GraphEditorRegistry
import de.cubbossa.pathfinder.node.NodeTypeRegistry
import de.cubbossa.pathfinder.node.NodeTypeRegistryImpl
import de.cubbossa.pathfinder.nodegroup.ModifierRegistryImpl
import de.cubbossa.pathfinder.storage.DatabaseType
import de.cubbossa.pathfinder.storage.StorageAdapter
import de.cubbossa.pathfinder.storage.StorageAdapterImpl
import de.cubbossa.pathfinder.storage.StorageImplementation
import de.cubbossa.pathfinder.storage.cache.CacheLayerImpl
import de.cubbossa.pathfinder.storage.implementation.RemoteSqlStorage
import de.cubbossa.pathfinder.storage.implementation.SqliteStorage
import de.cubbossa.pathfinder.util.VectorSplineLib
import de.cubbossa.pathfinder.visualizer.VisualizerTypeRegistry
import de.cubbossa.pathfinder.visualizer.VisualizerTypeRegistryImpl
import de.cubbossa.splinelib.SplineLib
import de.cubbossa.translations.GlobalMessageBundle
import de.cubbossa.translations.MessageBundle
import kotlinx.coroutines.runBlocking
import lombok.Getter
import lombok.SneakyThrows
import net.kyori.adventure.platform.AudienceProvider
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.flywaydb.core.api.migration.JavaMigration
import java.io.File
import java.nio.file.Files
import java.util.*

@Getter
abstract class AbstractPathFinder : PathFinder {
    protected var disposer: Disposer

    protected var state: PathFinder.ApplicationState = PathFinder.ApplicationState.DISABLED

    protected lateinit var nodeTypeRegistry: NodeTypeRegistry
    protected lateinit var visualizerTypeRegistry: VisualizerTypeRegistry
    protected lateinit var modifierRegistry: ModifierRegistry
    protected lateinit var extensionRegistry: ExtensionsRegistry
    lateinit var configFileLoader: ConfigFileLoader
    protected lateinit var configuration: PathFinderConfig
    protected lateinit var audiences: AudienceProvider
    protected lateinit var miniMessage: MiniMessage

    protected lateinit var storage: StorageAdapter
    protected lateinit var eventDispatcher: EventDispatcher<*>
    lateinit var translations: MessageBundle
    protected lateinit var dumpWriter: DumpWriter


    abstract fun getWorld(worldId: UUID?): World?

    init {
        disposer = createDisposer()
    }

    override fun load() {
        check((state == PathFinder.ApplicationState.DISABLED || state == PathFinder.ApplicationState.EXCEPTIONALLY)) { "Trying to load PathFinder - Application already enabled." }
        state = PathFinder.ApplicationState.LOADING
        onLoad()
    }

    @SneakyThrows
    open fun onLoad() {
        instance = this
        state = PathFinder.ApplicationState.LOADING
        PathFinderProvider.setPathFinder(this)

        dumpWriter = DumpWriterImpl()
        (dumpWriter as DumpWriterImpl).addProperty("pathfinder-version", { this.version })
        disposer.register(this, dumpWriter)

        nodeTypeRegistry = NodeTypeRegistryImpl(this)
        visualizerTypeRegistry = VisualizerTypeRegistryImpl(this)
        modifierRegistry = ModifierRegistryImpl(this)

        configFileLoader = ConfigFileLoader(dataFolder)
        disposer.register(this, configFileLoader)
        configuration = configFileLoader.loadConfig()

        (dumpWriter as DumpWriterImpl).addProperty("config") {
            try {
                return@addProperty Files.readString(File(dataFolder, "config.yml").toPath())
            } catch (t: Throwable) {
                return@addProperty t.toString()
            }
        }

        storage = StorageAdapterImpl(nodeTypeRegistry)
        disposer.register(this, storage)
        if (!getConfiguration().database.isCaching) {
            storage.cache = CacheLayerImpl.empty()
        }

        eventDispatcher = createEventDispatcher()
        disposer.register(this, eventDispatcher)

        this.extensionRegistry = ExtensionsRegistryImpl(this)
        extensionRegistry.loadExtensions()
        dumpWriter.addProperty("extensions") {
            extensionRegistry.getExtensions().stream()
                .map { e: PathFinderExtension -> if (e.isDisabled()) ("~ " + e.key) else e.key.toString() }
                .toList()
        }
    }

    @SneakyThrows
    open fun onEnable() {
        miniMessage = MiniMessage.miniMessage()

        audiences = createAudiences()
        Messages.setAudiences(audiences)

        if (!File(dataFolder, "lang/styles.properties").exists()) {
            saveResource("lang/styles.properties", false)
        }

        // Data
        setupMessages()

        File(dataFolder, "data/").mkdirs()
        val impl = storageImplementation

        Migrator(dataFolder, *Arrays.stream(migrations)
            .filter { it is JavaMigration }
            .map { it as JavaMigration }
            .toArray { arrayOfNulls(it) })
            .migrate()

        (storage as StorageAdapterImpl).implementation = impl
        storage.eventDispatcher = eventDispatcher
        (storage as StorageAdapterImpl).logger = logger
        storage.init()
        runBlocking {
            storage.createGlobalNodeGroup(visualizerTypeRegistry.defaultType)
        }


        disposer.register(this, GraphEditorRegistry(this))
        extensionRegistry.enableExtensions()

        dumpWriter.addProperty("node-types") {
            nodeTypeRegistry.types.stream()
                .map(Keyed::key).map { o: NamespacedKey? -> Objects.toString(o) }
                .toList()
        }
        dumpWriter.addProperty("modifier-types") {
            modifierRegistry.types.stream()
                .map(Keyed::key).map { o: NamespacedKey? -> Objects.toString(o) }
                .toList()
        }
        dumpWriter.addProperty("visualizer-types") {
            visualizerTypeRegistry.types.keys.stream()
                .map { o: NamespacedKey? -> Objects.toString(o) }
                .toList()
        }
        dumpWriter.addProperty("node-count") { runBlocking { storage.loadNodes().size } }
        dumpWriter.addProperty("group-count") { runBlocking { storage.loadAllGroups().size } }
        dumpWriter.addProperty("visualizer-count") { runBlocking { storage.loadVisualizers().size } }
    }

    override fun dispose() {
        instance = null
        PathFinderProvider.setPathFinder(null)
    }

    @SneakyThrows
    override fun shutdown() {
        check(!(state == PathFinder.ApplicationState.DISABLED || state == PathFinder.ApplicationState.EXCEPTIONALLY)) { "Trying to shutdown PathFinder - Application already disabled." }
        disposer.dispose(this)
        state = PathFinder.ApplicationState.DISABLED
    }

    override fun shutdownExceptionally(t: Throwable) {
        shutdown()
        state = PathFinder.ApplicationState.EXCEPTIONALLY
    }

    private fun setupMessages() {
        translations = GlobalMessageBundle.applicationTranslationsBuilder("PathFinder", dataFolder)
            .withDefaultLocale(getConfiguration().language.fallbackLanguage)
            .withEnabledLocales(*Locale.getAvailableLocales())
            .withPreferClientLanguage(getConfiguration().language.isClientLanguage)
            .withLogger(logger)
            .withPropertiesStorage(File(dataFolder, "lang"))
            .withPropertiesStyles(File(dataFolder, "lang/styles.properties"))
            .build()

        miniMessage = MiniMessage.builder()
            .editTags { builder: TagResolver.Builder ->
                builder
                    .resolvers(translations.getBundleResolvers())
                    .resolvers(translations.getStylesResolver())
            }
            .build()

        translations.addMessagesClass(Messages::class.java)
        translations.writeLocale(Locale.ENGLISH)

        Messages.formatter().setMiniMessage(miniMessage)
        Messages.formatter().setNullStyle(translations.getStyles()["c-offset-dark"])
        Messages.formatter().setTextStyle(translations.getStyles()["c-offset"])
        Messages.formatter().setNumberStyle(translations.getStyles()["c-offset-light"])
    }

    private val storageImplementation: StorageImplementation
        get() {
            val impl: StorageImplementation = when (getConfiguration().database.type) {
                DatabaseType.REMOTE_SQL -> RemoteSqlStorage(
                    configuration.database.remoteSql, nodeTypeRegistry,
                    modifierRegistry, visualizerTypeRegistry
                )

                else -> SqliteStorage(
                    getConfiguration().database.embeddedSql.file, nodeTypeRegistry,
                    modifierRegistry, visualizerTypeRegistry
                )
            }
            // impl = new DebugStorage(impl, getLogger());
            impl.setWorldLoader { worldId: UUID? -> this.getWorld(worldId) }
            impl.logger = logger
            return impl
        }

    override fun getVisualizerTypeRegistry(): VisualizerTypeRegistry {
        return visualizerTypeRegistry
    }

    abstract fun createAudiences(): AudienceProvider

    abstract fun createDisposer(): Disposer

    abstract fun createEventDispatcher(): EventDispatcher<*>

    abstract fun saveResource(name: String?, override: Boolean)

    companion object {
        @Getter
        private var instance: AbstractPathFinder? = null

        private val GLOBAL_GROUP_KEY = pathfinder("global")
        private val DEFAULT_VISUALIZER_KEY = pathfinder("default_visualizer")

        @JvmField
        val SPLINES: SplineLib<Vector> = VectorSplineLib()

        @JvmStatic
        fun globalGroupKey(): NamespacedKey {
            return GLOBAL_GROUP_KEY
        }

        fun defaultVisualizerKey(): NamespacedKey {
            return DEFAULT_VISUALIZER_KEY
        }

        @JvmStatic
        fun pathfinder(key: String?): NamespacedKey {
            return NamespacedKey("pathfinder", key!!)
        }
    }
}
