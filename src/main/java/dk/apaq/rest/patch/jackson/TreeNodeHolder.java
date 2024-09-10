package dk.apaq.rest.patch.jackson;

import com.fasterxml.jackson.core.TreeNode;

/**
 * A utility class that holds a {@link TreeNode} in a thread-local context.
 * This allows each thread to store and access the parsed JSON tree node during the deserialization process.
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
}
