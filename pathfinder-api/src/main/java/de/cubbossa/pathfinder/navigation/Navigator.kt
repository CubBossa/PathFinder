package de.cubbossa.pathfinder.navigation

import de.cubbossa.disposables.Disposable
import de.cubbossa.pathfinder.graph.NoPathFoundException
import de.cubbossa.pathfinder.misc.PathPlayer
import de.cubbossa.pathfinder.node.Node
import de.cubbossa.pathfinder.visualizer.PathView
import de.cubbossa.pathfinder.visualizer.PathVisualizer
import de.cubbossa.pathfinder.visualizer.VisualizerPath

interface Navigator : Disposable {

    @Throws(NoPathFoundException::class)
    fun createPath(route: Route): List<Node>

    @Throws(NoPathFoundException::class)
    fun <PlayerT> createRenderer(
        viewer: PathPlayer<PlayerT>, route: Route
    ): VisualizerPath<PlayerT>

    @Throws(NoPathFoundException::class)
    fun <PlayerT, ViewT : PathView<PlayerT>> createRenderer(
        viewer: PathPlayer<PlayerT>, route: Route, renderer: PathVisualizer<ViewT, PlayerT>
    ): VisualizerPath<PlayerT>
}
