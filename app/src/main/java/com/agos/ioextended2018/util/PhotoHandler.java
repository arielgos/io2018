package com.agos.ioextended2018.util;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Environment;
import android.util.Log;

import com.agos.ioextended2018.App;
import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.io.FileOutputStream;

public class PhotoHandler implements PictureCallback {

    private final Context context;
    private PictureSavedCallback callback;

    public PhotoHandler(Context context, PictureSavedCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        File pictureFile = new File(getFileName());
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
            this.callback.callback();
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.log(Log.ERROR, App.tag, e.getMessage());
            Crashlytics.logException(e);
        }
    }

    public static File getDir() {
        File sdDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(sdDir, "mlkit");
    }

    public static String getFileName() {
        File pictureFileDir = getDir();
        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {
            return "";
        }
        return pictureFileDir.getPath() + File.separator + "mlkit.jpg";
    }
}