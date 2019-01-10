/*  ___ _  ___ _ _                                                            *\
** / __| |/ (_) | |       The SKilL common JVM Implementation                 **
** \__ \ ' <| | | |__     (c) 2013-18 University of Stuttgart                 **
** |___/_|\_\_|_|____|    see LICENSE                                         **
\*                                                                            */
package ogss.common.streams;

import java.nio.ByteBuffer;

/**
 * This stream is used to parse a mapped region of field data.
 * 
 * @note The stream is called mapped for historical reasons. In fact we map the whole file and duplicate the buffer.
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
    public String toString() {
        return String.format("MappedInStream(0x%X -> 0x%X, next: 0x%X)", input.position(), input.limit(),
                input.get(input.position()));
    }
}
