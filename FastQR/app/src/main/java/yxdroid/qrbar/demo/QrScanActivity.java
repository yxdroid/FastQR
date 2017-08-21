package yxdroid.qrbar.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import yxdroid.fastqr.view.BaseQRScanActivity;
import yxdroid.fastqr.view.OnQRScanListener;
import yxdroid.fastqr.view.QRScanView;

public class QrScanActivity extends BaseQRScanActivity implements OnQRScanListener {

    private QRScanView qrScanView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_qr_scan);

        qrScanView = (QRScanView) findViewById(R.id.qrscan_view);
        qrScanView.setOnQRScanListener(this);
        qrScanView.setQRTipText("hello fast qr");
        qrScanView.setScanLine(R.mipmap.laser_li);
    }

    @Override
    protected void onResume() {
        super.onResume();
        qrScanView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        qrScanView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        qrScanView.onDestory();
    }

    @Override
    public void onScanSuccess(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onScanError(Exception e) {
        Toast.makeText(this, "相机打开异常", Toast.LENGTH_SHORT).show();
    }
}
