package dk.apaq.rest.patch.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Jackson ObjectMapper that puts each new treenode into TreeNodeHolder.
 */
public class JacksonTreeNodeMapper extends ObjectMapper {

    @Override
    protected Object _readMapAndClose(JsonParser jp, JavaType valueType) throws IOException {
        var node = jp.readValueAsTree();
        TreeNodeHolder.set(node);
        return super._readMapAndClose(node.traverse(), valueType);
    }
}
