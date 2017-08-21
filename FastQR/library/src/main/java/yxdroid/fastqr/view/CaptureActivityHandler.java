/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package yxdroid.fastqr.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.zxing.Result;

import yxdroid.fastqr.camera.CameraManager;
import yxdroid.fastqr.decode.DecodeHandler;
import yxdroid.fastqr.decode.DecodeThread;

public class CaptureActivityHandler extends Handler {
    public static final int HANDLE_RESTART_PREVIEW = 0x01;
    public static final int HANDLE_DECODE_SUCCEEDED = 0x02;
    public static final int HANDLE_DECODE_FAILED = 0x03;
    public static final int HANDLE_RETURN_SCAN_RESULT = 0x04;

    private final QRScanView qrScanView;
    private final DecodeThread decodeThread;
    private final CameraManager cameraManager;
    private State state;

    private enum State {
        PREVIEW, SUCCESS, DONE
    }

    public CaptureActivityHandler(QRScanView qrScanView, CameraManager cameraManager, int decodeMode) {
        this.qrScanView = qrScanView;
        decodeThread = new DecodeThread(qrScanView, decodeMode);
        decodeThread.start();
        state = State.SUCCESS;

        // Start ourselves capturing previews and decoding.
        this.cameraManager = cameraManager;
        cameraManager.startPreview();
        restartPreviewAndDecode();
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case HANDLE_RESTART_PREVIEW:
                restartPreviewAndDecode();
                break;
            case HANDLE_DECODE_SUCCEEDED:
                state = State.SUCCESS;
                Bundle bundle = message.getData();
                qrScanView.handleDecode((Result) message.obj, bundle);
                break;
            case HANDLE_DECODE_FAILED:
                // We're decoding as fast as possible, so when one decode fails,
                // start another.
                state = State.PREVIEW;
                cameraManager.requestPreviewFrame(decodeThread.getHandler(), DecodeHandler.HANDLE_DECODE);
                break;
            case HANDLE_RETURN_SCAN_RESULT:
                /*activity.setResult(Activity.RESULT_OK, (Intent) message.obj);
                activity.finish();*/
                break;
        }
    }

    public void quitSynchronously() {
        state = State.DONE;
        cameraManager.stopPreview();
        Message quit = Message.obtain(decodeThread.getHandler(), DecodeHandler.HANDLE_QUIT);
        quit.sendToTarget();
        try {
            // Wait at most half a second; should be enough time, and onPause()
            // will timeout quickly
            decodeThread.join(500L);
        } catch (InterruptedException e) {
            // continue
        }

        // Be absolutely sure we don't send any queued up messages
        removeMessages(HANDLE_DECODE_SUCCEEDED);
        removeMessages(HANDLE_DECODE_FAILED);
    }

    private void restartPreviewAndDecode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW;
            cameraManager.requestPreviewFrame(decodeThread.getHandler(), DecodeHandler.HANDLE_DECODE);
        }
    }

}
