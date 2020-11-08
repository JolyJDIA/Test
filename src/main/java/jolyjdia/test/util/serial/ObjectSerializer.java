package jolyjdia.test.util.serial;

import jolyjdia.test.PacketMoney;
import jolyjdia.test.PacketVk;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

import static java.util.Map.entry;

public final class ObjectSerializer {
    private static final Map<Type, Handler<?>> TYPE_PRODUCERS = Map.ofEntries(
            entry(Integer.TYPE, Typer.INTEGER),
            entry(int[].class, Typer.INTS),
            entry(Integer.class, Typer.INTEGER),

            entry(Boolean.TYPE, Typer.BOOLEAN),
            entry(boolean[].class, Typer.BOOLEANS),
            entry(Boolean.class, Typer.BOOLEAN),

            entry(Double.TYPE, Typer.DOUBLE),
            entry(double[].class, Typer.DOUBLES),
            entry(Double.class, Typer.DOUBLE),

            entry(Float.TYPE, Typer.FLOAT),
            entry(float[].class, Typer.FLOATS),
            entry(Float.class, Typer.FLOAT),

            entry(Character.TYPE, Typer.CHAR),
            entry(char[].class, Typer.CHARS),
            entry(Character.class, Typer.CHAR),

            entry(Long.TYPE, Typer.LONG),
            entry(long[].class, Typer.LONGS),
            entry(Long.class, Typer.LONG),

            entry(Short.TYPE, Typer.SHORT),
            entry(short[].class, Typer.SHORTS),
            entry(Short.class, Typer.SHORT),

            entry(Byte.TYPE, Typer.BYTE),
            entry(byte[].class, Typer.BYTES),
            entry(Byte.class, Typer.BYTE),

            entry(UUID.class, Typer.UUID),
            entry(String.class, Typer.STRING)
    );

    private ObjectSerializer() {}
    private static final String UID = "packetId";

    @SuppressWarnings({"cast", "unchecked"})
    public static void serialize(Object o, ByteArrayOutputStream output) {
        Class<?> type = o.getClass();
        Handler<Object> typeField;
        if (type.isArray() && !type.getComponentType().isPrimitive()) {
            Object[] array = (Object[]) o;
            output.writeBytes(Typer.SHORT.write((short) array.length));
            for (Object obj : array) {
                System.out.println("asdasd "+obj);
                serialize(obj, output);
            }
        } else if ((typeField = (Handler<Object>) TYPE_PRODUCERS.get(type)) == null) {
            while (type.getSuperclass() != null) {
                for (Field field : type.getDeclaredFields()) {
                    //142 int mask = (Modifier.PRIVATE | Modifier.PROTECTED | Modifier.STATIC | Modifier.TRANSIENT);
                    if ((!field.getName().equals(UID)) && (field.getModifiers() & 142) != 0) {
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
    @SuppressWarnings({"cast", "unchecked"})
    public static <T> T deserialize(ByteArrayInputStream input, Class<T> aClass) {
        try {
            Handler<T> tProducer;
            Class<?> ccl;
            if(aClass.isArray() && !(ccl = aClass.getComponentType()).isPrimitive()) {
                Object[] array = (Object[])Array.newInstance(ccl, Typer.SHORT.read(input));
                //for (int i = 0, len = array.length; i < len; ++i) {
                 //   array[i] = deserialize(input, ccl);
               // }
                array[0] = deserialize(input, PacketMoney.class);
                array[1] = deserialize(input, PacketVk.class);
                return (T) array;
            } else if ((tProducer = (Handler<T>) TYPE_PRODUCERS.get(aClass)) == null) {
                T instance = aClass.getConstructor().newInstance();
                Class<?> currentClass = aClass;
                while (currentClass.getSuperclass() != null) {
                    for (Field field : currentClass.getDeclaredFields()) {
                        if (field.getName().equals(UID)) {
                            input.skipNBytes(4);
                            continue;
                            //158 int mask = (Modifier.PRIVATE | Modifier.PROTECTED | Modifier.STATIC | Modifier.FINAL | Modifier.TRANSIENT);
                        } else if ((field.getModifiers() & 158) != 0) {
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