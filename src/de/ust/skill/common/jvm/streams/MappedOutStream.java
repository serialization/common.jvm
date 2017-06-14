/*  ___ _  ___ _ _                                                            *\
 * / __| |/ (_) | |       Your SKilL Scala Binding                            *
 * \__ \ ' <| | | |__     generated: 19.11.2014                               *
 * |___/_|\_\_|_|____|    by: Timm Felden                                     *
\*                                                                            */
package de.ust.skill.common.jvm.streams;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;

/**
 * Allows writing to memory mapped region.
 *
 * @author Timm Felden
 */
final public class MappedOutStream extends OutStream {

    protected MappedOutStream(ByteBuffer buffer) {
        super(buffer);
    }

    /**
     * @return reveals the internal buffer
     */
    public ByteBuffer buffer() {
        return buffer;
    }

    /**
     * creates a copy of this in the argument range
     */
    public MappedOutStream clone(int begin, int end) {
        ByteBuffer b = buffer.duplicate();
        b.position(begin);
        b.limit(end);
        return new MappedOutStream(b);
    }

    @Override
    protected void refresh() throws IOException {
        // do nothing; let the JIT remove this method and all related checks
    }

    public final void v64(int v) throws IOException {
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

    @Override
    public final void v64(long v) throws IOException {
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

    @Override
    public void close() {
        ((MappedByteBuffer) buffer).force();
        buffer = null;
    }
}
