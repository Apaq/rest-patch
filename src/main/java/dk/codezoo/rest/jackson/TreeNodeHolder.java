package dk.codezoo.rest.jackson;

import com.fasterxml.jackson.core.TreeNode;

public class TreeNodeHolder {

    private static final ThreadLocal<TreeNode> TREE_NODE = new ThreadLocal<>();

    private TreeNodeHolder() {
        throw new IllegalAccessError("Utility class");
    }

    public static TreeNode get() {
        return TREE_NODE.get();
    }

    public static void set(TreeNode treenode) {
        TREE_NODE.set(treenode);
    }
}
