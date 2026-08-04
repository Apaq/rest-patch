package dk.apaq.rest.patch;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Internal helper that reads and writes bean properties using path expressions such as
 * {@code child.text}, {@code array[0]}, {@code list[0].text} or {@code meta(color)} (Map access).
 *
 * <p>This replaces the historical dependency on Apache Commons BeanUtils while preserving the
 * supported path syntax. Property descriptors are introspected once per class and cached.
 */
final class PropertyAccess {

    private static final Map<Class<?>, Map<String, PropertyDescriptor>> DESCRIPTOR_CACHE = new ConcurrentHashMap<>();

    private PropertyAccess() {
    }

    private sealed interface Op permits PropertyOp, IndexOp, MapKeyOp {
    }

    private record PropertyOp(String name) implements Op {
    }

    private record IndexOp(int index) implements Op {
    }

    private record MapKeyOp(String key) implements Op {
    }

    /**
     * Reads the value at the given path from the target object.
     *
     * @param target The object to read from.
     * @param path   The property path to resolve.
     * @return The resolved value.
     * @throws IllegalArgumentException If the path cannot be resolved.
     */
    static Object getValue(Object target, String path) {
        List<Op> ops = parse(path);
        Object current = target;
        for (Op op : ops) {
            current = read(current, op, path);
        }
        return current;
    }

    /**
     * Writes the given value to the path on the target object.
     *
     * @param target The object to write to.
     * @param path   The property path to resolve.
     * @param value  The value to write.
     * @throws IllegalArgumentException If the path cannot be resolved.
     */
    static void setValue(Object target, String path, Object value) {
        List<Op> ops = parse(path);
        if (ops.isEmpty()) {
            throw new IllegalArgumentException("The property path '" + path + "' cannot be resolved.");
        }
        Object current = target;
        for (int i = 0; i < ops.size() - 1; i++) {
            current = read(current, ops.get(i), path);
        }
        write(current, ops.get(ops.size() - 1), path, value);
    }

    private static Object read(Object target, Op op, String path) {
        if (target == null) {
            throw new IllegalArgumentException("The property path '" + path + "' cannot be resolved: an intermediate value is null.");
        }
        if (op instanceof PropertyOp property) {
            return readProperty(target, property.name(), path);
        }
        if (op instanceof IndexOp index) {
            return readIndex(target, index.index(), path);
        }
        if (op instanceof MapKeyOp mapKey) {
            return readMapKey(target, mapKey.key(), path);
        }
        throw new IllegalStateException("Unknown operation: " + op);
    }

    private static void write(Object target, Op op, String path, Object value) {
        if (target == null) {
            throw new IllegalArgumentException("The property path '" + path + "' cannot be resolved: an intermediate value is null.");
        }
        if (op instanceof PropertyOp property) {
            writeProperty(target, property.name(), path, value);
            return;
        }
        if (op instanceof IndexOp index) {
            writeIndex(target, index.index(), path, value);
            return;
        }
        if (op instanceof MapKeyOp mapKey) {
            writeMapKey(target, mapKey.key(), path, value);
            return;
        }
        throw new IllegalStateException("Unknown operation: " + op);
    }

    private static Object readProperty(Object target, String name, String path) {
        Method getter = descriptor(target.getClass(), name).getReadMethod();
        if (getter == null) {
            throw new IllegalArgumentException("The property path '" + path + "' cannot be resolved: no getter for '" + name + "'.");
        }
        try {
            return getter.invoke(target);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("The property path '" + path + "' cannot be resolved.", e);
        }
    }

