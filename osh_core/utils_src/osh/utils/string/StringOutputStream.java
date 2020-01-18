package osh.utils.string;

import java.io.OutputStream;

/**
 * @author Ingo Mauser
 */
public class StringOutputStream extends OutputStream {

    final StringBuilder mBuf = new StringBuilder();

    public void write(int bytes) {
        this.mBuf.append((char) bytes);
    }

    public String getString() {
        return this.mBuf.toString();
    }
}
