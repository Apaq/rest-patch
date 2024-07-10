package dk.apaq.rest.patch.jackson;

import dk.apaq.rest.patch.DummyEntity;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class TreeNodeExtractorModuleTest {

    private TreeNodeExtractorModule module = new TreeNodeExtractorModule();
    private TreeNodePropertyReferenceConverter converter = new TreeNodePropertyReferenceConverter();

    @Test
    public void testTreeNodeResolvesField() throws IOException {
        var mapper = new ObjectMapper();
        mapper.registerModule(module);
        DummyEntity de = mapper.readValue("{\"text\": \"qwerty\"}", DummyEntity.class);
        Iterable<String> fields = converter.translate(TreeNodeHolder.get());
        assertEquals(Collections.singletonList("text"), fields);
    }

    @Test
    public void testTreeNodeResolvesChildField() throws IOException {
        var mapper = new ObjectMapper();
        mapper.registerModule(module);
        DummyEntity de = mapper.readValue("{\"child\": {\"text\":\"qwerty\"}}", DummyEntity.class);
        Iterable<String> fields = converter.translate(TreeNodeHolder.get());
        assertEquals(Collections.singletonList("child.text"), fields);
    }

    @Test
    public void testTreeNodeResolvedWithReaderFor() throws IOException {
        var json = "{\"text\": \"value\", \"child\": {\"text\":\"qwerty\", \"list\":[\"Karl\"],\"child\": {\"text\":\"value\"}}}";
        var mapper = new ObjectMapper();
        mapper.registerModule(module);
        DummyEntity de = mapper.readerFor(DummyEntity.class).readValue(json, DummyEntity.class);
        Iterable<String> fields = converter.translate(TreeNodeHolder.get());
        assertEquals(Arrays.asList("text", "child.text", "child.list", "child.child.text"), fields);
    }
}
