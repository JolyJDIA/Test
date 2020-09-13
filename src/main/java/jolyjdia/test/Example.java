package jolyjdia.test;

import jolyjdia.test.util.serial.ObjectSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public final class Example {

    private static final MariaDBConnectionFactory DB = new MariaDBConnectionFactory(
            "root",
            "",
            "test",
            "localhost:3306");

    private Example() {}

    public static void main(String[] args) {
        System.out.println(Object[].class);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectSerializer.serialize(new int[]{99, 7, 256, 26, 5212, 42}, byteArrayOutputStream);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

        int[] array = ObjectSerializer.deserialize(byteArrayInputStream, int[].class);
        System.out.println(Arrays.toString(array));
    }
    public static class Obj {
        public int s = 9999;
        public final Integer[] i = {7, 7, 5};

        @Override
        public String toString() {
            return Arrays.toString(i) + ' ' +s;
        }
    }
    public static class Obj0 {
        public int s;
        public int[] i;

        @Override
        public String toString() {
            return Arrays.toString(i) + ' ' +s;
        }
    }
}

