package com.tauk.android.espresso.listeners;


import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static androidx.test.platform.app.InstrumentationRegistry.getArguments;

import android.os.Build;
import android.view.View;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.util.HumanReadables;
import androidx.test.uiautomator.UiDevice;

import com.tauk.android.espresso.EspressoFailureHandler;
import com.tauk.android.espresso.Util;
import com.tauk.android.espresso.context.TaukContext;
import com.tauk.android.espresso.context.TestStatus;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Objects;


public class ExecutionListener extends RunListener {
    private final String DEFAULT_API_URL = "https://www.tauk.com/api/v1/session/upload";
    private TaukContext taukContext;
    private final UiDevice device = UiDevice.getInstance(getInstrumentation());
    private long testStartTime;

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
    public void testRunStarted(Description description) {
        try {
            Util.logToConsole("### testRunStarted: ----------------------------");
            super.testRunStarted(description);

            String apiUrl = getArguments().getString("taukApiUrl", DEFAULT_API_URL);
            String projectId = getArguments().getString("taukProjectId");
            String apiToken = getArguments().getString("taukApiToken");
            taukContext = new TaukContext(apiUrl, apiToken, projectId);

            EspressoFailureHandler failureHandler = new EspressoFailureHandler(getInstrumentation(), taukContext);
            Espresso.setFailureHandler(failureHandler);

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
    public void testRunFinished(Result result) {
        try {
            Util.logToConsole("### testRunFinished: ----------------------------");
        } catch (Exception e) {
            Util.logToConsole("testRunFinished ERROR: " + e.getMessage());
        }
    }

    /**
     * Called when an atomic test is about to be started.
     */
    public void testStarted(Description description) {
        try {
            testStartTime = System.currentTimeMillis();
            Util.logToConsole("### testStarted[" + description.getDisplayName() + "]: ----------------------------");
            taukContext.newTest(description.getClassName(), description.getMethodName());
        } catch (Exception e) {
            Util.logToConsole("testStarted ERROR: " + e.getMessage());
        }
    }

    /**
     * Called when an atomic test has finished, whether the test succeeds or fails.
     */
    public void testFinished(Description description) {
        try {
            if (!taukContext.hasScreenshot()) {
                taukContext.setScreenshot(Util.getBase64Bitmap(getInstrumentation().getUiAutomation().takeScreenshot()));
            }
            if (!taukContext.hasViewHierarchy()) {
                taukContext.setViewHierarchy(getViewHierarchyFromWindow());
            }

            Util.logToConsole("### testFinished[" + description.getDisplayName() + "]: ----------------------------");
            taukContext.setElapsedTime(System.currentTimeMillis() - testStartTime);
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
            Util.logToConsole("### testFailure[" + failure.getDescription() + "]: ----------------------------");
            taukContext.setTestStatus(TestStatus.FAILED.value);

            parseAndSetError(failure);
            taukContext.setLog(Util.getLogs());
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
    public void testIgnored(Description description) {
        Util.logToConsole("### testIgnored: ----------------------------");
    }

    //#############################################################################################
    //######### Private Methods ###################################################################
    //#############################################################################################


    private String getViewHierarchyFromWindow() throws IOException {
        OutputStream out = new ByteArrayOutputStream();
        device.dumpWindowHierarchy(out);
        return out.toString();
    }

    private String getViewHierarchyFromException(Throwable t) throws NoSuchFieldException, IllegalAccessException {
        //TODO add more check to get type of exception and handle it better
        Field f = ((NoMatchingViewException) t).getClass().getDeclaredField("rootView");
        f.setAccessible(true);
        View rootView = (View) f.get(t);
        return HumanReadables.getViewHierarchyErrorMessage(
                Objects.requireNonNull(rootView), null, "", null);
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


}
