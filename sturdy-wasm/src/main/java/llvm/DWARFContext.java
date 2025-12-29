package swam.binary.custom.dwarf.llvm;

import java.io.File;
import java.lang.ref.Cleaner;
import java.util.Arrays;
import java.util.List;

/**
 * This class contains the main DWARFContext and also has ownership of the loaded DWARF Debug information.
 * All other classes (DWARFUnit, DWARFDie, ...) just use the data provided by the DWARFContext.
 */
public class DWARFContext implements AutoCloseable {
    private static final Cleaner CLEANER = Cleaner.create();
    private final Cleaner.Cleanable cleanable;
    private final long handle; //DWARFContextHandle*
    /*It can NOT be converted to a local variable in the constructor because the C++ code accesses it!*/
    public DWARFContext(String path) {
        //TODO: fix absolute/relative path issues
        handle = createFromFile(path);
        //System.out.println("Current working dierectory (scala):\t" + System.getProperty("user.dir"));
        if (handle == 0) { throw new RuntimeException("Failed to create DWARFContext"); }
        cleanable = CLEANER.register(this, new NativeCleaner(handle, DWARFContext::destroy));        
    }
    //native functions:
    /**
     * Frees the memory of the allocated WrappedContext Struct (MemoryBuffer, ObjectFile and DWARFContext)
     * @param handle pointer to the heap allocated struct
     */
    static native void destroy(long handle);
    /**
     * Creates a DWARFContext object from the given path
     * @param path should be absolute but should also work with a path relative to the current working directory
     *             -> System.getProperty("user.dir")
     *             If using a relative path do not use a starting '/'
     *             so instead of '/src/test/...' use 'src/test'
     *             TODO: detect whether path is relative or absolute
     *             I don't know why llvm does not like the starting '/' of a relative path.
     * @return pointer to a WrappedContext struct allocated on the native heap (C++ pointer)
     */
    private native long createFromFile(String path);
    /**
     * @return an array of pointers to all compile units contained in the DWARFContext
     */
    private native long[] getCompileUnitHandles();
    /**
     * just a function to test things out on the native side. avoids generating the JNI headers
     */
    public native void devTest();

    //JVM functions:
    /**
     * frees the native memory of the DWARFContextHandle.
     */
    @Override
    public void close() { cleanable.clean(); }
    /**
     * @return a List of DWARFUnits that were returned by getCompileUnitHandles
     */
    public List<DWARFUnit> CompileUnits() {
        return Arrays.stream(getCompileUnitHandles())
                .mapToObj(ptr -> new DWARFUnit(ptr, this))
                .toList();
    }
    static {
        String sep = File.separator;
        String cwd = System.getProperty("user.dir");
        String pathToBuild = "sturdy-wasm" + sep + "src" + sep + "main" + sep + "build";
        String libname = "libLLVMDWARFAPI.so";
        String fullpath = cwd + sep + pathToBuild + sep + libname;
        try {
            System.out.println("Loading: " + fullpath);
            System.load(fullpath);
        } catch (UnsatisfiedLinkError e) {
            System.err.println("ERROR: Could not load " + libname +". " +
                    "Make sure the library exists under '"+ fullpath +"'.\n" +
                    "It should exist under '<project-root>/src/main/build/'\n" +
                    "and be included in the following list:\n" +
                    "\nCurrent 'java.library.path':");
            for (String path : System.getProperty("java.library.path").split(":")) {
                System.err.println("  " + path);
            }
            // Optionally, exit the program
            System.exit(1);
        }
    }
}
