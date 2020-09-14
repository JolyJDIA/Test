package jolyjdia.test;

import jolyjdia.test.util.serial.ObjectSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public final class Example {

    private static final MariaDBConnectionFactory DB = new MariaDBConnectionFactory(
            "root",
            "",
            "test",
            "localhost:3306");

    private Example() {}

    public static void main(String[] args) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ObjectSerializer.serialize(new short[]{99, 7, 256, 26, 5212, 42}, outputStream);

            try (ByteArrayInputStream input = new ByteArrayInputStream(outputStream.toByteArray())) {
                short[] array = ObjectSerializer.deserialize(input, short[].class);
                System.out.println(Arrays.toString(array));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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

