/*  ___ _  ___ _ _                                                            *\
 * / __| |/ (_) | |       Your SKilL Scala Binding                            *
 * \__ \ ' <| | | |__     generated: 19.11.2014                               *
 * |___/_|\_\_|_|____|    by: Timm Felden                                     *
\*                                                                            */
package de.ust.skill.common.jvm.streams;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Encapsulation of memory mapped files that allows to use skill types and mmap efficiently.
 *
 * @author Timm Felden
 */
abstract public class OutStream {

    // the buffer size is only used for mapping headers, which are usually
    // smaller
    protected final int BUFFERSIZE;
    protected ByteBuffer buffer;

    // current write position
    protected long position = 0L;

    public long position() {
        if (null == buffer)
            return position;

        return buffer.position() + position;
    }

    protected OutStream(ByteBuffer buffer) {
        this.buffer = buffer;
        BUFFERSIZE = buffer.capacity();
    }

    protected abstract void refresh() throws IOException;

    final public void bool(boolean data) throws IOException {
        if (null == buffer || buffer.position() == BUFFERSIZE)
            refresh();
        buffer.put(data ? (byte) 0xFF : (byte) 0x00);
    }

    final public void i8(byte data) throws IOException {
        if (null == buffer || buffer.position() == BUFFERSIZE)
            refresh();
        buffer.put(data);
    }

    final public void i16(short data) throws IOException {
        if (null == buffer || buffer.position() + 2 > BUFFERSIZE)
            refresh();
        buffer.putShort(data);
    }

    final public void i32(int data) throws IOException {
        if (null == buffer || buffer.position() + 4 > BUFFERSIZE)
            refresh();
        buffer.putInt(data);
    }

    final public void i64(long data) throws IOException {
        if (null == buffer || buffer.position() + 8 > BUFFERSIZE)
            refresh();
        buffer.putLong(data);
    }

    abstract public void v64(long data) throws IOException;

    final public void f32(float data) throws IOException {
        if (null == buffer || buffer.position() + 4 > BUFFERSIZE)
            refresh();
        buffer.putFloat(data);
    }

    final public void f64(double data) throws IOException {
        if (null == buffer || buffer.position() + 8 > BUFFERSIZE)
            refresh();
        buffer.putDouble(data);
    }
}
