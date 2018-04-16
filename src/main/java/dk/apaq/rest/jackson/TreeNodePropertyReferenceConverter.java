package dk.apaq.rest.jackson;

import com.fasterxml.jackson.core.TreeNode;
import dk.apaq.rest.PropertyReferenceConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Converter for Jackson TreeNodes into a Collection of fieldnames.
 */
public class TreeNodePropertyReferenceConverter implements PropertyReferenceConverter<TreeNode> {

    @Override
    public Collection<String> translate(TreeNode input) {
        List<String> refs = new ArrayList<>();
        if(input.isObject()) {
            traverseObject(input, refs, new ArrayList());
        }
        return refs;
    }
    
    private void traverseArray(TreeNode treeNode, List<String> fields, List<String> path) {
        
        for(int i=0;i<treeNode.size();i++) {
            path.add("[" + i + "]");
            TreeNode child = treeNode.get(i);
            traverse(child, fields, path);
            path.remove(path.size()-1);
        }
    }
    
    private void traverseObject(TreeNode treeNode, List<String> fields, List<String> path) {
        treeNode.fieldNames().forEachRemaining(item -> {
            path.add("." + item);
            TreeNode child = treeNode.get(item);
            traverse(child, fields, path);
            path.remove(path.size()-1);
        });
    }
    
    private void traverse(TreeNode child, List<String> fields, List<String> path) {
        if(child.isObject()) {
            traverseObject(child, fields, path);
        }
        
        if(child.isArray()) {
            // We currently do not support setting single array elements. Consider it a value,
            //traverseArray(child, fields, path);
            
            traverseValue(path, fields);
        }
        
        if(child.isValueNode()) {
            traverseValue(path, fields);
        }
    }

    private void traverseValue(List<String> path, List<String> fields) {
        String strPath = String.join("", path);
        if(strPath.startsWith(".")) {
            strPath = strPath.substring(1);
        }
        fields.add(strPath);
    }

}