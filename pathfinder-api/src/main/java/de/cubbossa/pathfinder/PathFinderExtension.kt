package de.cubbossa.pathfinder

import de.cubbossa.disposables.Disposable
import de.cubbossa.pathfinder.misc.Keyed
import org.pf4j.ExtensionPoint

/**
 * A PathFinderExtension is an interface that includes functions for different lifecycles of the
 * PathFinder application. It allows you to run code at the right time, and it is encouraged to use it to
 * register Modifier, Node and Visualizer types at the right time.
 *
 * To make this class recognizable to PathFinder, you must register it as Service in your manifest.
 * PathFinder will use the [java.util.ServiceLoader] class to retrieve all possible extensions.
 */
interface PathFinderExtension : Keyed, Disposable, ExtensionPoint {
    fun disable()

    val isDisabled: Boolean

    /**
     * LifeCycle hook that is called in the loading stage of the environment. Mostly, this will be a Bukkit servers
     * loading phase where all sibling plugins are being loaded.
     *
     * @param pathPlugin The current PathFinder instance that this class is an extension to.
     */
    fun onLoad(pathPlugin: PathFinder) {
    }

    /**
     * LifeCycle hook that is called in the enabling stage of the environment. Mostly, this will be a Bukkit servers
     * enabling phase where all sibling plugins are being enabled.
     *
     * @param pathPlugin The current PathFinder instance that this class is an extension to.
     */
    fun onEnable(pathPlugin: PathFinder) {
    }

    /**
     * LifeCycle hook that is called in the shutdown stage of the environment. Mostly, this will be a Bukkit servers
     * shutdown phase where all sibling plugins are being disabled.
     *
     * @param pathPlugin The current PathFinder instance that this class is an extension to.
     */
    fun onDisable(pathPlugin: PathFinder) {
    }
}
