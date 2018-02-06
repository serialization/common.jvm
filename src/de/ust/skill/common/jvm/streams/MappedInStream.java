/*  ___ _  ___ _ _                                                            *\
** / __| |/ (_) | |       The SKilL common JVM Implementation                 **
** \__ \ ' <| | | |__     (c) 2013-18 University of Stuttgart                 **
** |___/_|\_\_|_|____|    see LICENSE                                         **
\*                                                                            */
package de.ust.skill.common.jvm.streams;

import java.nio.ByteBuffer;

/**
 * This stream is used to parse a mapped region of field data.
 * 
 * @author Timm Felden
 */
public class MappedInStream extends InStream {

    public MappedInStream(ByteBuffer input) {
        super(input);
    }

    /**
     * creates a view onto this buffer that will not affect the buffer itself
     * 
     * @param begin
     *            relative beginning of the mapped stream
     * @param end
     *            relative ending of the mapped stream
     * @return a new mapped in stream, that can read from begin to end
     */
    public MappedInStream view(int begin, int end) {
        ByteBuffer r = input.duplicate();
        int pos = input.position();
        r.position(pos + begin);
        r.limit(pos + end);
        return new MappedInStream(r);
    }

    public ByteBuffer asByteBuffer() {
        return input;
    }

    @Override
    public void jump(long position) {
        throw new IllegalStateException("there is no sane reason to jump in a mapped stream");
    }

    @Override
    public String toString() {
        return String.format("MappedInStream(0x%X -> 0x%X, next: 0x%X)", input.position(), input.limit(),
                input.get(input.position()));
    }
}
