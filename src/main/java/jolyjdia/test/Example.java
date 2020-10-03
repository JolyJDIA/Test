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
        /*long s = System.currentTimeMillis();
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(bytes)) {
            objectOutputStream.writeObject(new Obj0());

            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes.toByteArray());
                 ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
                Obj0 obj0 = (Obj0) objectInputStream.readObject();
                long e = System.currentTimeMillis() - s;
                System.out.println(e);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }*/
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ObjectSerializer.serialize(new Obj(), outputStream);
            System.out.println(Arrays.toString(outputStream.toByteArray()));
            try (ByteArrayInputStream input = new ByteArrayInputStream(outputStream.toByteArray())) {
                Test array = ObjectSerializer.deserialize(input, Obj0.class);
                System.out.println(array);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }//[0, 3, 0, 3, 1, 1, 1, 0, 3, 0, 0, 0, 0, 3, 1, 1, 1]
    public static class Obj implements Test {
        @SuppressWarnings("serial")
        public boolean i = true;
        public int[] array = new int[0];

        @Override
        public String toString() {
            return "Obj0{" +
                    "i=" + i +
                    ", array=" + Arrays.toString(array) +
                    '}';
        }
    }
    public interface Test {

    }
    public static class Obj0 implements Test {
        public boolean i = true;
        public int[] array = new int[0];


        @Override
        public String toString() {
            return "Obj0{" +
                    "i=" + i +
                    ", array=" + Arrays.toString(array) +
                    '}';
        }
    }
}

