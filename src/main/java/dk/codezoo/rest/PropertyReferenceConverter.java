package dk.codezoo.rest;

import java.util.Collection;

@FunctionalInterface
public interface PropertyReferenceConverter<T> {

    public Collection<String> translate(T input);
}