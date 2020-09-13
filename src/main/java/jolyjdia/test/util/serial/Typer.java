package jolyjdia.test.util.serial;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class Typer {

    public static final Handler<Integer> INTEGER = new Handler<>() {
        @Override
        public byte[] write(Integer o) {
            return ByteBuffer.wrap(new byte[4]).putInt(o).array();
        }

        @Override
        public @NotNull Integer read(@NotNull ByteArrayInputStream stream) throws IOException {
            return ByteBuffer.wrap(stream.readNBytes(4)).getInt();
        }
    };
    public static final Handler<Long> LONG = new Handler<>() {
        @Override
        public byte[] write(Long o) {
            return ByteBuffer.wrap(new byte[8]).putLong(o).array();
        }

        @Override
        public @NotNull Long read(@NotNull ByteArrayInputStream stream) throws IOException {
            return ByteBuffer.wrap(stream.readNBytes(8)).getLong();
        }
    };
    public static final Handler<Short> SHORT = new Handler<>() {
        @Override
        public byte[] write(Short o) {
            return ByteBuffer.wrap(new byte[2]).putShort(o).array();
        }

        @Override
        public @NotNull Short read(@NotNull ByteArrayInputStream stream) throws IOException {
            return ByteBuffer.wrap(stream.readNBytes(2)).getShort();
        }
    };
    public static final Handler<Character> CHAR = new Handler<>() {
        @Override
        public byte[] write(Character o) {
            return ByteBuffer.wrap(new byte[2]).putChar(o).array();
        }

        @Override
        public @NotNull Character read(@NotNull ByteArrayInputStream stream) throws IOException {
            return ByteBuffer.wrap(stream.readNBytes(2)).getChar();
        }
    };
    public static final Handler<Double> DOUBLE = new Handler<>() {
        @Override
        public byte[] write(Double o) {
            return ByteBuffer.wrap(new byte[8]).putDouble(o).array();
        }

        @Override
        public @NotNull Double read(@NotNull ByteArrayInputStream stream) throws IOException {
            return ByteBuffer.wrap(stream.readNBytes(8)).getDouble();
        }
    };
    public static final Handler<Float> FLOAT = new Handler<>() {
        @Override
        public byte[] write(Float o) {
            return ByteBuffer.wrap(new byte[4]).putFloat(o).array();
        }

        @Override
        public @NotNull Float read(@NotNull ByteArrayInputStream stream) throws IOException {
            return ByteBuffer.wrap(stream.readNBytes(4)).getFloat();
        }
    };
    public static final Handler<Boolean> BOOLEAN = new Handler<>() {
        @Override
        public byte[] write(Boolean o) {
            return ByteBuffer.wrap(new byte[1]).put((byte) (o ? 1 : 0)).array();
        }

        @Override
        public @NotNull Boolean read(@NotNull ByteArrayInputStream stream) throws IOException {
            return ByteBuffer.wrap(stream.readNBytes(1)).get() == 1;
        }
    };
    public static final Handler<Byte> BYTE = new Handler<>() {
        @Override
        public byte[] write(Byte o) {
            return ByteBuffer.wrap(new byte[1]).put(o).array();
        }

        @Override
        public @NotNull Byte read(@NotNull ByteArrayInputStream stream) throws IOException {
            return ByteBuffer.wrap(stream.readNBytes(1)).get();
        }
    };
    public static final Handler<java.util.UUID> UUID = new Handler<>() {
        @Override
        public byte[] write(@NotNull java.util.UUID o) {
            return ByteBuffer.wrap(new byte[16])
                    .putLong(o.getMostSignificantBits())
                    .putLong(o.getLeastSignificantBits())
                    .array();
        }

        @Override
        public @NotNull UUID read(@NotNull ByteArrayInputStream stream) throws IOException {
            return new UUID(
                    ByteBuffer.wrap(stream.readNBytes(8)).getLong(),
                    ByteBuffer.wrap(stream.readNBytes(8)).getLong()
            );
        }
    };
    public static final Handler<String> STRING = new Handler<>() {
        @Override
        public byte[] write(@NotNull String o) {
            int utflen = o.length();

            if (utflen > 0 && utflen <= Short.MAX_VALUE) {
                return ByteBuffer.wrap(new byte[utflen+2])
                        .putShort((short) utflen)
                        .put(o.getBytes(StandardCharsets.UTF_8))
                        .array();
            } else {
                return write("NOT FOUND");
            }
        }

        @Override
        public @NotNull String read(@NotNull ByteArrayInputStream stream) throws IOException {
            short length = ByteBuffer.wrap(stream.readNBytes(2)).getShort();
            return new String(stream.readNBytes(length));
        }
    };

    //Ждем ВалХалу, а щас терпим
    public static final Handler<int[]> INTS = new Handler<>() {
        @Override
        public byte[] write(@NotNull int[] o) {
            int utflen = o.length;

            if (utflen > 0 && utflen <= Short.MAX_VALUE) {
                //2 = short
                ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[(utflen << 2) +2]).putShort((short) utflen);
                for(int i : o) {
                    byteBuffer.putInt(i);
                }
                return byteBuffer.array();
            } else {
                return write(new int[0]);
            }
        }

        @Override
        public @NotNull int[] read(@NotNull ByteArrayInputStream stream) throws IOException {
            short length = ByteBuffer.wrap(stream.readNBytes(2)).getShort();
            int[] ie = new int[length];
            for(int i = 0; i < length; ++i) {
                ie[i] = ByteBuffer.wrap(stream.readNBytes(4)).getInt();
            }
            return ie;
        }
    };

    private Typer() {}
}
