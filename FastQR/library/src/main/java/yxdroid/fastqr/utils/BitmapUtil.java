package yxdroid.fastqr.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;

public class BitmapUtil {

    public static Bitmap convertBitmap(Context context, String assertFile) {
        Bitmap image = null;
        AssetManager am = context.getResources().getAssets();
        try {
            InputStream is = am.open(assertFile);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;

    }

    public static Bitmap convertBitmap(Context context, int drawable) {
        Bitmap image = BitmapFactory.decodeResource(context.getResources(), drawable);
        return image;
    }
}
