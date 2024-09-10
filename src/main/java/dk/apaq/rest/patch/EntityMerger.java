package dk.apaq.rest.patch;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

/**
 * A utility class for merging two entities of the same type. This class allows you to specify
 * which fields should be updated (dirty fields) while preserving others. Fields can also be
 * ignored during the merge process.
 *
 * @param <T> The type of the entities to be merged.
 */
public class EntityMerger<T> {

    // Logger for logging errors or information during the merge process.
    private static final Logger LOG = LoggerFactory.getLogger(EntityMerger.class);

    // A list of fields that should be ignored by default during the merge process.
    private final List<String> defaultIgnoredFields;

    /**
     * Default constructor that initializes with no ignored fields.
     * By default, no fields are ignored during the merge process unless explicitly specified.
     */
    public EntityMerger() {
        this.defaultIgnoredFields = Collections.emptyList();
    }

    /**
     * Constructor that accepts a list of fields to be ignored by default during the merge process.
     *
     * @param defaultIgnoredFields A list of field names that should be ignored by default.
     *                             If null, an empty list will be used.
     */
    public EntityMerger(List<String> defaultIgnoredFields) {
        if (defaultIgnoredFields == null) {
            defaultIgnoredFields = Collections.emptyList();
        }
        this.defaultIgnoredFields = defaultIgnoredFields;
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
        return this.mergeEntities(existingEntity, newEntity, dirtyFields, Collections.emptyList());
    }

    /**
     * Merges the specified fields from the new entity into the existing entity, while considering
     * both default ignored fields and additional ignored fields. Fields to be updated are provided
     * via the dirtyFields iterable.
     *
     * @param existingEntity The entity to be updated (must not be null).
     * @param newEntity      The entity containing updated values (must not be null).
     * @param dirtyFields    The list of field names that should be updated (must not be null).
     * @param ignoredFields  A list of field names to be ignored during the merge process.
     *                       These are in addition to the default ignored fields (must not be null).
     * @return The updated existing entity with changes applied.
     * @throws IllegalArgumentException If any specified field cannot be updated or a null entity is provided.
     */
    public T mergeEntities(T existingEntity, T newEntity, Iterable<String> dirtyFields, List<String> ignoredFields) {
        // Validate that no null arguments are passed in.
        Validate.notNull(existingEntity, "existingEntity must be specified.");
        Validate.notNull(newEntity, "newEntity must be specified.");
        Validate.notNull(dirtyFields, "dirtyFields must be specified.");
        Validate.notNull(ignoredFields, "ignoredFields must be specified.");

        // Iterate over the dirtyFields and apply changes from newEntity to existingEntity.
        dirtyFields.iterator().forEachRemaining(item -> {
            if (!defaultIgnoredFields.contains(item) && !ignoredFields.contains(item)) {
                try {
                    // Copy the property from newEntity to existingEntity using reflection.
                    PropertyUtils.setProperty(existingEntity, item, PropertyUtils.getProperty(newEntity, item));
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | IndexOutOfBoundsException ex) {
                    LOG.error("Error occurred while merging entities.", ex);
                    // Throw a specific error if a field cannot be merged.
                    throw new IllegalArgumentException("The parameter '" + item + "' does not apply to this resource.");
                }
            }
        });
        return existingEntity;
    }
}
