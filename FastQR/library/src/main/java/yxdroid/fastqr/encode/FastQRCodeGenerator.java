package yxdroid.fastqr.encode;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Hashtable;

public class FastQRCodeGenerator {

    private static final String TAG = "QRCodeGenerator";

    public static void genQrCode(QRCode qrCode, String savePath) {
        Bitmap bitmap = genQrCodeBitmap(qrCode);
        try {
            if (bitmap != null && !TextUtils.isEmpty(savePath)) {
                File saveFile = new File(savePath);
                File dir = saveFile.getParentFile();
                if (dir == null) {
                    return;
                }

                if (!dir.exists()) {
                    dir.mkdirs();
                }
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(savePath));
            } else {
                Log.e(TAG, "gen qrcode fail");
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static Bitmap genQrCodeBitmap(QRCode qrCode) {

        if (qrCode == null) {
            Log.e(TAG, "qrcode is null");
            return null;
        }

        int widthPix = qrCode.getWidth(), heightPix = qrCode.getHeight();
        if (widthPix == 0 || widthPix == 0) {
            Log.e(TAG, "qrcode size is 0");
            return null;
        }

        String content = qrCode.getContent();
        if (TextUtils.isEmpty(content)) {
            Log.e(TAG, "qrcode content is empty");
            return null;
        }

        //配置参数
        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        //容错级别
        hints.put(EncodeHintType.ERROR_CORRECTION,
                ErrorCorrectionLevel.forBits(qrCode.getErrorCorrection().getBits()));

        //设置空白边距的宽度
        hints.put(EncodeHintType.MARGIN, qrCode.getMargin()); //default is 2

        Bitmap resultBitmap = null;
        try {
            // 图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE,
                    widthPix, heightPix, hints);
            int[] pixels = new int[widthPix * heightPix];
            // 两个for循环是图片横列扫描的结果
            for (int y = 0; y < heightPix; y++) {
                for (int x = 0; x < widthPix; x++) {
                    if (bitMatrix.get(x, y)) {
                        bitMatrix.flip(x, y);
                        Bitmap colorBitmap = qrCode.getColorBitmap();
                        if (colorBitmap == null) {
                            pixels[y * widthPix + x] = qrCode.getOneColor();
                        } else {
                            int pixColor = colorBitmap.getPixel(x, y);
                            pixels[y * widthPix + x] = Color.argb(Color.alpha(pixColor),
                                    Color.red(pixColor), Color.green(pixColor), Color.blue(pixColor));
                        }
                    } else {
                        pixels[y * widthPix + x] = qrCode.getZeroColor();
                    }
                }
            }

            // 生成二维码图片的格式，使用ARGB_8888
            resultBitmap = Bitmap.createBitmap(widthPix, heightPix, Bitmap.Config.ARGB_8888);
            resultBitmap.setPixels(pixels, 0, widthPix, 0, 0, widthPix, heightPix);

            // logo
            Bitmap logoBitmap = qrCode.getLogoBitmap();
            if (logoBitmap != null) {
                resultBitmap = addLogo(resultBitmap, logoBitmap);
            }

            // 圆角
            int cornerSize = qrCode.getCornerSize();
            if (cornerSize > 0) {
                resultBitmap = addCorner(resultBitmap, cornerSize);
            }

            // 边框
            Bitmap borderBitmap = qrCode.getBorder();
            if (borderBitmap != null) {
                resultBitmap = addBorder(resultBitmap, borderBitmap);
            }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return resultBitmap;
    }

    private static Bitmap addCorner(Bitmap src, int cornerSize) {

        Bitmap output = Bitmap.createBitmap(src.getWidth(),
                src.getHeight(), Bitmap.Config.ARGB_8888);

        try {
            Canvas canvas = new Canvas(output);
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, src.getWidth(),
                    src.getHeight());
            final RectF rectF = new RectF(new Rect(0, 0, src.getWidth(),
                    src.getHeight()));
            final float roundPx = cornerSize;
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(Color.BLACK);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(src, rect, rect, paint);
            return output;
        } catch (Exception e) {
            output = null;
            Log.e(TAG, e.getMessage());
        }
        src.recycle();
        return output;
    }

    private static Bitmap addBorder(Bitmap src, Bitmap border) {
        if (src == null) {
            return null;
        }

        if (border == null) {
            return src;
        }

        //获取图片的宽高
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int borderWidth = border.getWidth();
        int borderHeight = border.getHeight();

        if (srcWidth == 0 || srcHeight == 0) {
            return null;
        }

        if (borderWidth == 0 || borderHeight == 0) {
            return src;
        }

        Bitmap bitmap = Bitmap.createBitmap(borderWidth, borderHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(border, 0, 0, null);
            canvas.drawBitmap(src, (borderWidth - srcWidth) / 2, (borderHeight - srcHeight) / 2, null);

            canvas.save(Canvas.ALL_SAVE_FLAG);
            canvas.restore();
        } catch (Exception e) {
            bitmap = null;
            Log.e(TAG, e.getMessage());
        }

        src.recycle();
        border.recycle();
        return bitmap;
    }

    private static Bitmap addLogo(Bitmap src, Bitmap logo) {
        if (src == null) {
            return null;
        }

        if (logo == null) {
            return src;
        }

        //获取图片的宽高
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();

        if (srcWidth == 0 || srcHeight == 0) {
            return null;
        }

        if (logoWidth == 0 || logoHeight == 0) {
            return src;
        }

        //logo大小为二维码整体大小的1/4
        float scaleFactor = srcWidth * 1.0f / 4 / logoWidth;
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(src, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null);

            canvas.save(Canvas.ALL_SAVE_FLAG);
            canvas.restore();
        } catch (Exception e) {
            bitmap = null;
            Log.e(TAG, e.getMessage());
        }

        src.recycle();
        logo.recycle();
        return bitmap;
    }

}
