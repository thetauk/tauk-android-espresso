package com.tauk.android.espresso;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import android.app.Instrumentation;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;

import androidx.test.platform.app.InstrumentationRegistry;

import java.io.ByteArrayOutputStream;

public class Util {
    public static void logToConsole(String stringToPrint) {
        Bundle bundle = new Bundle();
        bundle.putString(Instrumentation.REPORT_KEY_STREAMRESULT, "TaukListener: " + stringToPrint + "\n");
        InstrumentationRegistry.getInstrumentation().sendStatus(0, bundle);
    }

    public static String getBase64Bitmap(Bitmap screenshot) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        screenshot.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
    }
}
