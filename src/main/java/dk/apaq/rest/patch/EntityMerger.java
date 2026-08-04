package dk.apaq.rest.patch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A utility class for merging two entities of the same type. This class allows you to specify
 * which fields should be updated (dirty fields) while preserving others. Fields can also be
 * ignored during the merge process.
 *
 * <p>Dirty fields support nested paths ({@code child.text}), array and list element access
 * ({@code array[0]}, {@code list[0].text}) and Map access ({@code meta(color)}).
 *
 * @param <T> The type of the entities to be merged.
 */
public class EntityMerger<T> {

    // Logger for logging errors or information during the merge process.
    private static final Logger LOG = LoggerFactory.getLogger(EntityMerger.class);

    // A set of fields that should be ignored by default during the merge process.
    private final Set<String> defaultIgnoredFields;

    /**
     * Default constructor that initializes with no ignored fields.
     * By default, no fields are ignored during the merge process unless explicitly specified.
     */
    public EntityMerger() {
        this.defaultIgnoredFields = Collections.emptySet();
    }

    /**
     * Constructor that accepts a collection of fields to be ignored by default during the merge process.
     *
     * @param defaultIgnoredFields A collection of field names that should be ignored by default.
     *                             If null, an empty set will be used.
     */
    public EntityMerger(Collection<String> defaultIgnoredFields) {
        this.defaultIgnoredFields = defaultIgnoredFields == null
                ? Collections.emptySet()
                : Collections.unmodifiableSet(new HashSet<>(defaultIgnoredFields));
    }

    /**
     * Merges the specified fields from the new entity into the existing entity. Fields
     * to be updated are provided via the dirtyFields iterable. Default ignored fields
     * are considered, but no additional ignored fields are provided.
     *
     * @param existingEntity The entity to be updated (must not be null).
     * @param newEntity      The entity containing updated values (must not be null).
     * @param dirtyFields    The list of field names that should be updated (must not be null).
     * @return The updated existing entity with changes applied.
     * @throws IllegalArgumentException If any specified field cannot be updated.
     */
    public T mergeEntities(T existingEntity, T newEntity, Iterable<String> dirtyFields) {
        return this.mergeEntities(existingEntity, newEntity, dirtyFields, Collections.emptySet());
    }

    /**
     * Merges the specified fields from the new entity into the existing entity, while considering
     * both default ignored fields and additional ignored fields. Fields to be updated are provided
     * via the dirtyFields iterable.
     *
     * @param existingEntity The entity to be updated (must not be null).
     * @param newEntity      The entity containing updated values (must not be null).
     * @param dirtyFields    The list of field names that should be updated (must not be null).
     * @param ignoredFields  A collection of field names to be ignored during the merge process.
     *                       These are in addition to the default ignored fields (must not be null).
     * @return The updated existing entity with changes applied.
     * @throws IllegalArgumentException If any specified field cannot be updated or a null entity is provided.
     */
    public T mergeEntities(T existingEntity, T newEntity, Iterable<String> dirtyFields, Collection<String> ignoredFields) {
        // Validate that no null arguments are passed in.
        Objects.requireNonNull(existingEntity, "existingEntity must be specified.");
        Objects.requireNonNull(newEntity, "newEntity must be specified.");
        Objects.requireNonNull(dirtyFields, "dirtyFields must be specified.");
        Objects.requireNonNull(ignoredFields, "ignoredFields must be specified.");

        Set<String> ignored = ignoredFields.isEmpty()
                ? Collections.emptySet()
                : Collections.unmodifiableSet(new HashSet<>(ignoredFields));

        // Iterate over the dirtyFields and apply changes from newEntity to existingEntity.
        for (String item : dirtyFields) {
            if (!defaultIgnoredFields.contains(item) && !ignored.contains(item)) {
                try {
                    // Copy the property from newEntity to existingEntity using reflection.
                    PropertyAccess.setValue(existingEntity, item, PropertyAccess.getValue(newEntity, item));
                } catch (RuntimeException ex) {
                    LOG.error("Error occurred while merging entities.", ex);
                    // Throw a specific error if a field cannot be merged.
                    throw new IllegalArgumentException("The parameter '" + item + "' does not apply to this resource.", ex);
                }
            }
        }
        return existingEntity;
    }
}
