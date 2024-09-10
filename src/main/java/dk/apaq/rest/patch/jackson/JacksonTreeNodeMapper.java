package dk.apaq.rest.patch.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Custom Jackson {@link ObjectMapper} that captures each parsed JSON tree node and places it into {@link TreeNodeHolder}.
 * This class is useful when you need to intercept and hold the tree representation of JSON objects during deserialization.
 */
public class JacksonTreeNodeMapper extends ObjectMapper {

    /**
     * Reads the JSON data, stores the resulting tree node in {@link TreeNodeHolder}, and then proceeds to map the data
     * to the target Java type.
     *
     * @param jp        The {@link JsonParser} used for reading JSON input.
     * @param valueType The target Java type to which the JSON data is to be deserialized.
     * @return The deserialized object of type {@code valueType}.
     * @throws IOException If an error occurs during the parsing or mapping process.
     */
    @Override
    protected Object _readMapAndClose(JsonParser jp, JavaType valueType) throws IOException {
        // Read the entire JSON input into a tree structure
        var node = jp.readValueAsTree();

        // Store the tree node in TreeNodeHolder for later retrieval or processing
        TreeNodeHolder.set(node);

        // Traverse the tree node and deserialize it to the target valueType
        return super._readMapAndClose(node.traverse(), valueType);
    }
}
