package yxdroid.fastqr.view;

public interface OnQRScanListener {

    void onScanSuccess(String text);

    void onScanError(Exception e);
}
