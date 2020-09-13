package jolyjdia.test.util.serial;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public interface Handler<T> {
    byte[] write(T t);

    T read(ByteArrayInputStream stream) throws IOException;
}
