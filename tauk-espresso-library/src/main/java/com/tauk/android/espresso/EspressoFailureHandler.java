/*
 * MIT License
 *
 * Copyright (c) 2022 Tauk, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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

public class EspressoFailureHandler implements FailureHandler {

    private final FailureHandler delegate;
    private final TaukContext context;

    public EspressoFailureHandler(@NonNull Instrumentation instrumentation, TaukContext context) {
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