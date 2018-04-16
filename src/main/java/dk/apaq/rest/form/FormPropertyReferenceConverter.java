package dk.apaq.rest.form;

import dk.apaq.rest.PropertyReferenceConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class FormPropertyReferenceConverter implements PropertyReferenceConverter<Map<String, String[]>> {

    /**
     * Pattern for matching map references defined as array, fx. 'meta[color]'.
     */
    private static final Pattern FORM_MAP_REFERENCE_PATTERN = Pattern.compile("((\\[)([a-zA-Z]{1}[a-zA-Z0-9\\_]*)(\\]))");

    /**
     * Translates Form input map to list of fields.
     * This method will use all keys directly as fields. Only exception is that map references defined as arrays with have
     * square brackets replaced with parantheses.
     *
     * __Example__
     * meta[color] becomes meta(color)
     *
     * @param input Map of properties
     * @return Returns list of fields
     */
    @Override
    public Collection<String> translate(Map<String, String[]> input) {
        List<String> refs = new ArrayList<>();
        for(String key: input.keySet()) {
            refs.add(FORM_MAP_REFERENCE_PATTERN.matcher(key).replaceAll("($3)"));
        }
        return refs;
    }

}
