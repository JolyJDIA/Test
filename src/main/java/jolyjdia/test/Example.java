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
            ObjectSerializer.serialize(new Obj(), outputStream);
            System.out.println(Arrays.toString(outputStream.toByteArray()));
            try (ByteArrayInputStream input = new ByteArrayInputStream(outputStream.toByteArray())) {
                Obj0 array = ObjectSerializer.deserialize(input, Obj0.class);
                System.out.println(array);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }//[0, 3, 0, 3, 1, 1, 1, 0, 3, 0, 0, 0, 0, 3, 1, 1, 1]
    public static class Obj {
        public boolean[][] array = {
                {true, true, true},
                {false, false, false},
                {true, true, true}
        };

        @Override
        public String toString() {
            return Arrays.deepToString(array);
        }
    }
    public static class Obj0 {
        public boolean[][] array;

        @Override
        public String toString() {
            return Arrays.deepToString(array);
        }
    }
}

