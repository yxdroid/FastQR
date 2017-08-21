package yxdroid.fastqr.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.zxing.Result;

import java.io.IOException;

import yxdroid.fastqr.camera.CameraManager;
import yxdroid.fastqr.decode.DecodeThread;

import static android.content.ContentValues.TAG;

public class QRScanView extends FrameLayout implements SurfaceHolder.Callback {

    private CameraManager cameraManager;

    private CaptureActivityHandler handler;

    private BaseQRScanActivity activity;

    private SurfaceView surfaceView;

    private boolean isHasSurface = false;

    private OnQRScanListener onQRScanListener;

    private ViewfinderView viewfinderView;

    public QRScanView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public QRScanView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        activity = (BaseQRScanActivity) context;

        surfaceView = new SurfaceView(context);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(surfaceView, layoutParams);

        cameraManager = new CameraManager(activity.getApplication());
        viewfinderView = new ViewfinderView(context, null);
        viewfinderView.setCameraManager(cameraManager);
        addView(viewfinderView, layoutParams);
    }

    public void onResume() {
        if (isHasSurface) {
            initCamera(surfaceView.getHolder());
        } else {
            surfaceView.getHolder().addCallback(this);
        }
    }

    public void onPause() {
        cameraManager.stopPreview();
        cameraManager.closeDriver();

        if (!isHasSurface) {
            surfaceView.getHolder().removeCallback(this);
        }
    }

    public void onDestory() {
        viewfinderView.recycle();
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    @Override
    public CaptureActivityHandler getHandler() {
        return handler;
    }

    public void setOnQRScanListener(OnQRScanListener onQRScanListener) {
        this.onQRScanListener = onQRScanListener;
    }

    public Rect getmCropRect() {
        return cameraManager.getFramingRect();
    }

    public void setQRTipText(String text) {
        viewfinderView.setTipText(text);
    }

    public void setScanLine(int res) {
        viewfinderView.setScanLineRes(res);
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            if (handler == null) {
                handler = new CaptureActivityHandler(this, cameraManager, DecodeThread.ALL_MODE);
            }
            cameraManager.startPreview();
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            cameraError(ioe);
        } catch (RuntimeException e) {
            Log.w(TAG, "Unexpected error initializing camera", e);
            cameraError(e);
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        if (!isHasSurface) {
            isHasSurface = true;
            initCamera(holder);
            invalidate();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isHasSurface = false;
    }

    private void cameraError(Exception e) {

        if (onQRScanListener != null) {
            onQRScanListener.onScanError(e);
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("提示");
        builder.setMessage("相机打开出错，请稍后重试");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.finish();
            }

        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                activity.finish();
            }
        });
        builder.show();
    }

    public void handleDecode(Result obj, Bundle bundle) {
        if (onQRScanListener != null) {
            onQRScanListener.onScanSuccess(obj.getText());
        } else {
            Toast.makeText(activity, obj.getText(), Toast.LENGTH_SHORT).show();
        }
    }

}
