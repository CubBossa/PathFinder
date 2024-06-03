package de.cubbossa.pathfinder.misc

/**
 * Represents an object that stores a permission node.
 * The permission node is used to provide and prevent access to features of the plugin.
 */
interface PermissionHolder {
    /**
     * @return null, if no permission is set. Null will by default be interpreted as "access".
     */
    var permission: String?
}
