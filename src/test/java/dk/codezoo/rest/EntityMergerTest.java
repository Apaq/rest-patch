package dk.codezoo.rest;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.Collections;

public class EntityMergerTest {


    private EntityMerger<DummyEntity> merger = new EntityMerger<>();

    @Test
    public void testMergeString() {
        DummyEntity patch = new DummyEntity("ytrewq", 0, null, null);
        DummyEntity persistence = new DummyEntity("qwerty", 1, new String[]{"A", "B", "C"}, Collections.singletonList("test"));

        merger.mergeEntities(persistence, patch, Collections.singletonList("text"));
        assertEquals("ytrewq", persistence.getText());
        assertEquals(1, persistence.getNumber());
        assertArrayEquals(new String[]{"A", "B", "C"}, persistence.getArray());
        assertEquals(Collections.singletonList("test"), persistence.getList());
    }

    @Test
    public void testMergeInt() {
        DummyEntity patch = new DummyEntity(null, 0, null, null);
        DummyEntity persistence = new DummyEntity("qwerty", 1, new String[]{"A", "B", "C"}, Collections.singletonList("test"));

        merger.mergeEntities(persistence, patch, Collections.singletonList("number"));
        assertEquals("qwerty", persistence.getText());
        assertEquals(0, persistence.getNumber());
        assertArrayEquals(new String[]{"A", "B", "C"}, persistence.getArray());
        assertEquals(Collections.singletonList("test"), persistence.getList());
    }

    @Test
    public void testMergeArray() {
        DummyEntity patch = new DummyEntity(null, 0, new String[]{"C", "B", "A"}, null);
        DummyEntity persistence = new DummyEntity("qwerty", 1, new String[]{"A", "B", "C"}, Collections.singletonList("test"));

        merger.mergeEntities(persistence, patch, Collections.singletonList("array"));
        assertEquals("qwerty", persistence.getText());
        assertEquals(1, persistence.getNumber());
        assertArrayEquals(new String[]{"C", "B", "A"}, persistence.getArray());
        assertEquals(Collections.singletonList("test"), persistence.getList());
    }

    @Test
    public void testMergeArraySpecificElement() {
        DummyEntity patch = new DummyEntity(null, 0, new String[]{"C", "B", "A"}, null);
        DummyEntity persistence = new DummyEntity("qwerty", 1, new String[]{"A", "B", "C"}, Collections.singletonList("test"));

        merger.mergeEntities(persistence, patch, Collections.singletonList("array[0]"));
        assertEquals("qwerty", persistence.getText());
        assertEquals(1, persistence.getNumber());
        assertArrayEquals(new String[]{"C", "B", "C"}, persistence.getArray());
        assertEquals(Collections.singletonList("test"), persistence.getList());
    }

    @Test
    public void testMergeList() {
        DummyEntity patch = new DummyEntity(null, 0, null, Collections.singletonList("qwerty"));
        DummyEntity persistence = new DummyEntity("qwerty", 1, new String[]{"A", "B", "C"}, Collections.singletonList("test"));

        merger.mergeEntities(persistence, patch, Collections.singletonList("list"));
        assertEquals("qwerty", persistence.getText());
        assertEquals(1, persistence.getNumber());
        assertArrayEquals(new String[]{"A", "B", "C"}, persistence.getArray());
        assertEquals(Collections.singletonList("qwerty"), persistence.getList());
    }

    @Test
    public void testMergeChildString() {
        DummyEntity patch = new DummyEntity(null, 0, null, null);
        patch.setChild(new DummyEntity("sibling", 0, null, null));
        DummyEntity persistence = new DummyEntity("qwerty", 1, new String[]{"A", "B", "C"}, Collections.singletonList("test"));
        persistence.setChild(new DummyEntity("child", 1, null, null));

        merger.mergeEntities(persistence, patch, Collections.singletonList("child.text"));
        assertEquals("sibling", persistence.getChild().getText());
    }

    @Test
    public void testMergeChildInt() {
        DummyEntity patch = new DummyEntity(null, 0, null, null);
        patch.setChild(new DummyEntity(null, 0, null, null));
        DummyEntity persistence = new DummyEntity("qwerty", 1, new String[]{"A", "B", "C"}, Collections.singletonList("test"));
        persistence.setChild(new DummyEntity("child", 1, null, null));

        merger.mergeEntities(persistence, patch, Collections.singletonList("child.number"));
        assertEquals(0, persistence.getChild().getNumber());
    }

    @Test
    public void testMergeChildArray() {
        DummyEntity patch = new DummyEntity(null, 0, null, null);
        patch.setChild(new DummyEntity(null, 0, new String[]{"C", "B", "A"}, null));
        DummyEntity persistence = new DummyEntity("qwerty", 1, null, Collections.singletonList("test"));
        persistence.setChild(new DummyEntity("child", 1, new String[]{"A", "B", "C"}, null));

        merger.mergeEntities(persistence, patch, Collections.singletonList("child.array"));
        assertArrayEquals(new String[]{"C", "B", "A"}, persistence.getChild().getArray());
    }

    @Test
    public void testMergeChildArraySpecific() {
        DummyEntity patch = new DummyEntity(null, 0, null, null);
        patch.setChild(new DummyEntity(null, 0, new String[]{"C", "B", "A"}, null));
        DummyEntity persistence = new DummyEntity("qwerty", 1, null, Collections.singletonList("test"));
        persistence.setChild(new DummyEntity("child", 1, new String[]{"A", "B", "C"}, null));

        merger.mergeEntities(persistence, patch, Collections.singletonList("child.array[2]"));
        assertArrayEquals(new String[]{"A", "B", "A"}, persistence.getChild().getArray());
    }
}
