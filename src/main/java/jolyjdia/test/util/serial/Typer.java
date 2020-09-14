package jolyjdia.test.util.serial;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class Typer {
    public static final Handler<Integer> INTEGER = new Handler<>() {
        @Override
        public byte[] write(Integer i) {
            return new byte[]{
                    (byte) (i >>> 24),
                    (byte) (i >>> 16),
                    (byte) (i >>>  8),
                    (byte) ((int)i  )
            };
        }

        @Override
        public Integer read(ByteArrayInputStream stream) throws IOException {
            return getInt(stream.readNBytes(4));
        }
    };
    //Ждем ВалХалу, а щас терпим
    public static final Handler<int[]> INTS = new Handler<>() {
        @Override
        public byte[] write(int[] array) {
            int utflen = array.length;
            if (utflen > 0 && utflen <= Short.MAX_VALUE) {
                //2 = short
                ByteBuffer byteBuffer = ByteBuffer
                        .wrap(new byte[(utflen << 2) + 2])
                        .putShort((short) utflen);
                for(int i : array) {
                    byteBuffer.putInt(i);
                }
                return byteBuffer.array();
            } else {
                return new byte[0];
            }
        }

        @Override
        public int[] read(ByteArrayInputStream stream) throws IOException {
            short length = getShort(stream.readNBytes(2));
            int[] ie = new int[length];
            for(int i = 0; i < length; ++i) {
                ie[i] = getInt(stream.readNBytes(4));
            }
            return ie;
        }
    };

    public static final Handler<Long> LONG = new Handler<>() {
        @Override
        public byte[] write(Long l) {
            return new byte[]{
                    (byte) (l >>> 56),
                    (byte) (l >>> 48),
                    (byte) (l >>> 40),
                    (byte) (l >>> 32),
                    (byte) (l >>> 24),
                    (byte) (l >>> 16),
                    (byte) (l >>>  8),
                    (byte) ((long)l )
            };
        }

        @Override
        public Long read(ByteArrayInputStream stream) throws IOException {
            return getLong(stream.readNBytes(8));
        }
    };

    public static final Handler<long[]> LONGS = new Handler<>() {
        @Override
        public byte[] write(long[] array) {
            int utflen = array.length;
            if (utflen > 0 && utflen <= Short.MAX_VALUE) {
                ByteBuffer byteBuffer = ByteBuffer
                        .wrap(new byte[(utflen << 3) + 2])
                        .putShort((short) utflen);
                for(long i : array) {
                    byteBuffer.putLong(i);
                }
                return byteBuffer.array();
            } else {
                return new byte[0];
            }
        }

        @Override
        public long[] read(ByteArrayInputStream stream) throws IOException {
            short length = getShort(stream.readNBytes(2));
            long[] ie = new long[length];
            for(int i = 0; i < length; ++i) {
                ie[i] = getLong(stream.readNBytes(8));
            }
            return ie;
        }
    };

    public static final Handler<Short> SHORT = new Handler<>() {
        @Override
        public byte[] write(Short o) {
            return new byte[]{
                    (byte) (o >>>  8),
                    (byte) ((short)o)
            };
        }

        @Override
        public Short read(ByteArrayInputStream stream) throws IOException {
            return getShort(stream.readNBytes(2));
        }
    };

    public static final Handler<short[]> SHORTS = new Handler<>() {
        @Override
        public byte[] write(short[] array) {
            int utflen = array.length;
            if (utflen > 0 && utflen <= Short.MAX_VALUE) {
                ByteBuffer byteBuffer = ByteBuffer
                        .wrap(new byte[(utflen << 1) + 2])
                        .putShort((short) utflen);
                for(short i : array) {
                    byteBuffer.putShort(i);
                }
                return byteBuffer.array();
            } else {
                return new byte[0];
            }
        }

        @Override
        public short[] read(ByteArrayInputStream stream) throws IOException {
            short length = getShort(stream.readNBytes(2));
            short[] ie = new short[length];
            for(int i = 0; i < length; ++i) {
                ie[i] = getShort(stream.readNBytes(2));
            }
            return ie;
        }
    };

    public static final Handler<Character> CHAR = new Handler<>() {
        @Override
        public byte[] write(Character c) {
            return new byte[]{
                    (byte) (c >>> 8),
                    (byte) ((char)c)
            };
        }

        @Override
        public Character read(ByteArrayInputStream stream) throws IOException {
            return getChar(stream.readNBytes(2));
        }
    };

    public static final Handler<char[]> CHARS = new Handler<>() {
        @Override
        public byte[] write(char[] array) {
            int utflen = array.length;
            if (utflen > 0 && utflen <= Short.MAX_VALUE) {
                ByteBuffer byteBuffer = ByteBuffer
                        .wrap(new byte[(utflen << 1) + 2])
                        .putShort((short) utflen);
                for(char i : array) {
                    byteBuffer.putChar(i);
                }
                return byteBuffer.array();
            } else {
                return new byte[0];
            }
        }

        @Override
        public char[] read(ByteArrayInputStream stream) throws IOException {
            short length = getShort(stream.readNBytes(2));
            char[] ie = new char[length];
            for(int i = 0; i < length; ++i) {
                ie[i] = getChar(stream.readNBytes(2));
            }
            return ie;
        }
    };

    public static final Handler<Double> DOUBLE = new Handler<>() {
        @Override
        public byte[] write(Double d) {
            return LONG.write(Double.doubleToLongBits(d));
        }

        @Override
        public Double read(ByteArrayInputStream stream) throws IOException {
            return getDouble(stream.readNBytes(8));
        }
    };

    public static final Handler<double[]> DOUBLES = new Handler<>() {
        @Override
        public byte[] write(double[] array) {
            int utflen = array.length;
            if (utflen > 0 && utflen <= Short.MAX_VALUE) {
                ByteBuffer byteBuffer = ByteBuffer
                        .wrap(new byte[(utflen << 3) + 2])
                        .putShort((short) utflen);
                for(double i : array) {
                    byteBuffer.putDouble(i);
                }
                return byteBuffer.array();
            } else {
                return new byte[0];
            }
        }

        @Override
        public double[] read(ByteArrayInputStream stream) throws IOException {
            short length = getShort(stream.readNBytes(2));
            double[] ie = new double[length];
            for(int i = 0; i < length; ++i) {
                ie[i] = getDouble(stream.readNBytes(8));
            }
            return ie;
        }
    };

    public static final Handler<Float> FLOAT = new Handler<>() {
        @Override
        public byte[] write(Float o) {
            return INTEGER.write(Float.floatToIntBits(o));
        }

        @Override
        public Float read(ByteArrayInputStream stream) throws IOException {
            return getFloat(stream.readNBytes(4));
        }
    };
    public static final Handler<float[]> FLOATS = new Handler<>() {
        @Override
        public byte[] write(float[] array) {
            int utflen = array.length;
            if (utflen > 0 && utflen <= Short.MAX_VALUE) {
                ByteBuffer byteBuffer = ByteBuffer
                        .wrap(new byte[(utflen << 2) + 2])
                        .putShort((short) utflen);
                for(float i : array) {
                    byteBuffer.putFloat(i);
                }
                return byteBuffer.array();
            } else {
                return new byte[0];
            }
        }

        @Override
        public float[] read(ByteArrayInputStream stream) throws IOException {
            short length = getShort(stream.readNBytes(2));
            float[] ie = new float[length];
            for(int i = 0; i < length; ++i) {
                ie[i] = getFloat(stream.readNBytes(4));
            }
            return ie;
        }
    };

    public static final Handler<Boolean> BOOLEAN = new Handler<>() {
        @Override
        public byte[] write(Boolean o) {
            return new byte[]{(byte) (o ? 1 : 0)};
        }

        @Override
        public Boolean read(ByteArrayInputStream stream) throws IOException {
            return stream.readNBytes(1)[0] != 0;
        }
    };
    public static final Handler<boolean[]> BOOLEANS = new Handler<>() {
        @Override
        public byte[] write(boolean[] array) {
            int utflen = array.length;
            if (utflen > 0 && utflen <= Short.MAX_VALUE) {
                ByteBuffer byteBuffer = ByteBuffer
                        .wrap(new byte[(utflen) + 2])
                        .putShort((short) utflen);
                for(boolean b : array) {
                    byteBuffer.put((byte) (b ? 1 : 0));
                }
                return byteBuffer.array();
            } else {
                return new byte[0];
            }
        }

        @Override
        public boolean[] read(ByteArrayInputStream stream) throws IOException {
            short length = getShort(stream.readNBytes(2));
            boolean[] ie = new boolean[length];
            for(int i = 0; i < length; ++i) {
                ie[i] = stream.readNBytes(1)[0] != 0;
            }
            return ie;
        }
    };

    public static final Handler<Byte> BYTE = new Handler<>() {
        @Override
        public byte[] write(Byte o) {
            return new byte[]{o};
        }

        @Override
        public Byte read(ByteArrayInputStream stream) throws IOException {
            return stream.readNBytes(1)[0];
        }
    };
    public static final Handler<byte[]> BYTES = new Handler<>() {
        @Override
        public byte[] write(byte[] array) {
            int utflen = array.length;
            if (utflen > 0 && utflen <= Short.MAX_VALUE) {
                return ByteBuffer.wrap(new byte[(utflen) + 2])
                        .putShort((short) utflen)
                        .put(array)
                        .array();
            } else {
                return new byte[0];
            }
        }

        @Override
        public byte[] read(ByteArrayInputStream stream) throws IOException {
            return stream.readNBytes(getShort(stream.readNBytes(2)));
        }
    };
    public static final Handler<java.util.UUID> UUID = new Handler<>() {
        @Override
        public byte[] write(UUID o) {
            return ByteBuffer.wrap(new byte[16])
                    .putLong(o.getMostSignificantBits())
                    .putLong(o.getLeastSignificantBits())
                    .array();
        }

        @Override
        public UUID read(ByteArrayInputStream stream) throws IOException {
            return new UUID(getLong(stream.readNBytes(8)), getLong(stream.readNBytes(8)));
        }
    };

    public static final Handler<String> STRING = new Handler<>() {
        @Override
        public byte[] write(String s) {
            byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
            int utflen = bytes.length;
            if (utflen > 0 && utflen <= Short.MAX_VALUE) {
                return ByteBuffer.wrap(new byte[utflen+2])
                        .putShort((short) utflen)
                        .put(bytes)
                        .array();
            } else {
                return write("NOT FOUND");
            }
        }

        @Override
        public String read(ByteArrayInputStream stream) throws IOException {
            short length = getShort(stream.readNBytes(2));
            return new String(stream.readNBytes(length));
        }
    };

    private Typer() {}

    static char getChar(byte[] b) {
        return (char) ((b[0] << 8) + (b[1] & 0xFF));
    }

    static short getShort(byte[] b) {
        return (short) ((b[0] << 8) + (b[1] & 0xFF));
    }

    static int getInt(byte[] b) {
        return ((b[0]) << 24) +
                ((b[1] & 0xFF) << 16) +
                ((b[2] & 0xFF) <<  8) +
                (b[3] & 0xFF);
    }

    static float getFloat(byte[] b) {
        return Float.intBitsToFloat(getInt(b));
    }

    static long getLong(byte[] b) {
        return (((long) b[0])   << 56) +
                ((b[1] & 0xFFL) << 48) +
                ((b[2] & 0xFFL) << 40) +
                ((b[3] & 0xFFL) << 32) +
                ((b[4] & 0xFFL) << 24) +
                ((b[5] & 0xFFL) << 16) +
                ((b[6] & 0xFFL) <<  8) +
                ((b[7] & 0xFFL)      );
    }

    static double getDouble(byte[] b) {
        return Double.longBitsToDouble(getLong(b));
    }
}