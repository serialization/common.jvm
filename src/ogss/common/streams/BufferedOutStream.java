/*  ___ _  ___ _ _                                                            *\
** / __| |/ (_) | |       The SKilL common JVM Implementation                 **
** \__ \ ' <| | | |__     (c) 2013-18 University of Stuttgart                 **
** |___/_|\_\_|_|____|    see LICENSE                                         **
\*                                                                            */
package ogss.common.streams;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayDeque;

/**
 * The write operations will be buffered in memory until they are written by the file output stream.
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
     * Put an array of bytes into the stream. Intended to be used for string images.
     * 
     * @note you may not modify data after putting it to a stream, because the actual put might be a deferred operation
     * @note in Java, the returned byte[] is not owned by the string, hence it can be reused later on as buffer
     * @param data
     *            the data to be written
     */
    public void put(byte[] data) throws IOException {
        // write the byte[] directly, if it is too large to be cached
        // efficiently
        if (data.length > BUFFER_SIZE) {
            refresh();
            ByteBuffer tmp = ByteBuffer.wrap(data);
            tmp.position(tmp.limit());
            complete.addLast(tmp);
        } else {
            if (buffer.remaining() < data.length)
                refresh();
            buffer.put(data);
        }
    }

    /**
     * Recycle this buffer. Invalidates its content, but keeps all memory.
     */
    public void recycle() {
        off = 0;
        cur = 0;
        empty.addAll(complete);
        complete.clear();
        if (empty.isEmpty()) {
            // note: empty should not be empty now, because a buffered out stream owns
            // always at least one ByteBuffer
            // it is, however, possible that the buffer was recycled due to a crash or discard in which case the
            // current buffer would be set already (i.e. no buffer ever completed)
        } else {
            buffer = empty.pop();
        }
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
        if (0 != off) {
            off = 0;
            i8(cur);
            cur = 0;
        }
    }
}
