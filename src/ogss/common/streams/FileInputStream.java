/*  ___ _  ___ _ _                                                            *\
** / __| |/ (_) | |       The SKilL Generator                                 **
** \__ \ ' <| | | |__     (c) 2014-18 University of Stuttgart                 **
** |___/_|\_\_|_|____|    see LICENSE                                         **
\*                                                                            */
package ogss.common.streams;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import sun.misc.Cleaner;
import sun.nio.ch.DirectBuffer;

/**
 * FileChannel based input stream.
 *
 * @author Timm Felden
 */
final public class FileInputStream extends InStream {

    private final Path path;
    private final FileChannel file;
    /**
     * true iff the file is shared with an output channel
     */
    private boolean sharedFile = false;

    FileChannel file() {
        sharedFile = true;
        return file;
    }

    public static FileInputStream open(Path path, boolean readOnly) throws IOException {
        FileChannel file = (FileChannel) (readOnly
                ? Files.newByteChannel(path, StandardOpenOption.CREATE, StandardOpenOption.READ)
                : Files.newByteChannel(path, StandardOpenOption.CREATE, StandardOpenOption.READ,
                        StandardOpenOption.WRITE));
        return new FileInputStream(file, path, readOnly);
    }

    private FileInputStream(FileChannel file, Path path, boolean readOnly) throws IOException {
        super(file.map(readOnly ? MapMode.READ_ONLY : MapMode.READ_WRITE, 0, file.size()));
        this.file = file;
        this.path = path;
    }

    /**
     * Maps a part of a file. The position is moved behind the mapped region.
     * 
     * @param size
     *            number of bytes to be mapped to the outgoing buffer
     */
    public MappedInStream map(int size) {
        ByteBuffer r = input.duplicate();
        int next = input.position() + size;
        r.limit(next);
        input.position(next);
        return new MappedInStream(r);
    }

    /**
     * Move the stream to a position.
     */
    public void jump(long position) {
        input.position((int) position);
    }

    /**
     * @return raw byte array taken from the stream at the required position
     */
    public final byte[] bytes(int position, int length) {
        final byte[] rval = new byte[length];
        final int storedPosition = input.position();
        input.position(position);
        input.get(rval);
        input.position(storedPosition);
        return rval;
    }

    public Path path() {
        return path;
    }

    public void close() throws IOException {
        if (file.isOpen()) {
            if (!sharedFile)
                file.close();

            if (null != input && input instanceof DirectBuffer) {
                Cleaner cleaner = ((DirectBuffer) input).cleaner();
                if (null != cleaner)
                    cleaner.clean();
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }
}
