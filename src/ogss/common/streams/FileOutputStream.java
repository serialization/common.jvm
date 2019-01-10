/*  ___ _  ___ _ _                                                            *\
** / __| |/ (_) | |       The SKilL common JVM Implementation                 **
** \__ \ ' <| | | |__     (c) 2013-18 University of Stuttgart                 **
** |___/_|\_\_|_|____|    see LICENSE                                         **
\*                                                                            */
package ogss.common.streams;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

/**
 * FileChannel based output stream.
 *
 * @author Timm Felden
 */
final public class FileOutputStream extends OutStream {

    private final FileChannel file;

    public FileOutputStream(FileInputStream target) throws IOException {
        super(ByteBuffer.allocate(BUFFER_SIZE));
        FileChannel f = target.file();
        // can happen after multiple flush operations
        if (!f.isOpen()) {
            f = (FileChannel) Files.newByteChannel(target.path(), StandardOpenOption.CREATE, StandardOpenOption.READ,
                    StandardOpenOption.WRITE);
        }
        f.position(0);
        this.file = f;
    }

    @Override
    protected void refresh() throws IOException {
        final int p;
        if (0 != (p = buffer.position())) {
            buffer.limit(p);
            buffer.position(0);
            file.write(buffer);
            buffer.position(0);
            buffer.limit(buffer.capacity());
        }
    }

    /**
     * Put an array of bytes into the stream. Intended to be used for string images.
     * 
     * @note you may not modify data after putting it to a stream, because the actual put might be a deferred operation
     * @param data
     *            the data to be written
     */
    public void put(byte[] data) throws IOException {
        // write the byte[] directly, if it is too large to be cached
        // efficiently
        if (data.length > BUFFER_SIZE / 4) {
            refresh();
            file.write(ByteBuffer.wrap(data));
        } else {
            if (buffer.remaining() < data.length)
                refresh();
            buffer.put(data);
        }
    }

    /**
     * Write a BufferdOutStream to disk.
     * 
     * @note out can be reused after calling this function, via out.recycle. It is invalid until then.
     * @param out
     *            the data to be written
     */
    public void write(BufferedOutStream out) throws IOException {
        refresh();

        // move out.buffer to completed
        ByteBuffer last = out.buffer;
        out.buffer = null;
        last.limit(last.position());
        last.position(0);
        out.complete.addLast(last);

        // write completed buffers
        for (ByteBuffer data : out.complete)
            file.write(data);
    }

    /**
     * Write a BufferdOutStream to disk prepending it with its size in bytes.
     * 
     * @note it is silently assumed, that the buffer of file output stream is unused
     * @param out
     *            the data to be written
     */
    public void writeSized(BufferedOutStream out) throws IOException {
        // move out.buffer to completed
        ByteBuffer last = out.buffer;
        out.buffer = null;
        last.limit(last.position());
        last.position(0);
        out.complete.addLast(last);

        // calculate and write size
        int size = 0;
        for (ByteBuffer data : out.complete)
            size += data.limit();

        buffer.position(0);
        buffer.limit(BUFFER_SIZE);
        v64(size);
        buffer.limit(buffer.position());
        buffer.position(0);
        file.write(buffer);

        // write completed buffers
        for (ByteBuffer data : out.complete)
            file.write(data);
    }

    /**
     * signal the underlying file channel to close
     * 
     * @note it is silently assumed, that the last operations were a sequence of write writeSized*
     */
    public void close() throws IOException {
        if (file.size() != file.position()) {
            file.truncate(file.position());
        }
        file.force(false);
        file.close();
    }
}
