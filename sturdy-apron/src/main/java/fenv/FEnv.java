package fenv;

public class FEnv {
    public static native int getRoundingMode();
    public static native void setRoundingMode(int roundingMode);
    public static native int FE_TONEAREST();
    public static native int FE_DOWNWARD();
    public static native int FE_UPWARD();
    public static native int FE_TOWARDZERO();

    static {
        System.loadLibrary("fenv");
    }
}
