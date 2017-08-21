package yxdroid.fastqr.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;

import java.util.ArrayList;
import java.util.List;

import yxdroid.fastqr.camera.CameraManager;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
final class ViewfinderView extends View {

    private static final int CURRENT_POINT_OPACITY = 0xA0;
    private static final int MAX_RESULT_POINTS = 20;
    private static final int POINT_SIZE = 6;
    private static final int DEFAULT_LASER_LINE_HEIGHT = 2;//扫描线默认高度
    private static final int DEFAULT_LASER_MOVE_SPEED = 6;//默认每毫秒移动6px

    private CameraManager cameraManager;
    private final Paint paint;
    private Bitmap resultBitmap;
    private List<ResultPoint> possibleResultPoints;
    private List<ResultPoint> lastPossibleResultPoints;

    private int animationDelay = 0;
    private Bitmap laserLineBitmap;

    private int maskColor = Scanner.color.VIEWFINDER_MASK;//扫描框以外区域半透明黑色
    private int resultColor = Scanner.color.RESULT_VIEW;//扫描成功后扫描框以外区域白色
    private int resultPointColor = Scanner.color.POSSIBLE_RESULT_POINTS;//聚焦扫描线中聚焦点红色
    private int lineColor = Scanner.color.VIEWFINDER_LASER;//扫描线颜色
    private int border4CornerColor = lineColor;//扫描框4角颜色
    private int laserLineTop;// 扫描线最顶端位置
    private int scanLineHeight;//扫描线默认高度
    private int scanLineMoveSpeed;// 扫描线默认移动1距离px
    private int border4CornerWidth;//扫描框4角宽
    private int border4CornerHeight;//扫描框4角高
    private int scanLineRes;//扫描线图片资源
    private String drawText = "将二维码放入框内，即可自动扫描";//提示文字
    private int drawTextSize;//提示文字大小
    private int drawTextColor = Color.WHITE;//提示文字颜色
    private boolean drawTextGravityBottom = true;//提示文字位置
    private int drawTextMargin;//提示文字与扫描框距离
    private boolean isScanGridLine;//是否为网格资源文件

    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        possibleResultPoints = new ArrayList<>(5);
        lastPossibleResultPoints = null;
        scanLineMoveSpeed = DEFAULT_LASER_MOVE_SPEED;//默认每毫秒移动6px
        scanLineHeight = Scanner.dp2px(context, DEFAULT_LASER_LINE_HEIGHT);
        border4CornerWidth = Scanner.dp2px(context, 2f);
        border4CornerHeight = Scanner.dp2px(context, 15f);
        drawTextSize = Scanner.sp2px(context, 15f);
        drawTextMargin = Scanner.dp2px(context, 20f);
    }

    void setCameraManager(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (cameraManager == null) {
            return;
        }
        // 正方形扫描框
        Rect frame = cameraManager.getFramingRect();
        //取屏幕预览
        Rect previewFrame = cameraManager.getFramingRectInPreview();
        if (frame == null || previewFrame == null) {
            return;
        }
        // 绘制扫描框以外4个区域
        drawMask(canvas, frame);
        //绘制扫描框
        drawFrame(canvas, frame);
        //绘制扫描框4角
        drawFrameCorner(canvas, frame);
        // 画扫描框下面的字
        drawText(canvas, frame);
        //绘制扫描线
        drawLaserLine(canvas, frame);
        //绘制扫描点标记
        drawResultPoint(canvas, frame, previewFrame);
        //计算移动位置
        moveLaserSpeed(frame);
    }

    private void moveLaserSpeed(Rect frame) {
        //初始化扫描线最顶端位置
        if (laserLineTop == 0) {
            laserLineTop = frame.top;
        }
        // 每次刷新界面，扫描线往下移动 LASER_VELOCITY
        laserLineTop += scanLineMoveSpeed;
        if (laserLineTop >= frame.bottom) {
            laserLineTop = frame.top;
        }
        if (animationDelay == 0) {
            animationDelay = (int) ((1.0f * 1000 * scanLineMoveSpeed) / (frame.bottom - frame.top));
        }

        // 只刷新扫描框的内容，其他地方不刷新
        postInvalidateDelayed(animationDelay, frame.left - POINT_SIZE, frame.top - POINT_SIZE
                , frame.right + POINT_SIZE, frame.bottom + POINT_SIZE);
    }

    /**
     * 画扫描框外区域
     *
     * @param canvas
     * @param frame
     */
    private void drawMask(Canvas canvas, Rect frame) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        paint.setColor(resultBitmap != null ? resultColor : maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);
    }

    /**
     * 绘制提示文字
     *
     * @param canvas
     * @param frame
     */
    private void drawText(Canvas canvas, Rect frame) {
        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(drawTextColor);
        textPaint.setTextSize(drawTextSize);

        float x = frame.left;//文字开始位置
        //根据 drawTextGravityBottom 文字在扫描框上方还是下文，默认下方
        float y = drawTextGravityBottom ? frame.bottom + drawTextMargin
                : frame.top - drawTextMargin;

        StaticLayout staticLayout = new StaticLayout(drawText, textPaint, frame.width()
                , Layout.Alignment.ALIGN_CENTER, 1.0f, 0, false);
        canvas.save();
        canvas.translate(x, y);
        staticLayout.draw(canvas);
        canvas.restore();
    }

    /**
     * 绘制扫描框4角
     *
     * @param canvas
     * @param frame
     */
    private void drawFrameCorner(Canvas canvas, Rect frame) {
        paint.setColor(border4CornerColor);
        paint.setStyle(Paint.Style.FILL);
        // 左上角
        canvas.drawRect(frame.left - border4CornerWidth, frame.top, frame.left, frame.top
                + border4CornerHeight, paint);
        canvas.drawRect(frame.left - border4CornerWidth, frame.top - border4CornerWidth
                , frame.left + border4CornerHeight, frame.top, paint);
        // 右上角
        canvas.drawRect(frame.right, frame.top, frame.right + border4CornerWidth,
                frame.top + border4CornerHeight, paint);
        canvas.drawRect(frame.right - border4CornerHeight, frame.top - border4CornerWidth,
                frame.right + border4CornerWidth, frame.top, paint);
        // 左下角
        canvas.drawRect(frame.left - border4CornerWidth, frame.bottom - border4CornerHeight,
                frame.left, frame.bottom, paint);
        canvas.drawRect(frame.left - border4CornerWidth, frame.bottom, frame.left
                + border4CornerHeight, frame.bottom + border4CornerWidth, paint);
        // 右下角
        canvas.drawRect(frame.right, frame.bottom - border4CornerHeight, frame.right
                + border4CornerWidth, frame.bottom, paint);
        canvas.drawRect(frame.right - border4CornerHeight, frame.bottom, frame.right
                + border4CornerWidth, frame.bottom + border4CornerWidth, paint);
    }

    /**
     * 画扫描框
     *
     * @param canvas
     * @param frame
     */
    private void drawFrame(Canvas canvas, Rect frame) {
        paint.setColor(Color.WHITE);//扫描边框白色
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(frame, paint);
    }

    /**
     * 画扫描线
     *
     * @param canvas
     * @param frame
     */
    private void drawLaserLine(Canvas canvas, Rect frame) {
        if (scanLineRes == 0) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(lineColor);// 设置扫描线颜色
            canvas.drawRect(frame.left, laserLineTop, frame.right
                    , laserLineTop + scanLineHeight, paint);
        } else {
            if (laserLineBitmap == null)//图片资源文件转为 Bitmap
                laserLineBitmap = BitmapFactory.decodeResource(getResources(), scanLineRes);
            int height = laserLineBitmap.getHeight();//取原图高
            //网格图片
            if (isScanGridLine) {
                RectF dstRectF = new RectF(frame.left, frame.top, frame.right, laserLineTop);
                Rect srcRect = new Rect(0, (int) (height - dstRectF.height())
                        , laserLineBitmap.getWidth(), height);
                canvas.drawBitmap(laserLineBitmap, srcRect, dstRectF, paint);
            }
            //线条图片
            else {
                //如果没有设置线条高度，则用图片原始高度
                if (scanLineHeight == Scanner.dp2px(getContext(), DEFAULT_LASER_LINE_HEIGHT)) {
                    scanLineHeight = laserLineBitmap.getHeight() / 2;
                }
                Rect laserRect = new Rect(frame.left, laserLineTop, frame.right
                        , laserLineTop + scanLineHeight);
                canvas.drawBitmap(laserLineBitmap, null, laserRect, paint);
            }
        }
    }

    private void drawResultPoint(Canvas canvas, Rect frame, Rect previewFrame) {
        float scaleX = frame.width() / (float) previewFrame.width();
        float scaleY = frame.height() / (float) previewFrame.height();

        List<ResultPoint> currentPossible = possibleResultPoints;
        List<ResultPoint> currentLast = lastPossibleResultPoints;
        int frameLeft = frame.left;
        int frameTop = frame.top;
        if (currentPossible.isEmpty()) {
            lastPossibleResultPoints = null;
        } else {
            possibleResultPoints = new ArrayList<>(5);
            lastPossibleResultPoints = currentPossible;
            paint.setAlpha(CURRENT_POINT_OPACITY);
            paint.setColor(resultPointColor);
            synchronized (currentPossible) {
                for (ResultPoint point : currentPossible) {
                    canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX),
                            frameTop + (int) (point.getY() * scaleY),
                            POINT_SIZE, paint);
                }
            }
        }
        if (currentLast != null) {
            paint.setAlpha(CURRENT_POINT_OPACITY / 2);
            paint.setColor(resultPointColor);
            synchronized (currentLast) {
                float radius = POINT_SIZE / 2.0f;
                for (ResultPoint point : currentLast) {
                    canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX),
                            frameTop + (int) (point.getY() * scaleY),
                            radius, paint);
                }
            }
        }
    }

    public void drawViewfinder() {
        Bitmap resultBitmap = this.resultBitmap;
        this.resultBitmap = null;
        if (resultBitmap != null) {
            resultBitmap.recycle();
        }
        invalidate();
    }

    /**
     * Draw a bitmap with the result points highlighted instead of the live scanning display.
     *
     * @param barcode An image of the decoded barcode.
     */
    public void drawResultBitmap(Bitmap barcode) {
        resultBitmap = barcode;
        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
        List<ResultPoint> points = possibleResultPoints;
        synchronized (points) {
            points.add(point);
            int size = points.size();
            if (size > MAX_RESULT_POINTS) {
                // trim it
                points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
            }
        }
    }

    public void setScanLineColor(int lineColor) {
        this.lineColor = lineColor;
    }

    public void setScanLineRes(int scanLineRes) {
        this.scanLineRes = scanLineRes;
        this.isScanGridLine = false;
    }

    public void setScanGridLineRes(int scanLineRes) {
        this.scanLineRes = scanLineRes;
        this.isScanGridLine = true;
    }

    public void setScanLineHeight(int lineHeight) {
        this.scanLineHeight = Scanner.dp2px(getContext(), lineHeight);
    }

    /**
     * 扫描框 4角颜色
     *
     * @param border4CornerColor
     */
    public void setBorder4CornerColor(int border4CornerColor) {
        this.border4CornerColor = border4CornerColor;
    }

    /**
     * 扫描框 4角宽度
     *
     * @param border4CornerHeight
     */
    public void setBorder4CornerHeight(int border4CornerHeight) {
        this.border4CornerHeight = Scanner.dp2px(getContext(), border4CornerHeight);
    }

    /**
     * 扫描框 4角高度
     *
     * @param border4CornerWidth
     */
    public void setBorder4CornerWidth(int border4CornerWidth) {
        this.border4CornerWidth = Scanner.dp2px(getContext(), border4CornerWidth);
    }

    /**
     * 二维码下方提示语
     * @param text
     */
    public void setTipText(String text) {
        if (!TextUtils.isEmpty(text)) {
            drawText = text;
        }
    }

    public void setTipText(String text, int textSize, int textColor
            , boolean isBottom, int textMargin) {
        if (!TextUtils.isEmpty(text))
            drawText = text;
        if (textSize > 0)
            drawTextSize = Scanner.sp2px(getContext(), textSize);
        if (textColor != 0)
            drawTextColor = textColor;
        drawTextGravityBottom = isBottom;
        if (textMargin > 0)
            drawTextMargin = Scanner.dp2px(getContext(), textMargin);
    }

    public void setTipText(int textColor) {
        if (textColor != 0)
            drawTextColor = textColor;
    }

    public void setTipTextSize(int textSize) {
        if (textSize > 0)
            drawTextSize = Scanner.sp2px(getContext(), textSize);
    }

    public void setScanLineMoveSpeed(int moveSpeed) {
        if (moveSpeed <= 0) {
            scanLineMoveSpeed = DEFAULT_LASER_MOVE_SPEED;
        } else {
            scanLineMoveSpeed = moveSpeed;
        }
    }

    public void recycle() {
        if (laserLineBitmap != null) {
            laserLineBitmap.recycle();
            laserLineBitmap = null;
        }
    }
}