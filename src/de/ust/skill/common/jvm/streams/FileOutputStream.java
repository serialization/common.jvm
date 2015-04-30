/*  ___ _  ___ _ _                                                            *\
 * / __| |/ (_) | |       Your SKilL Scala Binding                            *
 * \__ \ ' <| | | |__     generated: 19.11.2014                               *
 * |___/_|\_\_|_|____|    by: Timm Felden                                     *
\*                                                                            */
package de.ust.skill.common.jvm.streams;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * BufferedOutputStream based output stream.
 *
 * @author Timm Felden
 */
final public class FileOutputStream extends OutStream {

    private final FileChannel file;

    private FileOutputStream(FileChannel file) {
        // the size is smaller then 4KiB, because headers are expected to be
        // 1KiB at most
        super(ByteBuffer.allocate(1024));
        this.file = file;
    }

    /**
     * workaround for a bug involving read_write maps and read + append FileChannels
     */
    private FileOutputStream(FileChannel file, long position) {
        // the size is smaller then 4KiB, because headers are expected to be
        // 1KiB at most
        super(ByteBuffer.allocate(1024));
        this.file = file;
        this.position = position;
    }

    public static FileOutputStream write(Path target) throws IOException {
        Files.deleteIfExists(target);
        return new FileOutputStream(FileChannel.open(target, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.READ));
    }

    /**
     * @return a new file output stream, that is setup to append to the target fileoutput stream, that is setup to write
     *         the target file
     * @throws IOException
     *             propagated error
     */
    public static FileOutputStream append(Path target) throws IOException {
        FileChannel fc = FileChannel.open(target, StandardOpenOption.WRITE, StandardOpenOption.READ);
        // workaround for a bug involving read_write maps and read + append
        // FileChannels
        fc.position(fc.size());
        return new FileOutputStream(fc, fc.size());
    }

    @Override
    protected void refresh() throws IOException {
        if (null == buffer)
            buffer = ByteBuffer.allocate(BUFFERSIZE);
        else if (0 != buffer.position()) {
            flush();
            buffer = ByteBuffer.allocate(BUFFERSIZE);
        }
    }

    private void flush() throws IOException {
        if (null != buffer) {
            final int p = buffer.position();
            buffer.limit(p);
            buffer.position(0);
            assert (p == buffer.remaining());
            file.write(buffer, position);
            position += p;
        }
    }

    /**
     * put an array of bytes into the stream
     * 
     * @note you may not reuse data after putting it to a stream, because the actual put might be a deferred operation
     * @param data
     *            the data to be written
     */
    public void put(byte[] data) throws IOException {
        if (data.length > BUFFERSIZE) {
            if (null != buffer) {
                flush();
                buffer = null;
            }
            file.write(ByteBuffer.wrap(data), position);
            position += data.length;
        } else {
            if (null == buffer || buffer.position() + data.length > BUFFERSIZE)
                refresh();
            buffer.put(data);
        }
    }

    /**
     * put a ByteBuffer into the stream
     * 
     * @note you may not reuse data after putting it to a stream, because the actual put might be a deferred operation
     * @param data
     *            the data to be written
     */
    public void put(ByteBuffer data) throws IOException {
        if (null != buffer) {
            flush();
            buffer = null;
        }
        final int size = data.remaining();
        file.write(data, position);
        position += size;
    }

    /**
     * Creates a map as usually used for writing field data chunks concurrently.
     * 
     * @param basePosition
     *            absolute start index of the mapped region
     * @param begin
     *            begin offset of the mapped region
     * @param end
     *            end offset of the mapped region
     */
    synchronized public MappedOutStream map(long basePosition, long begin, long end) throws IOException {
        if (null != buffer) {
            flush();
            buffer = null;
        }
        long p = basePosition + end;
        position = position < p ? p : position;
        return new MappedOutStream(file.map(MapMode.READ_WRITE, basePosition + begin, end - begin));
    }

    /**
     * signal the stream to close
     */
    public void close() throws IOException {
        flush();
        file.force(false);
        file.close();
    }

    @Override
    public final void v64(long v) throws IOException {
        if (null == buffer || buffer.position() + 9 >= BUFFERSIZE)
            refresh();

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
