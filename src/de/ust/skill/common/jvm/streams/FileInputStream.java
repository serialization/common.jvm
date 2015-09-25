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
import java.util.Stack;

/**
 * FileChannel based input stream.
 *
 * @author Timm Felden
 */
final public class FileInputStream extends InStream {

    private final Stack<Long> positions = new Stack<Long>();
    private final Path path;
    private final FileChannel file;

    public static FileInputStream open(Path path) throws IOException {
        FileChannel file = (FileChannel) Files.newByteChannel(path, StandardOpenOption.READ);
        return new FileInputStream(file, path);
    }

    private FileInputStream(FileChannel file, Path path) throws IOException {
        super(file.map(MapMode.READ_ONLY, 0, file.size()));
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

    public void push(long position) {
        positions.push((long) input.position());
        input.position((int) position);
    }

    public void pop() {
        input.position(positions.pop().intValue());
    }

    public Path path() {
        return path;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        if (file.isOpen())
            file.close();
    }
}
