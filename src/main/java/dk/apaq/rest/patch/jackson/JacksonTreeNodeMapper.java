package dk.apaq.rest.patch.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.apaq.rest.patch.Patched;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Custom Jackson {@link ObjectMapper} that captures each parsed JSON tree node and places it into {@link TreeNodeHolder}.
 * This class is useful when you need to intercept and hold the tree representation of JSON objects during deserialization.
 *
 * <p>It also provides the holder-free {@link #readPatchedValue(String, Class)} family of methods, which
 * return the deserialized value together with the raw tree node via a {@link Patched} wrapper.
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
        // Drop any previously captured node so a failed parse never leaves stale data behind.
        TreeNodeHolder.clear();

        // Read the entire JSON input into a tree structure
        var node = jp.readValueAsTree();

        // Store the tree node in TreeNodeHolder for later retrieval or processing
        TreeNodeHolder.set(node);

        // Traverse the tree node and deserialize it to the target valueType
        return super._readMapAndClose(node.traverse(), valueType);
    }

    /**
     * Deserializes the given JSON content into a value of the target type and returns it wrapped in a
     * {@link Patched} instance together with the raw tree node. Unlike {@link #readValue(String, Class)},
     * the tree node is returned directly instead of relying on the thread-local {@link TreeNodeHolder}.
     *
     * @param content   The JSON content to deserialize.
     * @param valueType The target Java type to which the JSON data is to be deserialized.
     * @param <T>       The type of the deserialized value.
     * @return The deserialized value and its raw tree node.
     * @throws IOException If an error occurs during the parsing or mapping process.
     */
    public <T> Patched<T> readPatchedValue(String content, Class<T> valueType) throws IOException {
        return readPatchedValue(readTree(content), valueType);
    }

    /**
     * Deserializes the given JSON content into a value of the target type and returns it wrapped in a
     * {@link Patched} instance together with the raw tree node.
     *
     * @param content   The JSON content to deserialize.
     * @param valueType The target Java type to which the JSON data is to be deserialized.
     * @param <T>       The type of the deserialized value.
     * @return The deserialized value and its raw tree node.
     * @throws IOException If an error occurs during the parsing or mapping process.
     */
    public <T> Patched<T> readPatchedValue(byte[] content, Class<T> valueType) throws IOException {
        return readPatchedValue(readTree(content), valueType);
    }

    /**
     * Deserializes the given JSON content into a value of the target type and returns it wrapped in a
     * {@link Patched} instance together with the raw tree node.
     *
     * @param content   The JSON content to deserialize.
     * @param valueType The target Java type to which the JSON data is to be deserialized.
     * @param <T>       The type of the deserialized value.
     * @return The deserialized value and its raw tree node.
     * @throws IOException If an error occurs during the parsing or mapping process.
     */
    public <T> Patched<T> readPatchedValue(InputStream content, Class<T> valueType) throws IOException {
        return readPatchedValue(readTree(content), valueType);
    }

    /**
     * Deserializes the given JSON content into a value of the target type and returns it wrapped in a
     * {@link Patched} instance together with the raw tree node.
     *
     * @param content   The JSON content to deserialize.
     * @param valueType The target Java type to which the JSON data is to be deserialized.
     * @param <T>       The type of the deserialized value.
     * @return The deserialized value and its raw tree node.
     * @throws IOException If an error occurs during the parsing or mapping process.
     */
    public <T> Patched<T> readPatchedValue(Reader content, Class<T> valueType) throws IOException {
        return readPatchedValue(readTree(content), valueType);
    }

    private <T> Patched<T> readPatchedValue(JsonNode node, Class<T> valueType) throws IOException {
        return new Patched<>(treeToValue(node, valueType), node);
    }
}
