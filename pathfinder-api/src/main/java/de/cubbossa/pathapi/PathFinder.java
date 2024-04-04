package de.cubbossa.pathapi;

import de.cubbossa.disposables.Disposable;
import de.cubbossa.disposables.Disposer;
import de.cubbossa.pathapi.event.EventDispatcher;
import de.cubbossa.pathapi.group.ModifierRegistry;
import de.cubbossa.pathapi.misc.Task;
import de.cubbossa.pathapi.node.NodeTypeRegistry;
import de.cubbossa.pathapi.storage.StorageAdapter;
import de.cubbossa.pathapi.visualizer.VisualizerTypeRegistry;
import java.io.File;
import java.util.logging.Logger;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * The main handler of the PathFinder application.
 * Use a service provider or {@link PathFinderProvider} to get an instance.
 */
public interface PathFinder extends Disposable {

    enum ApplicationState {
        /**
         * The application prepares for state RUNNING.
         */
        LOADING,
        /**
         * The application converts old config files into new config files.
         */
        CONFIG_CONVERSION,
        /**
         * The application converts older data into new data.
         */
        DATA_CONVERSION,
        /**
         * The application is running, runtime features are enabled.
         */
        RUNNING,
        /**
         * Something went wrong that stopped the application from working.
         * Same as DISABLED but for an unexpected reason.
         */
        EXCEPTIONALLY,
        /**
         * An intended disable, for example when server stops.
         */
        DISABLED
    }

    /**
     * @return a logger that is used for the whole application.
     */
    Logger getLogger();

    /**
     * @return the current application state.
     */
    ApplicationState getState();

    void load();

    void shutdown();

    void shutdownExceptionally(Throwable t);

  Disposer getDisposer();

    /**
     * The storage is the main class to handle read and write. Use it to load, modify and save PathFinder data.
     *
     * @return a storage instance.
     */
    StorageAdapter getStorage();

    /**
     * The extension registry handles all plugins to the PathFinder Application. There must be only one
   * ExtensionRegistry instance for each application.
   *
   * @return The ExtensionRegistry instance.
   */
  ExtensionsRegistry getExtensionRegistry();

  /**
   * The event dispatcher wraps the event bus of the current environment or defines a custom implementation.
   * Preferably use the wrapper instead of the environment event bus to make an application portable.
   *
   * @return The EventDispatcher instance.
   */
  EventDispatcher<?> getEventDispatcher();

  /**
   * The modifier registry handles different modifier types that are valid for group modification.
   * Register modifier types before groups are being loaded from storage to prevent issues.
   *
   * @return The ModifierRegistry instance.
   */
  ModifierRegistry getModifierRegistry();

  /**
   * The node type registry handles different node types.
   * Register new node types in the loading phase of your application to allow the storage to load nodes
   * of such type.
   *
   * @return The NodeTypeRegistry instance.
   */
  NodeTypeRegistry getNodeTypeRegistry();

  /**
   * The visualizer type registry handles different visualizer types.
   * Register new visualizer types in the loading phase of your application to allow the storage to load
   * visualizers of such type.
   *
   * @return The VisualizerTypeRegistry instance.
   */
  VisualizerTypeRegistry getVisualizerTypeRegistry();

  PathFinderConfig getConfiguration();

  String getVersion();

  File getDataFolder();

  ClassLoader getClassLoader();

  MiniMessage getMiniMessage();

  AudienceProvider getAudiences();

  Task repeatingTask(Runnable runnable, long delay, long interval);

  void cancelTask(Task task);

  default Object[] getMigrations() {
    return new Object[0];
  }
}
