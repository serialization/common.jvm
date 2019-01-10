/*  ___ _  ___ _ _                                                            *\
** / __| |/ (_) | |       The SKilL common JVM Implementation                 **
** \__ \ ' <| | | |__     (c) 2013-18 University of Stuttgart                 **
** |___/_|\_\_|_|____|    see LICENSE                                         **
\*                                                                            */
package ogss.common.streams;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 * The write operations will be buffered in memory until they are written by the
 * file output stream.
 *
 * @author Timm Felden
 */
final public class BufferedOutStream extends OutStream {

    public BufferedOutStream() {
        super(ByteBuffer.allocate(BUFFER_SIZE));
        buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * full buffers waiting for write
     */
    final ArrayDeque<ByteBuffer> complete = new ArrayDeque<>();

    /**
     * recycled buffers
     */
    private final ArrayDeque<ByteBuffer> empty = new ArrayDeque<>();

    /**
     * Recycle this buffer. Invalidates its content, but keeps all memory.
     */
    public void recycle() {
        empty.addAll(complete);
        complete.clear();
        // note: empty cannot be empty now, because a buffered out stream owns
        // always at least one ByteBuffer
        buffer = empty.pop();
        buffer.position(0);
        buffer.limit(buffer.capacity());
    }

    @Override
    protected void refresh() throws IOException {
        complete.addLast(buffer);
        // create a new buffer
        if (empty.isEmpty()) {
            buffer = ByteBuffer.allocate(BUFFER_SIZE);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
        } else {
            buffer = empty.pop();
            buffer.position(0);
            buffer.limit(buffer.capacity());
        }
    }
}
