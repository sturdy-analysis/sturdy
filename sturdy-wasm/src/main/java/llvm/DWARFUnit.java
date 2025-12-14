package llvm;

public class DWARFUnit {
    private final long handle;          // DWARFUnit*
    private final DWARFContext owner;   // keeps context alive

    DWARFUnit(long handle, DWARFContext owner) {
        this.handle = handle;
        this.owner = owner;
    }
    //native functions:
    /**
     * @return the pointer for this llvm::DWARFUnit object on the native side
     */
    private native long getUnitDIEHandle();
    
    //JVM functions:

    /**
     * @return the root die for this DWARFUnit
     */
    public DWARFDie getUnitDIE() {
        return new DWARFDie(getUnitDIEHandle());
    }
}