/*  ___ _  ___ _ _                                                            *\
** / __| |/ (_) | |       The SKilL common JVM Implementation                 **
** \__ \ ' <| | | |__     (c) 2013-18 University of Stuttgart                 **
** |___/_|\_\_|_|____|    see LICENSE                                         **
\*                                                                            */
package ogss.common.jvm.streams;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Implementations of this class are used to turn a byte stream into a stream of integers and floats.
 *
 * @author Timm Felden
 */
public abstract class InStream {
    protected final ByteBuffer input;

    protected InStream(ByteBuffer input) {
        this.input = input;
        input.order(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * @return take an f64 from the stream
     */
    final public double f64() {
        return input.getDouble();
    }

    /**
     * @return take an f32 from the stream
     */
    public final float f32() {
        return input.getFloat();
    }

    /**
     * @return take an v64 from the stream
     */
    public final int v32() {
        int rval;

        return ((rval = i8()) < 0) ? longV32(rval) : rval;
    }

    /**
     * multi byte v64 values are treated in a different function to enable inlining of more common single byte v64
     * values
     */
    @SuppressWarnings("all")
    final private int longV32(int v) {
        v = (v & 0x7f) | (i8() << 7);

        boolean next = v < 0;
        v &= 0x3fff;

        if (next) {
            v |= i8() << 14;
            next = v < 0;
            v &= 0x1fffff;

            if (next) {
                v |= i8() << 21;
                next = v < 0;
                v &= 0xfffffff;

                if (next) {
                    v |= (long) i8() << 28;
                    next = v < 0;

                    if (next) {
                        throw new IllegalStateException("unexpected overlong v64 value (expected 32bit)");
                    }
                }
            }
        }
        return v;
    }

    /**
     * @return take an v64 from the stream
     */
    public final long v64() {
        long rval;

        return ((rval = i8()) < 0) ? longV64(rval) : rval;
    }

    /**
     * multi byte v64 values are treated in a different function to enable inlining of more common single byte v64
     * values
     */
    @SuppressWarnings("all")
    final private long longV64(long v) {
        v = (v & 0x7fL) | (i8() << 7L);

        boolean next = v < 0L;
        v &= 0x3fffL;

        if (next) {
            v |= i8() << 14L;
            next = v < 0L;
            v &= 0x1fffffL;

            if (next) {
                v |= i8() << 21L;
                next = v < 0L;
                v &= 0xfffffffL;

                if (next) {
                    v |= (long) i8() << 28L;
                    next = v < 0L;
                    v &= 0x7ffffffffL;

                    if (next) {
                        v |= (long) i8() << 35L;
                        next = v < 0L;
                        v &= 0x3ffffffffffL;

                        if (next) {
                            v |= (long) i8() << 42L;
                            next = v < 0L;
                            v &= 0x1ffffffffffffL;

                            if (next) {
                                v |= (long) i8() << 49L;
                                next = v < 0L;
                                v &= 0xffffffffffffffL;

                                if (next) {
                                    v |= (long) i8() << 56L;
                                }
                            }
                        }
                    }
                }
            }
        }
        return v;
    }

    /**
     * @return take an i64 from the stream
     */
    public final long i64() {
        return input.getLong();
    }

    /**
     * @return take an i32 from the stream
     * @throws UnexpectedEOF
     *             if there is no i32 in the stream
     */
    public final int i32() {
        return input.getInt();
    }

    /**
     * @return take an i16 from the stream
     */
    public final short i16() {
        return input.getShort();
    }

    /**
     * @return take an i8 from the stream
     */
    public final byte i8() {
        return input.get();
    }

    /**
     * @param position
     *            of the first read; on -1, the current position will be used and the current position will move
     * @note this function is not thread-safe on input if position != -1
     * @return raw byte array taken from the stream at the required position
     */
    public final byte[] bytes(int position, int length) {
        final byte[] rval = new byte[length];
        if (-1 == position) {
            input.get(rval);
        } else {
            final int storedPosition = input.position();
            input.position(position);
            input.get(rval);
            input.position(storedPosition);
        }
        return rval;
    }

    /**
     * @return true iff there are at least n bytes left in the stream
     */
    public final boolean has(int n) {
        return input.limit() >= n + input.position();
    }

    /**
     * @return true iff at the end of file (or stream)
     */
    public final boolean eof() {
        return input.limit() == input.position();
    }

    final public int position() {
        return input.position();
    }
}
