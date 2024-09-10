package dk.apaq.rest.patch.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.DelegatingDeserializer;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;

import java.io.IOException;

/**
 * Custom deserializer that extracts and stores the root JSON tree node in {@link TreeNodeHolder}.
 * This class extends {@link DelegatingDeserializer}, allowing it to delegate actual deserialization
 * to the default deserializer while also capturing the tree structure for further processing.
 */
public class TreeNodeExtractorDeserializer extends DelegatingDeserializer {

    /**
     * Constructor that accepts the default deserializer which will handle the actual deserialization.
     *
     * @param defaultDeserializer The default deserializer to delegate deserialization to.
     */
    public TreeNodeExtractorDeserializer(JsonDeserializer<?> defaultDeserializer) {
        super(defaultDeserializer);
    }

    /**
     * Creates a new instance of this deserializer with a new delegate. This is required for Jackson's
     * internal deserialization processes when delegation changes occur.
     *
     * @param newDelegate The new deserializer delegate.
     * @return A new instance of {@code TreeNodeExtractorDeserializer} with the new delegate.
     */
    @Override
    protected JsonDeserializer<?> newDelegatingInstance(JsonDeserializer<?> newDelegate) {
        return new TreeNodeExtractorDeserializer(newDelegate);
    }

    /**
     * Deserializes the input JSON, capturing the root JSON tree node in {@link TreeNodeHolder} when at the root level.
     * If the deserialization depth is 1, it extracts the JSON tree and re-parses it. Otherwise, it delegates
     * deserialization to the underlying deserializer.
     *
     * @param p     The JSON parser.
     * @param ctxt  The deserialization context.
     * @return The deserialized object.
     * @throws IOException If an I/O error occurs during parsing or deserialization.
     */
    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        var context = p.getParsingContext();
        var depth = context.getNestingDepth();

        // Check if we are at the root level of the JSON structure
        if (depth == 1 && !(p instanceof TreeTraversingParser)) {
            // Extract the root JSON tree node
            TreeNode treeNode = p.readValueAsTree();
            TreeNodeHolder.set(treeNode);  // Store the extracted node in TreeNodeHolder

            // Re-parse the tree node using a new parser
            JsonParser treeParser = treeNode.traverse(ctxt.getParser().getCodec());
            if (treeParser.getCurrentToken() == null) {
                treeParser.nextToken();  // Ensure the parser is positioned at the first token
            }

            // Delegate the actual deserialization using the re-parsed tree node
            return super.deserialize(treeParser, ctxt);
        } else {
            // If not at root level, delegate deserialization to the default deserializer
            return super.deserialize(p, ctxt);
        }
    }
}
