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

import sun.misc.Cleaner;
import sun.nio.ch.DirectBuffer;

/**
 * FileChannel based input stream.
 *
 * @author Timm Felden
 */
final public class FileInputStream extends InStream {

    private int storedPosition;
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
     * Maps a part of a file not changing the position of the file stream.
     * 
     * @param basePosition
     *            absolute start index of the mapped region
     * @param begin
     *            begin offset of the mapped region
     * @param end
     *            end offset of the mapped region
     */
    synchronized public MappedInStream map(long basePosition, long begin, long end) {
        ByteBuffer r = input.duplicate();
        r.position((int) (basePosition + begin));
        r.limit((int) (basePosition + end));
        return new MappedInStream(r);
    }

    @Override
    public void jump(long position) {
        input.position((int) position);
    }

    /**
     * Maps from current position until offset.
     * 
     * @return a buffer that has exactly offset many bytes remaining
     */
    public final MappedInStream jumpAndMap(int offset) {
        ByteBuffer r = input.duplicate();
        int pos = input.position();
        r.limit(pos + offset);
        input.position(pos + offset);
        return new MappedInStream(r);
    }

    /**
     * save current position and jump to a new one
     */
    public void push(long position) {
        storedPosition = input.position();
        input.position((int) position);
    }

    /**
     * restore saved position
     */
    public void pop() {
        input.position(storedPosition);
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
