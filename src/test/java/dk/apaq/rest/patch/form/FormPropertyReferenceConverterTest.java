package dk.apaq.rest.patch.form;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class FormPropertyReferenceConverterTest {

    private final FormPropertyReferenceConverter converter = new FormPropertyReferenceConverter();

    @Test
    public void testPlainKeysPassThrough() {
        Map<String, String[]> input = new HashMap<>();
        input.put("name", new String[]{"Bessie"});

        assertEquals(java.util.Collections.singletonList("name"), converter.translate(input));
    }

    @Test
    public void testMapReferenceIsTransformed() {
        Map<String, String[]> input = new HashMap<>();
        input.put("meta[color]", new String[]{"red"});

        assertEquals(java.util.Collections.singletonList("meta(color)"), converter.translate(input));
    }

    @Test
    public void testMixedKeys() {
        Map<String, String[]> input = new HashMap<>();
        input.put("name", new String[]{"Bessie"});
        input.put("meta[color]", new String[]{"red"});
        input.put("meta[size]", new String[]{"big"});

        java.util.Collection<String> refs = converter.translate(input);
        assertTrue(refs.contains("name"));
        assertTrue(refs.contains("meta(color)"));
        assertTrue(refs.contains("meta(size)"));
        assertEquals(3, refs.size());
    }

    @Test
    public void testEmptyInput() {
        assertEquals(0, converter.translate(new HashMap<>()).size());
    }
}
