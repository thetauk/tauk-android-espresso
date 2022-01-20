package com.tauk.android.espresso;


import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import android.graphics.Bitmap;
import android.util.Base64;
import android.os.Build;
import android.view.View;

import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.util.HumanReadables;
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
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ExecutionListener extends RunListener {
    private TaukContext taukContext;
    private UiDevice device = UiDevice.getInstance(getInstrumentation());
    private long testStartTime;
    private long testFinishedTime;

    public ExecutionListener() {
        super();
    }

    @Override
    public void testSuiteStarted(Description description) throws Exception {
        super.testSuiteStarted(description);
        Util.logToConsole("### testSuiteStarted: ----------------------------");
    }

    @Override
    public void testSuiteFinished(Description description) throws Exception {
        super.testSuiteFinished(description);
        Util.logToConsole("### testSuiteFinished: ----------------------------");
    }

    /**
     * Called before any tests have been run.
     */
    public void testRunStarted(Description description) throws Exception {
        try {
            Util.logToConsole("### testRunStarted: ----------------------------");
            super.testRunStarted(description);

            // TODO: Validate api token and project
            String taukApiUrl = InstrumentationRegistry.getArguments().getString("taukApiUrl");
            String projectId = InstrumentationRegistry.getArguments().getString("projectId");
            String apiToken = InstrumentationRegistry.getArguments().getString("apiToken");
            taukContext = new TaukContext(apiToken, projectId, taukApiUrl);

            taukContext.addTag("releaseVersion", Build.VERSION.RELEASE);
            taukContext.addTag("sdkVersion", Build.VERSION.SDK_INT);
            taukContext.addTag("manufacturer", Build.MANUFACTURER);
            taukContext.addTag("model", Build.MODEL);

            taukContext.addTag("productName", device.getProductName());
        } catch (Exception e) {
            Util.logToConsole("testRunStarted ERROR: " + e.getMessage());
        }

    }

    /**
     * Called when all tests have finished
     */
    public void testRunFinished(Result result) throws java.lang.Exception {
        try {
            Util.logToConsole("### testRunFinished: ----------------------------");
        } catch (Exception e) {
            Util.logToConsole("testRunFinished ERROR: " + e.getMessage());
        }
    }

    /**
     * Called when an atomic test is about to be started.
     */
    public void testStarted(Description description) throws java.lang.Exception {
        try {
            Util.logToConsole("### testStarted: ----------------------------");
            testStartTime = System.currentTimeMillis();
            taukContext.setTestFileName(description.getClassName());
            taukContext.setTestName(description.getMethodName());
        } catch (Exception e) {
            Util.logToConsole("testStarted ERROR: " + e.getMessage());
        }
    }

    /**
     * Called when an atomic test has finished, whether the test succeeds or fails.
     */
    public void testFinished(Description description) throws Exception {
        try {
            Util.logToConsole("### testFinished: ----------------------------");
            testFinishedTime = System.currentTimeMillis();
            taukContext.setElapsedTime(testFinishedTime - testStartTime);
            taukContext.print();
            taukContext.upload();
        } catch (Exception e) {
            Util.logToConsole("testFinished ERROR: " + e.getMessage());
        }
    }

    /**
     * Called when an atomic test fails, or when a listener throws an exception.
     */
    public void testFailure(Failure failure) {
        try {
            Util.logToConsole("### testFailure: ----------------------------" + failure.getTestHeader());
            taukContext.setTestStatus(TestStatus.FAILED.value);

            // Fetch the rootView from NoMatchingViewException.
            // Since its a private variable we have to set it to accessible
            try {
                taukContext.setViewHierarchy(getViewHierarchyFromException(failure.getException()));
            } catch (Exception e) {
                Util.logToConsole("Failed to extract view hierarchy from exception");
            }

            parseAndSetError(failure);
            taukContext.setScreenshot(getBase64Screenshot());
            taukContext.setLog(getLogs());

        } catch (Exception e) {
            Util.logToConsole("testFailure ERROR: " + e.getMessage());
        }
    }

    /**
     * Called when an atomic test flags that it assumes a condition that is false
     */
    public void testAssumptionFailure(Failure failure) {
        try {
            Util.logToConsole("### testAssumptionFailure: ----------------------------");
            taukContext.setTestStatus(TestStatus.FAILED.value);
        } catch (Exception e) {
            Util.logToConsole("testAssumptionFailure ERROR: " + e.getMessage());
        }
    }

    /**
     * Called when a test will not be run, generally because a test method is annotated with
     * org.junit.Ignore.
     */
    public void testIgnored(Description description) throws IOException {
        Util.logToConsole("### testIgnored: ----------------------------");
    }


    private String getBase64Screenshot() {
        Bitmap screenshot = getInstrumentation().getUiAutomation().takeScreenshot();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        screenshot.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
    }

    private String getViewHierarchyFromWindow() throws IOException {
        OutputStream out = new ByteArrayOutputStream();
        device.dumpWindowHierarchy(out);
        String hierarchy = out.toString();
        return hierarchy;
    }

    private String getViewHierarchyFromException(Throwable t) throws NoSuchFieldException, IllegalAccessException {
        //TODO add more check to get type of exception and handle it better
        Field f = ((NoMatchingViewException) t).getClass().getDeclaredField("rootView");
        f.setAccessible(true);
        View rootView = (View) f.get(t);
        return HumanReadables.getViewHierarchyErrorMessage(rootView, null, "", null);
    }

    private void parseAndSetError(Failure failure) {
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
    }


    private List<com.tauk.android.espresso.context.Log> getLogs() throws IOException {
        ArrayList<com.tauk.android.espresso.context.Log> logs = new ArrayList<>();
        String stringLogs = device.executeShellCommand("logcat -v epoch brief -t 15");
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
        if (matcher.find()) {
            if (matcher.groupCount() == 5) {
                log.setTimestamp(Double.valueOf(Double.parseDouble(matcher.group(1))).longValue());
                log.setLevel(matcher.group(4));
                log.setType("Logcat");
                log.setMessage(matcher.group(5));
            } else {
                Util.logToConsole("testFailure: Not considering log line: " + line);

            }
        }
        return log;
    }

}
