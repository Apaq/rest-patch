package dk.apaq.rest.patch;

import java.util.Collection;

@FunctionalInterface
public interface PropertyReferenceConverter<T> {

    public Collection<String> translate(T input);
}