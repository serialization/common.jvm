/*  ___ _  ___ _ _                                                            *\
** / __| |/ (_) | |       The SKilL common JVM Implementation                 **
** \__ \ ' <| | | |__     (c) 2013-18 University of Stuttgart                 **
** |___/_|\_\_|_|____|    see LICENSE                                         **
\*                                                                            */
package de.ust.skill.common.jvm.streams;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

/**
 * BufferedOutputStream based output stream.
 *
 * @author Timm Felden
 */
final public class FileOutputStream extends OutStream {

    private static final int BUFFERSIZE = 1024;

    private final FileChannel file;

    private FileOutputStream(FileChannel file) {
        // the size is smaller then 4KiB, because headers are expected to be
        // 1KiB at most
        super(ByteBuffer.allocate(BUFFERSIZE));
        this.file = file;
    }

    /**
     * workaround for a bug involving read_write maps and read + append
     * FileChannels
     */
    private FileOutputStream(FileChannel file, long position) {
        // the size is smaller then 4KiB, because headers are expected to be
        // 1KiB at most
        super(ByteBuffer.allocate(BUFFERSIZE));
        this.file = file;
        this.position = position;
    }

    public static FileOutputStream write(FileInputStream target) throws IOException {
        FileChannel f = target.file();
        // can happen after multiple flush operations
        if (!f.isOpen()) {
            f = (FileChannel) Files.newByteChannel(target.path(), StandardOpenOption.CREATE, StandardOpenOption.READ,
                    StandardOpenOption.WRITE);
        }
        f.position(0);
        return new FileOutputStream(f);
    }

    /**
     * @return a new file output stream, that is setup to append to the target
     *         fileoutput stream, that is setup to write the target file
     * @throws IOException
     *             propagated error
     */
    public static FileOutputStream append(FileInputStream target) throws IOException {
        FileChannel fc = target.file();
        // can happen after multiple flushs
        if (!fc.isOpen()) {
            fc = (FileChannel) Files.newByteChannel(target.path(), StandardOpenOption.CREATE, StandardOpenOption.READ,
                    StandardOpenOption.WRITE);
        }
        // workaround for a bug involving read_write maps and read + append
        // FileChannels
        final long size = fc.size();
        fc.position(size);
        return new FileOutputStream(fc, size);
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
     * @note you may not reuse data after putting it to a stream, because the
     *       actual put might be a deferred operation
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
            if (null == buffer || buffer.remaining() < data.length)
                refresh();
            buffer.put(data);
        }
    }

    /**
     * Create a map and advance position by the map's size
     * 
     * @param size
     *            size of the mapped region
     * @return the created map
     * @throws IOException
     */
    public MappedOutStream mapBlock(int size) throws IOException {
        if (null != buffer) {
            flush();
            buffer = null;
        }
        long pos = this.position();
        MappedByteBuffer r = file.map(MapMode.READ_WRITE, pos, size);
        this.position = pos + size;
        return new MappedOutStream(r);
    }

    /**
     * put a ByteBuffer into the stream
     * 
     * @note you may not reuse data after putting it to a stream, because the
     *       actual put might be a deferred operation
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
     * signal the stream to close
     */
    @Override
    public void close() throws IOException {
        flush();
        if (file.size() != position) {
            file.truncate(position);
        }
        file.force(false);
        file.close();
    }

    public final void v64(int v) throws IOException {
        if (null == buffer || buffer.remaining() < 5)
            refresh();
        if (0 == (v & 0xFFFFFF80)) {
            buffer.put((byte) v);
        } else {
            buffer.put((byte) (0x80 | v));
            if (0 == (v & 0xFFFFC000)) {
                buffer.put((byte) (v >> 7));
            } else {
                buffer.put((byte) (0x80 | v >> 7));
                if (0 == (v & 0xFFE00000)) {
                    buffer.put((byte) (v >> 14));
                } else {
                    buffer.put((byte) (0x80 | v >> 14));
                    if (0 == (v & 0xF0000000)) {
                        buffer.put((byte) (v >> 21));
                    } else {
                        buffer.put((byte) (0x80 | v >> 21));
                        buffer.put((byte) (v >> 28));
                    }
                }
            }
        }
    }

    @Override
    public final void v64(long v) throws IOException {
        if (null == buffer || buffer.remaining() < 9)
            refresh();
        if (0L == (v & 0xFFFFFFFFFFFFFF80L)) {
            buffer.put((byte) v);
        } else {
            v64Medium(v);
        }
    }

    final private void v64Medium(long v) {
        buffer.put((byte) (0x80L | v));
        if (0L == (v & 0xFFFFFFFFFFFFC000L)) {
            buffer.put((byte) (v >> 7));
        } else {
            v64Large(v);
        }
    }

    final private void v64Large(long v) {
        buffer.put((byte) (0x80L | v >> 7));
        if (0L == (v & 0xFFFFFFFFFFE00000L)) {
            buffer.put((byte) (v >> 14));
        } else {
            buffer.put((byte) (0x80L | v >> 14));
            if (0L == (v & 0xFFFFFFFFF0000000L)) {
                buffer.put((byte) (v >> 21));
            } else {
                buffer.put((byte) (0x80L | v >> 21));
                if (0L == (v & 0xFFFFFFF800000000L)) {
                    buffer.put((byte) (v >> 28));
                } else {
                    buffer.put((byte) (0x80L | v >> 28));
                    if (0L == (v & 0xFFFFFC0000000000L)) {
                        buffer.put((byte) (v >> 35));
                    } else {
                        buffer.put((byte) (0x80L | v >> 35));
                        if (0L == (v & 0xFFFE000000000000L)) {
                            buffer.put((byte) (v >> 42));
                        } else {
                            buffer.put((byte) (0x80L | v >> 42));
                            if (0L == (v & 0xFF00000000000000L)) {
                                buffer.put((byte) (v >> 49));
                            } else {
                                buffer.put((byte) (0x80L | v >> 49));
                                buffer.put((byte) (v >> 56));
                            }
                        }
                    }
                }
            }
        }
    }
}
