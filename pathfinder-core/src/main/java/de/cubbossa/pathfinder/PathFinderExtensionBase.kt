package de.cubbossa.pathfinder

import lombok.Getter
import lombok.Setter

@Setter
@Getter
abstract class PathFinderExtensionBase : PathFinderExtension {
    private var disabled = false

    override fun disable() {
        disabled = true
    }
}
