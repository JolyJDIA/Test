package jolyjdia.test.util.serial;

import jolyjdia.test.PacketMoney;
import jolyjdia.test.PacketVk;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ObjectSerializer {
    private static final Map<Type, Handler<?>> TYPE_PRODUCERS = new HashMap<>();

    static {
        TYPE_PRODUCERS.put(Integer.TYPE, Typer.INTEGER);
        TYPE_PRODUCERS.put(int[].class, Typer.INTS);
        TYPE_PRODUCERS.put(Integer.class, Typer.INTEGER);
        TYPE_PRODUCERS.put(Boolean.TYPE, Typer.BOOLEAN);
        TYPE_PRODUCERS.put(boolean[].class, Typer.BOOLEANS);
        TYPE_PRODUCERS.put(Boolean.class, Typer.BOOLEAN);
        TYPE_PRODUCERS.put(Double.TYPE, Typer.DOUBLE);
        TYPE_PRODUCERS.put(double[].class, Typer.DOUBLES);
        TYPE_PRODUCERS.put(Double.class, Typer.DOUBLE);
        TYPE_PRODUCERS.put(Float.TYPE, Typer.FLOAT);
        TYPE_PRODUCERS.put(float[].class, Typer.FLOATS);
        TYPE_PRODUCERS.put(Float.class, Typer.FLOAT);
        TYPE_PRODUCERS.put(Character.TYPE, Typer.CHAR);
        TYPE_PRODUCERS.put(char[].class, Typer.CHARS);
        TYPE_PRODUCERS.put(Character.class, Typer.CHAR);
        TYPE_PRODUCERS.put(Long.TYPE, Typer.LONG);
        TYPE_PRODUCERS.put(long[].class, Typer.LONGS);
        TYPE_PRODUCERS.put(Long.class, Typer.LONG);
        TYPE_PRODUCERS.put(Short.TYPE, Typer.SHORT);
        TYPE_PRODUCERS.put(short[].class, Typer.SHORTS);
        TYPE_PRODUCERS.put(Short.class, Typer.SHORT);
        TYPE_PRODUCERS.put(Byte.TYPE, Typer.BYTE);
        TYPE_PRODUCERS.put(byte[].class, Typer.BYTES);
        TYPE_PRODUCERS.put(Byte.class, Typer.BYTE);
        TYPE_PRODUCERS.put(UUID.class, Typer.UUID);
        TYPE_PRODUCERS.put(String.class, Typer.STRING);
    }

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