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
    }

    protected abstract void refresh() throws IOException;

    final public void bool(boolean data) throws IOException {
        if (null == buffer || buffer.remaining() < 1)
            refresh();
        buffer.put(data ? (byte) 0xFF : (byte) 0x00);
    }

    final public void i8(byte data) throws IOException {
        if (null == buffer || buffer.remaining() < 1)
            refresh();
        buffer.put(data);
    }

    final public void i16(short data) throws IOException {
        if (null == buffer || buffer.remaining() < 2)
            refresh();
        buffer.putShort(data);
    }

    final public void i32(int data) throws IOException {
        if (null == buffer || buffer.remaining() < 4)
            refresh();
        buffer.putInt(data);
    }

    final public void i64(long data) throws IOException {
        if (null == buffer || buffer.remaining() < 8)
            refresh();
        buffer.putLong(data);
    }

    abstract public void v64(long data) throws IOException;

    final public void f32(float data) throws IOException {
        if (null == buffer || buffer.remaining() < 4)
            refresh();
        buffer.putFloat(data);
    }

    final public void f64(double data) throws IOException {
        if (null == buffer || buffer.remaining() < 8)
            refresh();
        buffer.putDouble(data);
    }

    /**
     * close the out stream; this should not be necessary, but apparently, windows does not play by the rules
     * 
     * @throws IOException
     */
    abstract public void close() throws IOException;
}
