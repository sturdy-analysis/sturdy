package llvm;

import java.lang.ref.Cleaner;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * This class allows access to the llvm::DWARFDie class.
 * currently supports:
 *      hasChildren()
 *      children() (returns an array instead of an iterator_range)
 *      getTag()
 *      getNameAttr() (DW_AT_name)
 *      getType
 */
public class DWARFDie implements AutoCloseable {
    private static final Cleaner CLEANER = Cleaner.create();
    private final Cleaner.Cleanable cleanable;
    private final long handle;          // DWARFDieHandle*
    DWARFDie(long handle) {
        this.handle = handle;
        cleanable = CLEANER.register(this, new NativeCleaner(handle, DWARFDie::destroy));
    }
    //native functions:
    static native void destroy(long handle);
    /**
     * @return true if this DWARFDie has child-Dies or false if it does not
     */
    public native boolean hasChildren();
    /**
     * Function to collect all child DWARFDies
     * @return array of handles pointing to native DWARFDie Wrapper Objects
     */
    private native long[] getChildHandles();
    private native int getAddrSizeFromUnit();
    /**
     * @return returns an int representing a
     */
    private native int getTagAsInteger();
    private native String getAttrAsString(int dwarfAttr); /*DwarfAttr.value*/
    private native long getAttrAsDWARFDieHandle(int dwarfAttr); /*DwarfAttr.value*/
    private native long getAttrAsLong(int dwarfAttr); /*DwarfAttr.value*/
    public native long getOffset();
    /**
     * just a function to have to be able to quickly test stuff without having to regenerate the JNI headers
     */
    public native void devTest();
    //JVM functions:
    /**
     * frees the native memory of the DWARFDieHandle
     */
    @Override
    public void close() { cleanable.clean(); }
    /**
     * Wrapper of the native function returning the offsets to create Java Objects
     * @return Java List containing all child DWARFDies of this DWARFDie
     */
    public List<DWARFDie> children() {
        return Arrays.stream(getChildHandles())
                .mapToObj(DWARFDie::new)
                .toList();
    }
    /**
     * returns the tag of the DWARFDie
     */
    public DwarfTag getTag() { return DwarfTag.fromValue(getTagAsInteger()); }

    /**
     * @param cmp DWARFTag to be compared to
     * @return true if the Die.getTag and cmp are the same DWARFTag, false if they are not the same tag
     */
    public boolean hasTag(DwarfTag cmp) { return getTag().equals(cmp); }
    /**
     * tries to get the DW_AT_name attribute.
     */
    public Optional<String> getNameAttr() {
        String attrVal = getAttrAsString(DwarfAttr.name.getValue());
        if (attrVal == null) return Optional.empty();
        return Optional.of(attrVal);
    }
    public Optional<DWARFDie> getTypeAttr() {
        long handle = getAttrAsDWARFDieHandle(DwarfAttr.type.getValue());
        if (handle == 0) return Optional.empty();
        return Optional.of(new DWARFDie(handle));
    }
    public Optional<Long> getByteSizeAttr() {
        long attrVal = getAttrAsLong(DwarfAttr.byte_size.getValue());
        if (attrVal == Long.MAX_VALUE) return Optional.empty();
        return Optional.of(attrVal);
    }
    public Optional<Long> getCountAttr() {
        long attrVal = getAttrAsLong(DwarfAttr.count.getValue());
        if (attrVal == Long.MAX_VALUE) return Optional.empty();
        return Optional.of(attrVal);
    }
    public int getAddrSize() {
        return getAddrSizeFromUnit();
    }
    public Optional<Long> getLocationAttr() {
        throw new RuntimeException("getLocationAttr does not work at the moment");
        //long attrVal = getAttrAsLong(DwarfAttr.location.getValue());
        //if (attrVal == Long.MAX_VALUE) return Optional.empty();
        //return Optional.of(attrVal);
    }
}
