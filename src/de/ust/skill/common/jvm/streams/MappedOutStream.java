/*  ___ _  ___ _ _                                                            *\
 * / __| |/ (_) | |       Your SKilL Scala Binding                            *
 * \__ \ ' <| | | |__     generated: 19.11.2014                               *
 * |___/_|\_\_|_|____|    by: Timm Felden                                     *
\*                                                                            */
package de.ust.skill.common.jvm.streams;

import java.io.IOException;
import java.nio.MappedByteBuffer;

/**
 * Allows writing to memory mapped region.
 *
 * @author Timm Felden
 */
final public class MappedOutStream extends OutStream {

	protected MappedOutStream(MappedByteBuffer buffer) {
		super(buffer);
	}

	@Override
	protected void refresh() throws IOException {
		// do nothing; let the JIT remove this method and all related checks
	}

    @Override
    public final void v64(long v) throws IOException {
    	if (0L == (v & 0xFFFFFFFFFFFFFF80L)) {
    		buffer.put((byte) v);
    	} else if (0L == (v & 0xFFFFFFFFFFFFC000L)) {
    		buffer.put((byte) (0x80L | v));
    		buffer.put((byte) (v >> 7));
    	} else if (0L == (v & 0xFFFFFFFFFFE00000L)) {
    		buffer.put((byte) (0x80L | v));
    		buffer.put((byte) (0x80L | v >> 7));
    		buffer.put((byte) (v >> 14));
    	} else if (0L == (v & 0xFFFFFFFFF0000000L)) {
    		buffer.put((byte) (0x80L | v));
    		buffer.put((byte) (0x80L | v >> 7));
    		buffer.put((byte) (0x80L | v >> 14));
    		buffer.put((byte) (v >> 21));
    	} else if (0L == (v & 0xFFFFFFF800000000L)) {
    		buffer.put((byte) (0x80L | v));
    		buffer.put((byte) (0x80L | v >> 7));
    		buffer.put((byte) (0x80L | v >> 14));
    		buffer.put((byte) (0x80L | v >> 21));
    		buffer.put((byte) (v >> 28));
    	} else if (0L == (v & 0xFFFFFC0000000000L)) {
    		buffer.put((byte) (0x80L | v));
    		buffer.put((byte) (0x80L | v >> 7));
    		buffer.put((byte) (0x80L | v >> 14));
    		buffer.put((byte) (0x80L | v >> 21));
    		buffer.put((byte) (0x80L | v >> 28));
    		buffer.put((byte) (v >> 35));
    	} else if (0L == (v & 0xFFFE000000000000L)) {
    		buffer.put((byte) (0x80L | v));
    		buffer.put((byte) (0x80L | v >> 7));
    		buffer.put((byte) (0x80L | v >> 14));
    		buffer.put((byte) (0x80L | v >> 21));
    		buffer.put((byte) (0x80L | v >> 28));
    		buffer.put((byte) (0x80L | v >> 35));
    		buffer.put((byte) (v >> 42));
    	} else if (0L == (v & 0xFF00000000000000L)) {
    		buffer.put((byte) (0x80L | v));
    		buffer.put((byte) (0x80L | v >> 7));
    		buffer.put((byte) (0x80L | v >> 14));
    		buffer.put((byte) (0x80L | v >> 21));
    		buffer.put((byte) (0x80L | v >> 28));
    		buffer.put((byte) (0x80L | v >> 35));
    		buffer.put((byte) (0x80L | v >> 42));
    		buffer.put((byte) (v >> 49));
    	} else {
    		buffer.put((byte) (0x80L | v));
    		buffer.put((byte) (0x80L | v >> 7));
    		buffer.put((byte) (0x80L | v >> 14));
    		buffer.put((byte) (0x80L | v >> 21));
    		buffer.put((byte) (0x80L | v >> 28));
    		buffer.put((byte) (0x80L | v >> 35));
    		buffer.put((byte) (0x80L | v >> 42));
    		buffer.put((byte) (0x80L | v >> 49));
    		buffer.put((byte) (v >> 56));
    	}
    }
}
