package yxdroid.fastqr.encode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.Writer;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.Map;

public class QRCodeWriter implements Writer {

    @Override
    public BitMatrix encode(String s, BarcodeFormat barcodeFormat, int i, int i1) throws WriterException {
        return null;
    }

    @Override
    public BitMatrix encode(String s, BarcodeFormat barcodeFormat, int i, int i1, Map<EncodeHintType, ?> map) throws WriterException {
        return null;
    }
}
