package dk.apaq.rest.patch.jackson;

import dk.apaq.rest.patch.DummyEntity;
import dk.apaq.rest.patch.Patched;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class PatchedTest {

    private final JacksonTreeNodeMapper mapper = new JacksonTreeNodeMapper();

    @Test
    public void testReadPatchedValueResolvesValueAndFields() throws IOException {
        Patched<DummyEntity> patched = mapper.readPatchedValue("{\"text\": \"qwerty\"}", DummyEntity.class);

        assertEquals("qwerty", patched.value().getText());
        assertNotNull(patched.node());
        assertEquals(Collections.singletonList("text"), patched.dirtyFields());
    }

    @Test
    public void testReadPatchedValueResolvesNestedFields() throws IOException {
        Patched<DummyEntity> patched = mapper.readPatchedValue(
                "{\"child\": {\"text\":\"qwerty\"}}", DummyEntity.class);

        assertEquals(Collections.singletonList("child.text"), patched.dirtyFields());
    }

    @Test
    public void testReadPatchedValueResolvesMultipleFields() throws IOException {
        Patched<DummyEntity> patched = mapper.readPatchedValue(
                "{\"text\": \"value\", \"child\": {\"text\":\"qwerty\", \"list\":[\"Karl\"],\"child\": {\"text\":\"value\"}}}",
                DummyEntity.class);

        assertEquals(Arrays.asList("text", "child.text", "child.list", "child.child.text"), patched.dirtyFields());
    }

    @Test
    public void testReadPatchedValueFromInputStream() throws IOException {
        Patched<DummyEntity> patched = mapper.readPatchedValue(
                new ByteArrayInputStream("{\"text\": \"qwerty\"}".getBytes(StandardCharsets.UTF_8)), DummyEntity.class);

        assertEquals("qwerty", patched.value().getText());
        assertEquals(Collections.singletonList("text"), patched.dirtyFields());
    }

    @Test
    public void testReadPatchedValueFromReader() throws IOException {
        Patched<DummyEntity> patched = mapper.readPatchedValue(
                new InputStreamReader(new ByteArrayInputStream("{\"text\": \"qwerty\"}".getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8),
                DummyEntity.class);

        assertEquals("qwerty", patched.value().getText());
        assertEquals(Collections.singletonList("text"), patched.dirtyFields());
    }

    @Test
    public void testReadPatchedValueFromBytes() throws IOException {
        Patched<DummyEntity> patched = mapper.readPatchedValue(
                "{\"text\": \"qwerty\"}".getBytes(StandardCharsets.UTF_8), DummyEntity.class);

        assertEquals("qwerty", patched.value().getText());
        assertEquals(Collections.singletonList("text"), patched.dirtyFields());
    }

    @Test
    public void testPatchedNodeIsIndependentOfHolder() throws IOException {
        Patched<DummyEntity> patched = mapper.readPatchedValue("{\"text\": \"qwerty\"}", DummyEntity.class);

        // The returned node must keep working even after the thread-local holder is cleared.
        TreeNodeHolder.clear();
        assertEquals(Collections.singletonList("text"), patched.dirtyFields());

        // And a subsequent, unrelated read must not affect the already captured patched instance.
        Patched<DummyEntity> other = mapper.readPatchedValue("{\"text\": \"other\"}", DummyEntity.class);
        assertEquals(Collections.singletonList("text"), patched.dirtyFields());
        assertEquals(Collections.singletonList("text"), other.dirtyFields());
        assertEquals("qwerty", patched.value().getText());
        assertEquals("other", other.value().getText());
    }
}
