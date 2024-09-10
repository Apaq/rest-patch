package dk.apaq.rest.patch.jackson;

import com.fasterxml.jackson.core.TreeNode;
import dk.apaq.rest.patch.PropertyReferenceConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A converter class that transforms a Jackson {@link TreeNode} into a collection of property reference field names.
 * This class is useful for extracting field names from a parsed JSON tree, where the fields represent
 * paths in the object hierarchy.
 */
public class TreeNodePropertyReferenceConverter implements PropertyReferenceConverter<TreeNode> {

    /**
     * Converts the given {@link TreeNode} into a collection of string property references.
     * If the input is an object, it traverses the object structure to collect field names.
     *
     * @param input The input {@link TreeNode} to be converted.
     * @return A collection of field names representing paths in the JSON object tree.
     */
    @Override
    public Collection<String> translate(TreeNode input) {
        var refs = new ArrayList<String>();
        if (input.isObject()) {
            traverseObject(input, refs, new ArrayList<>());
        }
        return refs;
    }

    /**
     * Traverses a JSON array node, extracting field paths for each element.
     *
     * @param treeNode The current {@link TreeNode} representing the array.
     * @param fields   The list to collect the field names.
     * @param path     The current traversal path in the JSON hierarchy.
     */
    private void traverseArray(TreeNode treeNode, List<String> fields, List<String> path) {
        for (int i = 0; i < treeNode.size(); i++) {
            path.add("[" + i + "]");
            TreeNode child = treeNode.get(i);
            traverse(child, fields, path);
            path.remove(path.size() - 1);
        }
    }

    /**
     * Traverses a JSON object node, extracting field paths for each field.
     *
     * @param treeNode The current {@link TreeNode} representing the object.
     * @param fields   The list to collect the field names.
     * @param path     The current traversal path in the JSON hierarchy.
     */
    private void traverseObject(TreeNode treeNode, List<String> fields, List<String> path) {
        treeNode.fieldNames().forEachRemaining(item -> {
            path.add("." + item);
            TreeNode child = treeNode.get(item);
            traverse(child, fields, path);
            path.remove(path.size() - 1);
        });
    }

    /**
     * Traverses a {@link TreeNode} of any type (object, array, or value).
     * Directs traversal to the appropriate method based on the node type.
     *
     * @param child  The current child {@link TreeNode}.
     * @param fields The list to collect the field names.
     * @param path   The current traversal path in the JSON hierarchy.
     */
    private void traverse(TreeNode child, List<String> fields, List<String> path) {
        if (child.isObject()) {
            traverseObject(child, fields, path);
        }

        if (child.isArray()) {
            // Array elements are treated as values; single elements are not supported.
            traverseValue(path, fields);
        }

        if (child.isValueNode()) {
            traverseValue(path, fields);
        }
    }

    /**
     * Extracts the current path and adds it to the fields list, handling both object and array value nodes.
     *
     * @param path   The current traversal path.
     * @param fields The list to collect the field names.
     */
    private void traverseValue(List<String> path, List<String> fields) {
        var strPath = String.join("", path);
        if (strPath.startsWith(".")) {
            strPath = strPath.substring(1);
        }
        fields.add(strPath);
    }
}
