package dk.apaq.rest.patch;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

    @Test
    public void testMergeListSpecificElement() {
        DummyEntity patch = new DummyEntity(null, 0, null, java.util.Arrays.asList("qwerty"));
        DummyEntity persistence = new DummyEntity("qwerty", 1, new String[]{"A", "B", "C"}, java.util.Arrays.asList("test", "other"));

        merger.mergeEntities(persistence, patch, Collections.singletonList("list[0]"));
        assertEquals(java.util.Arrays.asList("qwerty", "other"), persistence.getList());
    }

    @Test
    public void testMergeMapSpecificKey() {
        DummyEntity patch = new DummyEntity(null, 0, null, null);
        Map<String, String> patchMeta = new HashMap<>();
        patchMeta.put("color", "red");
        patch.setMeta(patchMeta);

        DummyEntity persistence = new DummyEntity("qwerty", 1, null, null);
        Map<String, String> persistenceMeta = new HashMap<>();
        persistenceMeta.put("size", "big");
        persistence.setMeta(persistenceMeta);

        merger.mergeEntities(persistence, patch, Collections.singletonList("meta(color)"));

        assertEquals("red", persistence.getMeta().get("color"));
        assertEquals("big", persistence.getMeta().get("size"));
    }

    @Test
    public void testMergeWholeMap() {
        DummyEntity patch = new DummyEntity(null, 0, null, null);
        Map<String, String> patchMeta = new HashMap<>();
        patchMeta.put("color", "red");
        patch.setMeta(patchMeta);

        DummyEntity persistence = new DummyEntity("qwerty", 1, null, null);
        persistence.setMeta(new HashMap<>());

        merger.mergeEntities(persistence, patch, Collections.singletonList("meta"));

        assertEquals("red", persistence.getMeta().get("color"));
    }

    @Test
    public void testMergeIgnoredFields() {
        DummyEntity patch = new DummyEntity("ytrewq", 2, null, null);
        DummyEntity persistence = new DummyEntity("qwerty", 1, null, null);

        merger.mergeEntities(persistence, patch, java.util.Arrays.asList("text", "number"), Collections.singletonList("text"));

        assertEquals("qwerty", persistence.getText());
        assertEquals(2, persistence.getNumber());
    }

    @Test
    public void testMergeDefaultIgnoredFields() {
        EntityMerger<DummyEntity> defaultIgnoringMerger = new EntityMerger<>(Collections.singletonList("text"));
        DummyEntity patch = new DummyEntity("ytrewq", 2, null, null);
        DummyEntity persistence = new DummyEntity("qwerty", 1, null, null);

        defaultIgnoringMerger.mergeEntities(persistence, patch, java.util.Arrays.asList("text", "number"));

        assertEquals("qwerty", persistence.getText());
        assertEquals(2, persistence.getNumber());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMergeInvalidFieldThrows() {
        DummyEntity patch = new DummyEntity("ytrewq", 0, null, null);
        DummyEntity persistence = new DummyEntity("qwerty", 1, null, null);

        merger.mergeEntities(persistence, patch, Collections.singletonList("doesNotExist"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMergeNullIntermediateThrows() {
        DummyEntity patch = new DummyEntity(null, 0, null, null);
        patch.setChild(new DummyEntity("sibling", 0, null, null));
        DummyEntity persistence = new DummyEntity("qwerty", 1, null, null);

        merger.mergeEntities(persistence, patch, Collections.singletonList("child.text"));
    }

    @Test(expected = NullPointerException.class)
    public void testMergeNullEntitiesThrows() {
        merger.mergeEntities(null, new DummyEntity(), Collections.singletonList("text"));
    }

    @Test(expected = NullPointerException.class)
    public void testMergeNullDirtyFieldsThrows() {
        merger.mergeEntities(new DummyEntity(), new DummyEntity(), null);
    }
}
