package dk.apaq.rest.patch.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.DelegatingDeserializer;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;

import java.io.IOException;

public class TreeNodeExtractorDeserializer extends DelegatingDeserializer {
    
    public TreeNodeExtractorDeserializer(JsonDeserializer<?> defaultDeserializer) {
        super(defaultDeserializer);
    }

    @Override
    protected JsonDeserializer<?> newDelegatingInstance(JsonDeserializer<?> newDelegate) {
        return new TreeNodeExtractorDeserializer(newDelegate);
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        var context = p.getParsingContext();
        var depth = context.getNestingDepth();

        if(depth == 1 && !(p instanceof TreeTraversingParser)) {
            // Extract the JSON tree
            TreeNode treeNode = p.readValueAsTree();
            TreeNodeHolder.set(treeNode);

            // Re-parse the tree node to get the actual object
            JsonParser treeParser = treeNode.traverse(ctxt.getParser().getCodec());
            if(treeParser.getCurrentToken() == null)
                treeParser.nextToken();
            return super.deserialize(treeParser, ctxt);
        } else {
            return super.deserialize(p, ctxt);
        }
    }
}
