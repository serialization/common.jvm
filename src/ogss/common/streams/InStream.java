/*  ___ _  ___ _ _                                                            *\
** / __| |/ (_) | |       The SKilL common JVM Implementation                 **
** \__ \ ' <| | | |__     (c) 2013-18 University of Stuttgart                 **
** |___/_|\_\_|_|____|    see LICENSE                                         **
\*                                                                            */
package ogss.common.streams;

import java.nio.ByteBuffer;

/**
 * Implementations of this class are used to turn a byte stream into a stream of
 * integers and floats.
 *
 * @author Timm Felden
 */
public abstract class InStream {
    protected final ByteBuffer input;

    protected InStream(ByteBuffer input) {
        this.input = input;
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

        return (0 != ((rval = i8()) & 0x80)) ? multiByteV32(rval) : rval;
    }

    /**
     * multi byte v64 values are treated in a different function to enable
     * inlining of more common single byte v64 values
     */
    @SuppressWarnings("all")
    final private int multiByteV32(int rval) {
        int r;
        rval = (rval & 0x7f) | (((r = i8()) & 0x7f) << 7);

        if (0 != (r & 0x80)) {
            rval |= ((r = i8()) & 0x7f) << 14;

            if (0 != (r & 0x80)) {
                rval |= ((r = i8()) & 0x7f) << 21;

                if (0 != (r & 0x80)) {
                    rval |= ((r = i8()) & 0x7f) << 28;

                    if (0 != (r & 0x80)) {
                        throw new IllegalStateException("unexpected overlong v64 value (expected 32bit)");
                    }
                }
            }
        }
        return rval;
    }

    /**
     * @return take an v64 from the stream
     */
    public final long v64() {
        long rval;

        return (0 != ((rval = i8()) & 0x80)) ? multiByteV64(rval) : rval;
    }

    /**
     * multi byte v64 values are treated in a different function to enable
     * inlining of more common single byte v64 values
     */
    @SuppressWarnings("all")
    final private long multiByteV64(long rval) {
        long r;
        rval = (rval & 0x7f) | (((r = i8()) & 0x7f) << 7);

        if (0 != (r & 0x80)) {
            rval |= ((r = i8()) & 0x7f) << 14;

            if (0 != (r & 0x80)) {
                rval |= ((r = i8()) & 0x7f) << 21;

                if (0 != (r & 0x80)) {
                    rval |= ((r = i8()) & 0x7f) << 28;

                    if (0 != (r & 0x80)) {
                        rval |= ((r = i8()) & 0x7f) << 35;

                        if (0 != (r & 0x80)) {
                            rval |= ((r = i8()) & 0x7f) << 42;

                            if (0 != (r & 0x80)) {
                                rval |= ((r = i8()) & 0x7f) << 49;

                                if (0 != (r & 0x80)) {
                                    rval |= (((long) i8()) << 56);
                                }
                            }
                        }
                    }
                }
            }
        }
        return rval;
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
     * @return take a bool from the stream
     */
    public final boolean bool() {
        return input.get() != 0;
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
