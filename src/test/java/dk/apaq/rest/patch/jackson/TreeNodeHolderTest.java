package dk.apaq.rest.patch.jackson;

import org.junit.After;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class TreeNodeHolderTest {

    @After
    public void tearDown() {
        TreeNodeHolder.clear();
    }

    @Test
    public void testInitiallyEmpty() {
        assertNull(TreeNodeHolder.get());
    }

    @Test
    public void testSetAndGet() throws IOException {
        var mapper = new JacksonTreeNodeMapper();
        mapper.readValue("{\"text\": \"qwerty\"}", dk.apaq.rest.patch.DummyEntity.class);
        assertNotNull(TreeNodeHolder.get());
        assertTrue(TreeNodeHolder.get().isObject());
    }

    @Test
    public void testClearRemovesValue() throws IOException {
        var mapper = new JacksonTreeNodeMapper();
        mapper.readValue("{\"text\": \"qwerty\"}", dk.apaq.rest.patch.DummyEntity.class);
        assertNotNull(TreeNodeHolder.get());

        TreeNodeHolder.clear();
        assertNull(TreeNodeHolder.get());
    }

    @Test
    public void testFailedReadLeavesNoStaleValue() {
        // Simulate a stale node being present before a failing read.
        TreeNodeHolder.set(com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode());
        var mapper = new JacksonTreeNodeMapper();

        try {
            mapper.readValue("{not valid json", dk.apaq.rest.patch.DummyEntity.class);
            fail("Expected readValue to throw");
        } catch (Exception expected) {
            // expected
        }

        // A failed parse must not leave the previous request's node behind.
        assertNull(TreeNodeHolder.get());
    }
}
