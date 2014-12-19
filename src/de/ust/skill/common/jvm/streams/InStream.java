/*  ___ _  ___ _ _                                                            *\
 * / __| |/ (_) | |       Your SKilL Scala Binding                            *
 * \__ \ ' <| | | |__     generated: 19.11.2014                               *
 * |___/_|\_\_|_|____|    by: Timm Felden                                     *
\*                                                                            */
package de.ust.skill.common.jvm.streams;

import java.nio.MappedByteBuffer;

/**
 * Implementations of this class are used to turn a byte stream into a stream of
 * integers and floats.
 *
 * @author Timm Felden
 */
public abstract class InStream {
    protected final MappedByteBuffer input;

    protected InStream(MappedByteBuffer input) {
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
    public final long v64() {
        long count = 0;
        long rval = 0;
        long r = i8();
        while (count < 8 && 0 != (r & 0x80)) {
            rval |= (r & 0x7f) << (7 * count);

            count += 1;
            r = i8();
        }
        rval = (rval | (8 == count ? r : (r & 0x7f)) << (7 * count));
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
	 * @return raw byte array taken from the stream
	 */
    public final byte[] bytes(long length) {
        final byte[] rval = new byte[(int) length];
        input.get(rval);
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

	/**
	 * use with care!
	 * 
	 * @param position
	 *            jump to target position, without the ability to restore the
	 *            old position
	 */
	public abstract void jump(long position);

    final public long position() {
        return input.position();
    }
}
