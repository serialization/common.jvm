package ogss.common.streams;

import java.io.IOException;

/**
 * Wraps an output stream to allow efficient and correct implementation of bool().
 * 
 * @author Timm Felden
 */
public final class BoolOutWrapper extends OutStream {

    private byte cur;
    private byte off;
    private final OutStream out;

    final public void bool(boolean data) throws IOException {
        if (data)
            cur |= (1 << off);
        if (8 == ++off) {
            off = 0;
            i8(cur);
            cur = 0;
        }
    }

    /**
     * Take control of out to write a series of boolean values.
     */
    public BoolOutWrapper(OutStream out) {
        super(out.buffer);
        this.out = out;
    }

    @Override
    protected void refresh() throws IOException {
        out.refresh();
        this.buffer = out.buffer;

        if (0 != off) {
            off = 0;
            i8(cur);
            cur = 0;
        }

    }

    /**
     * Release control of out to allow writing of further values to out. Do not call any other method after calling
     * unwrap. 
     */
    public void unwrap() throws IOException {
        // TODO write with padding!

        // finish booleans
        if (0 != off) {
            i8(cur);
        }
    }

    /**
     * cannot happen
     */
    @Override
    public void close() {}

}
