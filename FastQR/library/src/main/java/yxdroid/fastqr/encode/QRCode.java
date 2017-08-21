package yxdroid.fastqr.encode;

import android.content.Context;
import android.graphics.Bitmap;

import yxdroid.fastqr.utils.BitmapUtil;

public class QRCode {

    public static final int DEFAULT_QR_SIZE = 380;
    public static final int DEFAULT_QR_MARGIN = 2;

    // 宽
    private int width;
    // 高
    private int height;

    // 内容
    private String content;

    // 1 颜色
    private int oneColor;
    // 0 颜色
    private int zeroColor;

    // 二维码颜色源 来自图片映射
    private Bitmap colorBitmap;

    // 中间logo
    private Bitmap logoBitmap;

    // 边缘空白区域大小
    private int margin;

    // 边框
    private Bitmap border;

    // 圆角
    private int cornerSize;

    // 容错率
    private ErrorCorrection errorCorrection;

    public QRCode(Builder builder) {
        this.width = builder.width;
        this.height = builder.height;
        this.content = builder.content;
        this.oneColor = builder.oneColor;
        this.zeroColor = builder.zeroColor;
        this.colorBitmap = builder.colorBitmap;
        this.logoBitmap = builder.logoBitmap;
        this.margin = builder.margin;
        this.border = builder.border;
        this.cornerSize = builder.cornerSize;
        this.errorCorrection = builder.errorCorrection;
    }

    public QRCode(String content) {
        this.content = content;
    }

    public int getWidth() {
        if (width == 0) {
            return DEFAULT_QR_SIZE;
        }
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        if (height == 0) {
            return DEFAULT_QR_SIZE;
        }
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getOneColor() {
        return oneColor;
    }

    public void setOneColor(int oneColor) {
        this.oneColor = oneColor;
    }

    public int getZeroColor() {
        return zeroColor;
    }

    public void setZeroColor(int zeroColor) {
        this.zeroColor = zeroColor;
    }

    public Bitmap getColorBitmap() {
        return colorBitmap;
    }

    public void setColorBitmap(Bitmap colorBitmap) {
        this.colorBitmap = colorBitmap;
    }

    public Bitmap getLogoBitmap() {
        return logoBitmap;
    }

    public void setLogoBitmap(Bitmap logoBitmap) {
        this.logoBitmap = logoBitmap;
    }

    public int getMargin() {
        if (margin == 0) {
            return DEFAULT_QR_MARGIN;
        }
        return margin;
    }

    public void setMargin(int margin) {
        this.margin = margin;
    }

    public Bitmap getBorder() {
        return border;
    }

    public void setBorder(Bitmap border) {
        this.border = border;
    }

    public int getCornerSize() {
        return cornerSize;
    }

    public void setCornerSize(int cornerSize) {
        this.cornerSize = cornerSize;
    }

    public ErrorCorrection getErrorCorrection() {
        if (errorCorrection == null) {
            return ErrorCorrection.H;
        }
        return errorCorrection;
    }

    public void setErrorCorrection(ErrorCorrection errorCorrection) {
        this.errorCorrection = errorCorrection;
    }

    public static class Builder {

        private Context context;

        private int width;
        private int height;
        private String content;
        private int oneColor;
        private int zeroColor;
        private Bitmap colorBitmap;
        private Bitmap logoBitmap;
        private int margin;
        private Bitmap border;
        private int cornerSize;
        private ErrorCorrection errorCorrection;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder size(int widthPix, int heightPix) {
            this.width = widthPix;
            this.height = heightPix;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder oneColor(int oneColor) {
            this.oneColor = oneColor;
            return this;
        }


        public Builder zeroColor(int zeroColor) {
            this.zeroColor = zeroColor;
            return this;
        }

        public Builder color(int oneColor, int zeroColor) {
            this.oneColor = oneColor;
            this.zeroColor = zeroColor;
            return this;
        }

        public Builder colorBitmap(int drawable) {
            this.colorBitmap = BitmapUtil.convertBitmap(context, drawable);
            return this;
        }

        public Builder logo(Bitmap logoBitmap) {
            this.logoBitmap = logoBitmap;
            return this;
        }

        public Builder logo(String assertFile) {
            this.logoBitmap = BitmapUtil.convertBitmap(context, assertFile);
            return this;
        }

        public Builder logo(int drawable) {
            this.logoBitmap = BitmapUtil.convertBitmap(context, drawable);
            return this;
        }

        public Builder margin(int size) {
            this.margin = size;
            return this;
        }

        public Builder border(Bitmap bitmap) {
            this.border = bitmap;
            return this;
        }

        public Builder border(String assertFile) {
            this.border = BitmapUtil.convertBitmap(context, assertFile);
            return this;
        }

        public Builder border(int drawable) {
            this.border = BitmapUtil.convertBitmap(context, drawable);
            return this;
        }

        public Builder corner(int cornerSize) {
            this.cornerSize = cornerSize;
            return this;
        }

        /**
         * 设置容错级别
         *
         * @param errorCorrection
         * @return
         */
        public Builder errorCorrection(ErrorCorrection errorCorrection) {
            this.errorCorrection = errorCorrection;
            return this;
        }

        public QRCode build() {
            return new QRCode(this);
        }
    }

}
