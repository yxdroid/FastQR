package yxdroid.fastqr.encode;

public enum ErrorCorrection {
    L(1),
    M(0),
    Q(3),
    H(2);

    private final int bits;

    private ErrorCorrection(int bits) {
        this.bits = bits;
    }

    public int getBits() {
        return bits;
    }
}
