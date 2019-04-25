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

    private final FileChannel file;
    /**
     * true iff the file is shared with an output channel
     */
    private boolean sharedFile = false;

    FileChannel file() {
        sharedFile = true;
        return file;
    }

    /**
     * The factory method.
     * 
     * @note required to adhere to stupid Java initialization rules ;)
     */
    public static FileInputStream open(Path path) throws IOException {
        return new FileInputStream(
                (FileChannel) (Files.newByteChannel(path, StandardOpenOption.CREATE, StandardOpenOption.READ)), path);
    }

    private FileInputStream(FileChannel file, Path path) throws IOException {
        super(file.map(MapMode.READ_ONLY, 0, file.size()));
        this.file = file;
    }

    /**
     * @return size of the file in bytes
     * @note we can only get here, if size fits into int (ByteBuffer madness)
     */
    public int size() {
        try {
            return (int) file.size();
        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * Maps a part of a file. The position is moved behind the mapped region.
     * 
     * @param size
     *            number of bytes to be mapped to the outgoing buffer; if -1 is passed, a copy of the whole buffer is
     *            created
     */
    public MappedInStream map(int size) {
        ByteBuffer r = input.duplicate();
        // hack to allow string pool to clone the InStream
        if (-1 != size) {
            int next = input.position() + size;
            r.limit(next);
            input.position(next);
        }
        return new MappedInStream(r);
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
