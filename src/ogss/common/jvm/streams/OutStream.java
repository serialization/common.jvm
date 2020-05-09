/*  ___ _  ___ _ _                                                            *\
** / __| |/ (_) | |       The SKilL common JVM Implementation                 **
** \__ \ ' <| | | |__     (c) 2013-18 University of Stuttgart                 **
** |___/_|\_\_|_|____|    see LICENSE                                         **
\*                                                                            */
package ogss.common.jvm.streams;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Write abstraction of ByteBuffer operations to OGSS tokens.
 *
 * @author Timm Felden
 */
abstract public class OutStream {
    protected static final int BUFFER_SIZE = 4096;

    /**
     * Operations work on this buffer. If it's full refresh will be invoked.
     */
    protected ByteBuffer buffer;

    protected OutStream(ByteBuffer buffer) {
        this.buffer = buffer;
        buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Refresh the buffer.
     * 
     * @note do not call this method if the buffer is empty
     */
    protected abstract void refresh() throws IOException;

    final public void i8(byte data) throws IOException {
        if (buffer.remaining() < 1)
            refresh();
        buffer.put(data);
    }

    final public void i16(short data) throws IOException {
        if (buffer.remaining() < 2)
            refresh();
        buffer.putShort(data);
    }

    final public void i32(int data) throws IOException {
        if (buffer.remaining() < 4)
            refresh();
        buffer.putInt(data);
    }

    final public void i64(long data) throws IOException {
        if (buffer.remaining() < 8)
            refresh();
        buffer.putLong(data);
    }

    final public void v64(int v) throws IOException {
        if (buffer.remaining() < 5)
            refresh();
        if (0 == (v & 0xFFFFFF80)) {
            buffer.put((byte) v);
        } else {
            buffer.put((byte) (0x80 | v));
            if (0 == (v & 0xFFFFC000)) {
                buffer.put((byte) (v >> 7));
            } else {
                buffer.put((byte) (0x80 | v >> 7));
                if (0 == (v & 0xFFE00000)) {
                    buffer.put((byte) (v >> 14));
                } else {
                    buffer.put((byte) (0x80 | v >> 14));
                    if (0 == (v & 0xF0000000)) {
                        buffer.put((byte) (v >> 21));
                    } else {
                        buffer.put((byte) (0x80 | v >> 21));
                        buffer.put((byte) (v >> 28));
                    }
                }
            }
        }
    }

    final public void v64(long v) throws IOException {
        if (buffer.remaining() < 9)
            refresh();
        if (0L == (v & 0xFFFFFFFFFFFFFF80L)) {
            buffer.put((byte) v);
        } else {
            v64Medium(v);
        }
    }

    final private void v64Medium(long v) {
        buffer.put((byte) (0x80L | v));
        if (0L == (v & 0xFFFFFFFFFFFFC000L)) {
            buffer.put((byte) (v >> 7));
        } else {
            v64Large(v);
        }
    }

    final private void v64Large(long v) {
        buffer.put((byte) (0x80L | v >> 7));
        if (0L == (v & 0xFFFFFFFFFFE00000L)) {
            buffer.put((byte) (v >> 14));
        } else {
            buffer.put((byte) (0x80L | v >> 14));
            if (0L == (v & 0xFFFFFFFFF0000000L)) {
                buffer.put((byte) (v >> 21));
            } else {
                buffer.put((byte) (0x80L | v >> 21));
                if (0L == (v & 0xFFFFFFF800000000L)) {
                    buffer.put((byte) (v >> 28));
                } else {
                    buffer.put((byte) (0x80L | v >> 28));
                    if (0L == (v & 0xFFFFFC0000000000L)) {
                        buffer.put((byte) (v >> 35));
                    } else {
                        buffer.put((byte) (0x80L | v >> 35));
                        if (0L == (v & 0xFFFE000000000000L)) {
                            buffer.put((byte) (v >> 42));
                        } else {
                            buffer.put((byte) (0x80L | v >> 42));
                            if (0L == (v & 0xFF00000000000000L)) {
                                buffer.put((byte) (v >> 49));
                            } else {
                                buffer.put((byte) (0x80L | v >> 49));
                                buffer.put((byte) (v >> 56));
                            }
                        }
                    }
                }
            }
        }
    }

    final public void f32(float data) throws IOException {
        if (buffer.remaining() < 4)
            refresh();
        buffer.putFloat(data);
    }

    final public void f64(double data) throws IOException {
        if (buffer.remaining() < 8)
            refresh();
        buffer.putDouble(data);
    }

    /**
     * ensure that the stream is in the expected state
     * 
     * @note close streams before reusing them!
     */
    abstract public void close() throws IOException;
}
