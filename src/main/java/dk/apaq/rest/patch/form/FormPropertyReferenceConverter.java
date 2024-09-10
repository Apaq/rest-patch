package dk.apaq.rest.patch.form;

import dk.apaq.rest.patch.PropertyReferenceConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A converter class that transforms a form input map (typically from HTML forms) into a collection of property references.
 * It processes keys from the form input and adjusts them based on specific patterns, such as converting array-like
 * references (e.g., 'meta[color]') to a more standardized format (e.g., 'meta(color)').
 */
public class FormPropertyReferenceConverter implements PropertyReferenceConverter<Map<String, String[]>> {

    /**
     * A pattern to match form keys that reference map-like arrays, e.g., 'meta[color]'.
     * This pattern captures keys that use square brackets to reference fields in nested maps or arrays.
     */
    private static final Pattern FORM_MAP_REFERENCE_PATTERN = Pattern.compile("((\\[)([a-zA-Z]{1}[a-zA-Z0-9\\_]*)(\\]))");

    /**
     * Translates the given form input map into a list of property reference field names.
     * This method takes all keys from the input map directly as fields, except when they match the pattern
     * for array/map references (e.g., 'meta[color]'), which will be transformed by replacing square brackets with parentheses.
     *
     * <p><strong>Example:</strong><br>
     * 'meta[color]' becomes 'meta(color)'.
     * </p>
     *
     * @param input The input map containing form fields as keys and their values as string arrays.
     * @return A collection of field names with adjusted formatting for map-like references.
     */
    @Override
    public Collection<String> translate(Map<String, String[]> input) {
        List<String> refs = new ArrayList<>();
        for (String key : input.keySet()) {
            // Replace keys matching the form map reference pattern and collect them into refs.
            refs.add(FORM_MAP_REFERENCE_PATTERN.matcher(key).replaceAll("($3)"));
        }
        return refs;
    }
}
