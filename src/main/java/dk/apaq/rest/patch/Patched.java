package dk.apaq.rest.patch;

import com.fasterxml.jackson.core.TreeNode;
import dk.apaq.rest.patch.jackson.TreeNodePropertyReferenceConverter;

import java.util.Collection;

/**
 * A wrapper around a deserialized value and the raw JSON tree node it was deserialized from.
 *
 * <p>Unlike the thread-local {@code TreeNodeHolder}, this type carries the tree node alongside the
 * value, so it can be used safely with nested, sequential or concurrent deserializations without
 * relying on global state.
 *
 * @param <T> The type of the deserialized value.
 */
public record Patched<T>(T value, TreeNode node) {

    /**
     * Translates the captured tree node into the list of dirty field paths present in the request.
     *
     * @return The collection of dirty field names represented by the raw request payload.
     */
    public Collection<String> dirtyFields() {
        return new TreeNodePropertyReferenceConverter().translate(node);
    }
}
