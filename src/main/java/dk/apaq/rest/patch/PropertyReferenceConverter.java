package dk.apaq.rest.patch;

import java.util.Collection;

/**
 * Functional interface for converting an object of type T into a collection of property references (field names).
 * This can be used to dynamically map or translate properties of an object into a list of strings representing
 * the properties that can be referenced or updated.
 *
 * @param <T> The type of the input object that will be used to produce a collection of property references.
 */
@FunctionalInterface
public interface PropertyReferenceConverter<T> {

    /**
     * Translates the given input object into a collection of property references (field names).
     * This method should return a collection of strings, each representing a property in the input object.
     *
     * @param input The input object to be translated.
     * @return A collection of strings representing the property references of the input object.
     */
    Collection<String> translate(T input);
}
