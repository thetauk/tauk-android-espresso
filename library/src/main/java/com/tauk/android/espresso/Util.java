package com.tauk.android.espresso;

import android.app.Instrumentation;
import android.os.Bundle;

import androidx.test.platform.app.InstrumentationRegistry;

public class Util {
    public static void logToConsole(String stringToPrint) {
        Bundle bundle = new Bundle();
        bundle.putString(Instrumentation.REPORT_KEY_STREAMRESULT, stringToPrint);
        InstrumentationRegistry.getInstrumentation().sendStatus(0, bundle);
    }
}
