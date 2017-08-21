# FastQR
基于ZXing 封装的二维码生成器，以及快速实现二维码扫描

FastQR遵循了Build设计模式，提供了灵活的接口build出二维码生成模型

# 使用方式

1. Build 二维码模 QrCode 模型

QRCode.Builder builder = new QRCode.Builder(MainActivity.this)
        // 二维码内容
        .content("hello world")
        // 二维码前景色
        .oneColor(Color.BLACK)
        // 二维码背景色
        .zeroColor(Color.WHITE)
        // 二维码边距
        .margin(1)
        // 通过bitmap 来设置二维码前景色
        .colorBitmap(myAdapter.getCurrentColor())
        // 二维码容错级别
        .errorCorrection(ErrorCorrection.H);

2. 生成二维码

Bitmap bitmap = FastQRCodeGenerator.genQrCodeBitmap(builder.build());
ivQr.setImageBitmap(bitmap);        

# 显示效果

![](https://github.com/yxdroid/FastQR/blob/master/device-2017-08-21-104647.mp4)

# 二维码扫描

定义QRScanView作为扫描试图

public class QrScanActivity extends BaseQRScanActivity implements OnQRScanListener {

    private QRScanView qrScanView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_qr_scan);

        qrScanView = (QRScanView) findViewById(R.id.qrscan_view);
        // 设置扫描监听器
        qrScanView.setOnQRScanListener(this);
        // 设置扫描框下方提示语
        qrScanView.setQRTipText("hello fast qr");
        // 设置扫描横线
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