    private static void writeProperty(Object target, String name, String path, Object value) {
        Method setter = descriptor(target.getClass(), name).getWriteMethod();
        if (setter == null) {
            throw new IllegalArgumentException("The property path '" + path + "' cannot be resolved: no setter for '" + name + "'.");
        }
        try {
            setter.invoke(target, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("The property path '" + path + "' cannot be resolved.", e);
        }
    }

    private static Object readIndex(Object target, int index, String path) {
        if (target.getClass().isArray()) {
            try {
                return Array.get(target, index);
            } catch (IndexOutOfBoundsException e) {
                throw new IllegalArgumentException("The property path '" + path + "' cannot be resolved: index " + index + " is out of bounds.", e);
            }
        }
        if (target instanceof List<?> list) {
            try {
                return list.get(index);
            } catch (IndexOutOfBoundsException e) {
                throw new IllegalArgumentException("The property path '" + path + "' cannot be resolved: index " + index + " is out of bounds.", e);
            }
        }
        throw new IllegalArgumentException("The property path '" + path + "' cannot be resolved: not an array or list.");
    }

    private static void writeIndex(Object target, int index, String path, Object value) {
        if (target.getClass().isArray()) {
            try {
                Array.set(target, index, value);
                return;
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException("The property path '" + path + "' cannot be resolved: index " + index + " is out of bounds.", e);
            }
        }
        if (target instanceof List<?>) {
            try {
                ((List<Object>) target).set(index, value);
                return;
            } catch (IndexOutOfBoundsException e) {
                throw new IllegalArgumentException("The property path '" + path + "' cannot be resolved: index " + index + " is out of bounds.", e);
            }
        }
        throw new IllegalArgumentException("The property path '" + path + "' cannot be resolved: not an array or list.");
    }

    private static Object readMapKey(Object target, String key, String path) {
        if (target instanceof Map<?, ?> map) {
            return map.get(key);
        }
        throw new IllegalArgumentException("The property path '" + path + "' cannot be resolved: not a map.");
    }

    private static void writeMapKey(Object target, String key, String path, Object value) {
        if (target instanceof Map<?, ?>) {
            ((Map<Object, Object>) target).put(key, value);
            return;
        }
        throw new IllegalArgumentException("The property path '" + path + "' cannot be resolved: not a map.");
    }

    private static PropertyDescriptor descriptor(Class<?> type, String name) {
        Map<String, PropertyDescriptor> descriptors = DESCRIPTOR_CACHE.computeIfAbsent(type, PropertyAccess::introspect);
        PropertyDescriptor descriptor = descriptors.get(name);
        if (descriptor == null) {
            throw new IllegalArgumentException("No such property '" + name + "' on " + type.getSimpleName() + ".");
        }
        return descriptor;
    }

    private static Map<String, PropertyDescriptor> introspect(Class<?> type) {
        try {
            PropertyDescriptor[] descriptors = Introspector.getBeanInfo(type, Object.class).getPropertyDescriptors();
            Map<String, PropertyDescriptor> byName = new HashMap<>();
            for (PropertyDescriptor descriptor : descriptors) {
                byName.put(descriptor.getName(), descriptor);
            }
            return byName;
        } catch (IntrospectionException e) {
            throw new IllegalStateException("Unable to introspect " + type.getSimpleName() + ".", e);
        }
    }

    /**
     * Parses a property path into a list of operations. Paths use dot-separated segments, with
     * optional {@code [index]} array/list access and {@code (key)} Map access, e.g.
     * {@code child.array[2]}, {@code list[0].text}, {@code meta(color)}.
     */
    private static List<Op> parse(String path) {
        List<Op> ops = new ArrayList<>();
        int i = 0;
        int n = path.length();
        while (i < n) {
            while (i < n && path.charAt(i) == '.') {
                i++;
            }
            if (i >= n) {
                break;
            }
            int start = i;
            while (i < n && isIdentifierChar(path.charAt(i))) {
                i++;
            }
            if (i == start) {
                throw new IllegalArgumentException("Invalid property path '" + path + "'.");
            }
            ops.add(new PropertyOp(path.substring(start, i)));
            while (i < n) {
                char c = path.charAt(i);
                if (c == '[') {
                    int close = path.indexOf(']', i);
                    if (close < 0) {
                        throw new IllegalArgumentException("Invalid property path '" + path + "': unterminated '['.");
                    }
                    int index;
                    try {
                        index = Integer.parseInt(path.substring(i + 1, close));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid property path '" + path + "': expected a numeric index.", e);
                    }
                    ops.add(new IndexOp(index));
                    i = close + 1;
                } else if (c == '(') {
                    int close = path.indexOf(')', i);
                    if (close < 0) {
                        throw new IllegalArgumentException("Invalid property path '" + path + "': unterminated '('.");
                    }
                    ops.add(new MapKeyOp(path.substring(i + 1, close)));
                    i = close + 1;
                } else {
                    break;
                }
            }
        }
        return ops;
    }

    private static boolean isIdentifierChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '$';
    }
}
