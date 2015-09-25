/*  ___ _  ___ _ _                                                            *\
 * / __| |/ (_) | |       Your SKilL Scala Binding                            *
 * \__ \ ' <| | | |__     generated: 19.11.2014                               *
 * |___/_|\_\_|_|____|    by: Timm Felden                                     *
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

    public ByteBuffer asByteBuffer() {
        return input;
    }

	@Override
	public void jump(long position) {
        throw new IllegalStateException("there is no sane reason to jump in a mapped stream");
	}
}
