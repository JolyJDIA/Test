package jolyjdia.test.util.serial;

import com.google.common.collect.ImmutableMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

public final class ObjectSerializer {
    private static final Map<Type, Handler<?>> OBJECT_PRODUCER = ImmutableMap.<Type, Handler<?>>builder()
            .put(int.class, Typer.INTEGER).put(int[].class, Typer.INTS).put(Integer.class, Typer.INTEGER)
            .put(boolean.class, Typer.BOOLEAN).put(boolean[].class, Typer.BOOLEANS).put(Boolean.class, Typer.BOOLEAN)
            .put(double.class, Typer.DOUBLE).put(double[].class, Typer.DOUBLES).put(Double.class, Typer.DOUBLE)
            .put(float.class, Typer.FLOAT).put(float[].class, Typer.FLOATS).put(Float.class, Typer.FLOAT)
            .put(char.class, Typer.CHAR).put(char[].class, Typer.CHARS).put(Character.class, Typer.CHAR)
            .put(long.class, Typer.LONG).put(long[].class, Typer.LONGS).put(Long.class, Typer.LONG)
            .put(short.class, Typer.SHORT).put(short[].class, Typer.SHORTS).put(Short.class, Typer.SHORT)
            .put(byte.class, Typer.BYTE).put(byte[].class, Typer.BYTES).put(Byte.class, Typer.BYTE)
            .put(UUID.class, Typer.UUID).put(String.class, Typer.STRING)
            .build();

    private ObjectSerializer() {
    }

    public static void serialize(Object o, ByteArrayOutputStream output) {
        Class<?> type = o.getClass();
        Handler<Object> typeField;
        if (type.isArray() && !type.getComponentType().isPrimitive()) {
            Object[] array = (Object[]) o;
            output.writeBytes(Typer.SHORT.write((short) array.length));
            for (Object obj : array) {
                serialize(obj, output);
            }
        } else if ((typeField = (Handler<Object>) OBJECT_PRODUCER.get(type)) == null) {
            while (type.getSuperclass() != null) {
                for (Field field : type.getDeclaredFields()) {
                    //14 = (Modifier.PRIVATE | Modifier.PROTECTED | Modifier.STATIC)
                    if ((field.getModifiers() & 14) != 0) {
                        continue;
                    }
                    try {
                        Object fieldObj = field.get(o);
                        serialize(fieldObj, output);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Ошибка сериализации", e);
                    }
                }
                type = type.getSuperclass();
            }
        } else {
            output.writeBytes(typeField.write(o));
        }
    }

    public static <T> T deserialize(ByteArrayInputStream input, Class<T> aClass) {
        try {
            Handler<T> tProducer;
            Class<?> ccl;
            if(aClass.isArray() && !(ccl = aClass.getComponentType()).isPrimitive()) {
                Object[] array = (Object[])Array.newInstance(ccl, Typer.SHORT.read(input));
                for (int i = 0, len = array.length; i < len; ++i) {
                    array[i] = deserialize(input, ccl);
                }
                return (T) array;
            } else if ((tProducer = (Handler<T>) OBJECT_PRODUCER.get(aClass)) == null) {
                T instance = aClass.getConstructor().newInstance();
                Class<?> currentClass = aClass;
                while (currentClass.getSuperclass() != null) {
                    for (Field field : currentClass.getDeclaredFields()) {
                        //30 = (Modifier.PRIVATE | Modifier.PROTECTED | Modifier.STATIC | Modifier.FINAL)
                        if ((field.getModifiers() & 30) != 0) {
                            continue;
                        }
                        field.setAccessible(true);
                        field.set(instance, deserialize(input, field.getType()));
                    }
                    currentClass = currentClass.getSuperclass();
                }
                return instance;
            } else {
                return tProducer.read(input);
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка десериализации", e);
        }
    }
}