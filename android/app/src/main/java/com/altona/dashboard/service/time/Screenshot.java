package com.altona.dashboard.service.time;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;
import java.io.FileOutputStream;

public class Screenshot {

    private String base64;

    public Screenshot(
            @JsonProperty(value = "base64", required = true) String base64
    ) {
        this.base64 = base64;
    }

    public void save(Context context, String attemptId) {
        byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);

        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        File directory = new File(root + "/Dashboard/Synchronizations");
        directory.mkdirs();

        File file = new File(directory, attemptId + ".jpg");
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        context.sendBroadcast(intent);
    }

}
