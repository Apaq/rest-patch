package dk.codezoo.rest;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

public class EntityMerger<T> {

    private static final Logger LOG = LoggerFactory.getLogger(EntityMerger.class);
    private final List<String> defaultIgnoredFields;

    public EntityMerger() {
        this.defaultIgnoredFields = Collections.emptyList();
    }

    public EntityMerger(List<String> defaultIgnoredFields) {
        if(defaultIgnoredFields == null) {
            defaultIgnoredFields = Collections.emptyList();
        }
        this.defaultIgnoredFields = defaultIgnoredFields;
    }

    public T mergeEntities(T existingEntity, T newEntity, Iterable<String> dirtyFields) {
        return this.mergeEntities(existingEntity, newEntity, dirtyFields, Collections.emptyList());
    }

    public T mergeEntities(T existingEntity, T newEntity, Iterable<String> dirtyFields, List<String> ignoredFields) {
        Validate.notNull(existingEntity, "existingEntity must be specified.");
        Validate.notNull(newEntity, "newEntity must be specified.");
        Validate.notNull(dirtyFields, "dirtyField must be specified.");
        Validate.notNull(ignoredFields, "ignoredField must be specified.");

        dirtyFields.iterator().forEachRemaining(item -> {
            if (!defaultIgnoredFields.contains(item) && !ignoredFields.contains(item)) {
                try {
                    PropertyUtils.setProperty(existingEntity, item, PropertyUtils.getProperty(newEntity, item));
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | IndexOutOfBoundsException ex) {
                    LOG.error("Error occured while merging entities.", ex);
                    throw new IllegalArgumentException("The parameter '" + item + "' does not apply to this resource.");
                }
            }
        });
        return existingEntity;
    }
}

