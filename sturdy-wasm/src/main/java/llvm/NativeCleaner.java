package llvm;

import java.util.function.LongConsumer;

/**
 * Takes a long representing a pointer of a C++ Object and handles memory management on the native side once the
 * java object gets garbage collected
 */
public class NativeCleaner implements Runnable {
    private long handle;
    private final LongConsumer destroyer;

    public NativeCleaner(long handle, LongConsumer destroyer) {
        this.handle = handle;
        this.destroyer = destroyer;
    }

    @Override
    public void run() {
        if (handle != 0) {
            destroyer.accept(handle);
            handle = 0;
        }
    }
}
