package com.tauk.android.espresso;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class ExecutionListener2 extends RunListener {
    /**
     * Called before any tests have been run.
     */
//    @RequiresApi(api = Build.VERSION_CODES.O)
    public void testRunStarted(Description description) throws Exception {
        Log.d("TaukListener", "testRunStarted: " +
                "MethodName: [" + description.getMethodName() + "], " +
                "ClassName: [" + description.getClassName() + "], " +
                "TestCount: [" + description.testCount() + "], " +
                "DisplayName: [" + description.getDisplayName() + "]");
        Log.d("TaukListener", "testRunStarted: BaseOS: " + Build.VERSION.BASE_OS);
        Log.d("TaukListener", "testRunStarted: CODENAME: " + Build.VERSION.CODENAME);
        Log.d("TaukListener", "testRunStarted: RELEASE: " + Build.VERSION.RELEASE);
        Log.d("TaukListener", "testRunStarted: SDK_INT: " + Build.VERSION.SDK_INT);
//        Log.d("TaukListener", "testRunStarted: Serial: "+ Build.getSerial());
        Log.d("TaukListener", "testRunStarted: MANUFACTURER: " + Build.MANUFACTURER);
        Log.d("TaukListener", "testRunStarted: MODEL: " + Build.MODEL);
    }

    /**
     * Called when all tests have finished
     */
    public void testRunFinished(Result result) throws Exception {
        Log.d("TaukListener", "testRunFinished: " +
                "RunCount: [" + result.getRunCount() + "], " +
                "FailureCount: [" + result.getFailureCount() + "], " +
                "IgnoreCount: [" + result.getIgnoreCount() + "], " +
                "Failures: [" + result.getFailures() + "]");

    }

    public void testSuiteStarted(Description description) throws Exception {
        Log.d("TaukListener", "testSuiteStarted: " +
                "MethodName: [" + description.getMethodName() + "], " +
                "ClassName: [" + description.getClassName() + "], " +
                "TestCount: [" + description.testCount() + "], " +
                "DisplayName: [" + description.getDisplayName() + "]");
    }

    public void testSuiteFinished(Description description) throws Exception {
        Log.d("TaukListener", "testSuiteFinished: " +
                "MethodName: [" + description.getMethodName() + "], " +
                "ClassName: [" + description.getClassName() + "], " +
                "TestCount: [" + description.testCount() + "], " +
                "DisplayName: [" + description.getDisplayName() + "]");
    }

    /**
     * Called when an atomic test is about to be started.
     */
    public void testStarted(Description description) throws Exception {
        Log.d("TaukListener", "testStarted: " +
                "MethodName: [" + description.getMethodName() + "], " +
                "ClassName: [" + description.getClassName() + "], " +
                "TestCount: [" + description.testCount() + "], " +
                "DisplayName: [" + description.getDisplayName() + "]");
        description.getTestClass(); // TODO: checkout what this could be used for
    }

    public void testFinished(Description description) throws Exception {
        Log.d("TaukListener", "testFinished: " +
                "MethodName: [" + description.getMethodName() + "], " +
                "ClassName: [" + description.getClassName() + "], " +
                "TestCount: [" + description.testCount() + "], " +
                "DisplayName: [" + description.getDisplayName() + "]");
    }

    public void testFailure(Failure failure) throws IOException {
        Log.d("TaukListener", "testFailure: Message: [" + failure.getMessage() + "]");
        Log.d("TaukListener", "testFailure: TestHeader: [" + failure.getTestHeader() + "]");
        Log.d("TaukListener", "testFailure: Trace: [" + failure.getTrace() + "]");
        Log.d("TaukListener", "testFailure: Description: [" + failure.getDescription() + "]");
        Log.e("TaukListener", "testFailure: Exception: "+  failure.getException().toString());
        Log.d("TaukListener", "testFailure: Class: [" + failure.getClass() + "]");
        Log.d("TaukListener", "testFailure: TrimmedTrace: [" + failure.getTrimmedTrace() + "]");


        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        getBase64Screenshot(device);
        getViewHierarchy(device);
        getLogs(device);

    }

    private String getBase64Screenshot(UiDevice device) throws IOException {
        File file = File.createTempFile("screen", ".png");
        Log.d("TaukListener", "testFailure: Capturing screenshot to: [" + file.getAbsolutePath() + "]");

        device.takeScreenshot(file);

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        String base64Screenshot = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
        Log.d("TaukListener", "testFailure: Base64 screenshot: [" + base64Screenshot + "]");
        return base64Screenshot;
    }

    private String getViewHierarchy(UiDevice device) throws IOException {
        OutputStream out = new ByteArrayOutputStream();
        device.dumpWindowHierarchy(out);
        String hierarchy = out.toString();
        Log.d("TaukListener", "testFailure: View Hierarchy: [" + hierarchy + "]");
        return hierarchy;
    }

    private String getLogs(UiDevice device) throws IOException {
        String logs = device.executeShellCommand("logcat -t 10");
        Log.d("TaukListener", "testFailure: Logcat: [" + logs + "]");
        return logs;
    }

}
