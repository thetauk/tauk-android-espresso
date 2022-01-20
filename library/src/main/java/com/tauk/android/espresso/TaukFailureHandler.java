package com.tauk.android.espresso;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import android.app.Instrumentation;
import android.graphics.Bitmap;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.test.espresso.FailureHandler;
import androidx.test.espresso.base.DefaultFailureHandler;
import androidx.test.uiautomator.UiDevice;

import com.tauk.android.espresso.context.TaukContext;

import org.hamcrest.Matcher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class TaukFailureHandler implements FailureHandler {

    private final FailureHandler delegate;
    private final TaukContext context;

    public TaukFailureHandler(@NonNull Instrumentation instrumentation, TaukContext context) {
        this.context = context;
        delegate = new DefaultFailureHandler(instrumentation.getTargetContext());
    }

    @Override
    public void handle(Throwable error, Matcher<View> viewMatcher) {
        // Capture screenshot and view hierarchy
        captureScreenshot();
        captureViewHierarchy();

        // Then delegate the error handling to the default handler which will throw an exception
        delegate.handle(error, viewMatcher);
    }

    private void captureScreenshot() {
        Bitmap screenshot = getInstrumentation().getUiAutomation().takeScreenshot();
        context.setScreenshot(Util.getBase64Bitmap(screenshot));
    }

    private void captureViewHierarchy() {
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        OutputStream out = new ByteArrayOutputStream();
        try {
            device.dumpWindowHierarchy(out);
        } catch (IOException e) {
            Util.logToConsole("ERROR: Failed to capture view hierarchy");
        }
        context.setViewHierarchy(out.toString());
    }
}