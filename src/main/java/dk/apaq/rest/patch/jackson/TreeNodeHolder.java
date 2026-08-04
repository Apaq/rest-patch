package dk.apaq.rest.patch.jackson;

import com.fasterxml.jackson.core.TreeNode;

/**
 * A utility class that holds a {@link TreeNode} in a thread-local context.
 * This allows each thread to store and access the parsed JSON tree node during the deserialization process.
 *
 * <p>The holder is a single slot per thread and is therefore neither re-entrant nor safe across
 * multiple deserializations on the same thread; each deserialization overwrites the previous value.
 * It is also cleared at the start of every capture, so a failed or aborted deserialization will
 * never leave a stale tree node behind. Consumers should call {@link #clear()} once the captured
 * node has been consumed (e.g. in a {@code finally} block) to avoid retaining the tree until the
 * next deserialization on that thread.
 *
 * <p>Prefer the holder-free {@code Patched} API (see {@code JacksonTreeNodeMapper#readPatchedValue})
 * when you control the read, as it returns the tree node alongside the deserialized value without
 * relying on thread-local state.
 *
 * This class is typically used in conjunction with the {@link TreeNodeExtractorDeserializer} and
 * {@link TreeNodeExtractorModule} to capture and hold the root JSON tree node for later use.
 */
public class TreeNodeHolder {

    // ThreadLocal variable to store the TreeNode, ensuring thread-safe access to the JSON tree node.
    private static final ThreadLocal<TreeNode> TREE_NODE = new ThreadLocal<>();

    /**
     * Private constructor to prevent instantiation. This is a utility class and should not be instantiated.
     *
     * @throws IllegalAccessError if the constructor is called.
     */
    private TreeNodeHolder() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * Retrieves the currently stored {@link TreeNode} for the current thread.
     *
     * @return The {@link TreeNode} stored in the current thread's context, or {@code null} if none is set.
     */
    public static TreeNode get() {
        return TREE_NODE.get();
    }

    /**
     * Stores the provided {@link TreeNode} in the current thread's context.
     *
     * @param treenode The {@link TreeNode} to store.
     */
    public static void set(TreeNode treenode) {
        TREE_NODE.set(treenode);
    }

    /**
     * Removes the currently stored {@link TreeNode} for the current thread, releasing any
     * references held by the holder. Call this once the captured node has been consumed to avoid
     * retaining the tree until the next deserialization on the same thread.
     */
    public static void clear() {
        TREE_NODE.remove();
    }
}
