package com.tauk.android.espresso;


import android.app.Instrumentation;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.os.Build;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import com.tauk.android.espresso.context.TaukContext;
import com.tauk.android.espresso.context.TestStatus;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ExecutionListener extends RunListener {
    private TaukContext taukContext;
    private UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    private long testStartTime;
    private long testFinishedTime;

    public ExecutionListener() {
        super();
    }

    @Override
    public void testSuiteStarted(Description description) throws Exception {
        super.testSuiteStarted(description);
    }

    @Override
    public void testSuiteFinished(Description description) throws Exception {
        super.testSuiteFinished(description);
    }

    /**
     * Called before any tests have been run.
     */
    public void testRunStarted(Description description) throws Exception {
        super.testRunStarted(description);

        // TODO: Validate api token and project
        // TODO: Exception handling
        Util.logToConsole("TaukListener testRunStarted: ******************");
        String apiToken = InstrumentationRegistry.getArguments().getString("apiToken");
        String projectId = InstrumentationRegistry.getArguments().getString("projectId");
        taukContext = new TaukContext(apiToken, projectId);

        taukContext.addTag("releaseVersion", Build.VERSION.RELEASE);
        taukContext.addTag("sdkVersion", Build.VERSION.SDK_INT);
        taukContext.addTag("manufacturer", Build.MANUFACTURER);
        taukContext.addTag("model", Build.MODEL);

        taukContext.addTag("productName", device.getProductName());

    }

    /**
     * Called when all tests have finished
     */
    public void testRunFinished(Result result) throws java.lang.Exception {
        taukContext.setElapsedTime(testFinishedTime - testStartTime);

//        Log.d("TaukListener", "testRunFinished: " + taukContext.toJson());
        Log.d("TaukListener", "testRunFinished: ********* DONE *********");

//        taukContext.upload();
    }

    /**
     * Called when an atomic test is about to be started.
     */
    public void testStarted(Description description) throws java.lang.Exception {
        testStartTime = System.currentTimeMillis();
        taukContext.setTestFileName("io.aj.sample.ExampleInstrumentedTest");
        taukContext.setTestName("useAppContext");
    }

    /**
     * Called when an atomic test has finished, whether the test succeeds or fails.
     */
    public void testFinished(Description description) throws Exception {
        testFinishedTime = System.currentTimeMillis();
    }

    /**
     * Called when an atomic test fails, or when a listener throws an exception.
     */
    public void testFailure(Failure failure) throws IOException {
        taukContext.setTestStatus(TestStatus.FAILED);

        String[] failureMessage = failure.getMessage().split("\\r?\\n|\\r", 2);
        String failureDescription = failureMessage[0];

        long lineNumber = -1;
        String invokedFunctionName = "";
        String codeExecuted = "";
        StackTraceElement[] stackTrace = failure.getException().getStackTrace();
        for (StackTraceElement trace : stackTrace) {
            if (trace.getClassName().equals(taukContext.getTestFileName())) {
                lineNumber = trace.getLineNumber();
                invokedFunctionName = trace.getMethodName();
                codeExecuted = trace.toString();
                break;
            }
        }


        taukContext.setError(
                failure.getException().getClass().getSimpleName(),
                failureDescription,
                lineNumber,
                invokedFunctionName,
                codeExecuted
        );

        taukContext.setScreenshot(getBase64Screenshot());
        taukContext.setViewHierarchy(getViewHierarchy());
        taukContext.setLog(getLogs());

    }

    /**
     * Called when an atomic test flags that it assumes a condition that is false
     */
    public void testAssumptionFailure(Failure failure) {
        taukContext.setTestStatus(TestStatus.FAILED);
    }

    /**
     * Called when a test will not be run, generally because a test method is annotated with
     * org.junit.Ignore.
     */
    public void testIgnored(Description description) throws IOException {
        taukContext.setTestStatus(TestStatus.EXCLUDED);
    }


    /*************************** private methods ***************************/


    private String getBase64Screenshot() throws IOException {
        File file = File.createTempFile("screen", ".png");
        Log.d("TaukListener", "testFailure: Capturing screenshot to: [" + file.getAbsolutePath() + "]");

        device.takeScreenshot(file);

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        String base64Screenshot = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
//        Log.d("TaukListener", "testFailure: Base64 screenshot: [" + base64Screenshot + "]");
        return base64Screenshot;
    }

    private String getViewHierarchy() throws IOException {
        OutputStream out = new ByteArrayOutputStream();
        device.dumpWindowHierarchy(out);
        String hierarchy = out.toString();
//        Log.d("TaukListener", "testFailure: View Hierarchy: [" + hierarchy + "]");
        return hierarchy;
    }

    private List<com.tauk.android.espresso.context.Log> getLogs() throws IOException {
        ArrayList<com.tauk.android.espresso.context.Log> logs = new ArrayList<>();
        String stringLogs = device.executeShellCommand("logcat -v epoch brief -t 3");
//        Log.d("TaukListener", "testFailure: Logcat: [" + stringLogs + "]");

        BufferedReader bufReader = new BufferedReader(new StringReader(stringLogs));
        String line = null;
        while ((line = bufReader.readLine()) != null) {
            logs.add(parseLogLine(line));
        }
        return logs;
    }

    private com.tauk.android.espresso.context.Log parseLogLine(String line) {
        com.tauk.android.espresso.context.Log log = new com.tauk.android.espresso.context.Log();
        Pattern pattern = Pattern.compile("\\s([0-9]+\\.[0-9]+)\\s+(\\d*)\\s+(\\d*)\\s+([DWEVI])\\s+(.+)");
        Matcher matcher = pattern.matcher(line);
//        Log.d("TaukListener", "testFailure: LogLine: [" + line + "]");
        if (matcher.find()) {
            if (matcher.groupCount() == 5) {
                log.setTimestamp(Double.valueOf(Double.parseDouble(matcher.group(1))).longValue());
                log.setLevel(matcher.group(4));
                log.setType("Logcat");
                log.setMessage(matcher.group(5));
            } else {
                Log.d("TaukListener", "testFailure: Not considering log line: " + line);

            }
        }
        return log;
    }



}
