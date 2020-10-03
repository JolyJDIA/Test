package jolyjdia.test;

import jolyjdia.test.util.serial.ObjectSerializer;
import jolyjdia.test.util.serial.Typer;

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

    public static void main(String[] args) throws IOException {
        execute(
                new Packet("money"),
                new Packet("vk"),
                new Packet("chat"),
                new Packet("exp"),
                new Packet("message")
        );

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        if(Typer.BOOLEAN.read(inputStream)) {
            Packet[] array = ObjectSerializer.deserialize(inputStream, Packet[].class);
            System.out.println(Arrays.toString(array));
        } else {
            Packet packet = ObjectSerializer.deserialize(inputStream, Packet.class);
            System.out.println(packet);
        }
    }
    static ByteArrayOutputStream outputStream;

    public static void execute(Object... packets) {
        if(packets.length == 0) {
            return;//void
        }
        outputStream = new ByteArrayOutputStream();
        boolean batch = packets.length != 1;
        outputStream.writeBytes(Typer.BOOLEAN.write(batch));
        ObjectSerializer.serialize(batch ? packets : packets[0], outputStream);
    }
}

