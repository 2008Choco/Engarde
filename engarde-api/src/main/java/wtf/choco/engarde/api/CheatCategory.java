package wtf.choco.engarde.api;

/**
 * Represents a category in which a {@link CheatType} may be grouped.
 *
 * @apiNote Not all categories will be supported by any given anti cheat. This is a
 * very primitive overview of commonly used cheat categories
 */
public enum CheatCategory {

    /**
     * Cheats related to breaking blocks or entities.
     */
    BREAK,

    /**
     * Cheats related to placing blocks or items.
     */
    PLACE,

    /**
     * Cheats related to interacting with the world, entities, or blocks.
     */
    INTERACT,

    /**
     * Cheats related to player vs player, or player vs entity combat.
     */
    COMBAT,

    /**
     * Cheats related to players interacting with inventories.
     */
    INVENTORY,

    /**
     * Cheats related to player movement.
     */
    MOVEMENT,

    /**
     * Cheats related to chat messages. Often this category is used for more broad
     * chat offenses such as spam bots.
     */
    CHAT,

    /**
     * Miscellaneous cheats that do not fit in any other category.
     */
    MISC;

}
