package wtf.choco.engarde.api;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a specific type of cheat.
 * <p>
 * Anti cheat service providers are expected to create instances of CheatType for each
 * detectable cheat in the anti cheat. Ideally, only one instance of CheatType should
 * be created for each type of cheat, however there is no guarantee that one instance may
 * be referentially equal to one another. Should the need arise to compare a CheatType,
 * the use of {@link #equals(Object)} is advised where possible.
 */
public final class CheatType {

    private final String id;
    private final String name;
    private final String description;
    private final CheatCategory category;

    /**
     * Construct a new {@link CheatType}.
     *
     * @param id the unique id of the cheat
     * @param name the name of the cheat
     * @param description a brief description of the cheat
     * @param category the category that best describes this cheat
     */
    public CheatType(@NotNull String id, @NotNull String name, @NotNull String description, @NotNull CheatCategory category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
    }

    /**
     * Get the unique id of this cheat.
     *
     * @return the unique id
     */
    @NotNull
    public String getId() {
        return id;
    }

    /**
     * Get the name of the cheat.
     *
     * @return the name
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Get the description of the cheat.
     *
     * @return the description
     */
    @NotNull
    public String getDescription() {
        return description;
    }

    /**
     * Get the {@link CheatCategory} to which this cheat belongs.
     *
     * @return the category
     */
    @NotNull
    public CheatCategory getCategory() {
        return category;
    }

    @Override
    public int hashCode() {
        int hash = 7;

        hash = hash * 31 + id.hashCode();
        hash = hash * 31 + name.hashCode();
        hash = hash * 31 + description.hashCode();
        hash = hash * 31 + category.hashCode();

        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof CheatType)) {
            return false;
        }

        CheatType other = (CheatType) obj;
        return Objects.equals(id, other.id) && Objects.equals(name, other.name)
                && Objects.equals(description, other.description) && category == other.category;
    }

    @Override
    public String toString() {
        return String.format("CheatType [id=%s, name=%s, description=%s, category=%s]", id, name, description, category);
    }

}
