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

package com.tauk.android.espresso.rules;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import androidx.test.espresso.NoMatchingViewException;

import com.tauk.android.espresso.TaukException;
import com.tauk.android.espresso.Util;
import com.tauk.android.espresso.context.TaukContext;
import com.tauk.android.espresso.context.TestStatus;

import org.junit.AssumptionViolatedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.io.IOException;
import java.util.Objects;


public class TaukWatcher extends TestWatcher {
    private final TaukContext taukContext;

    private long testStartTime;

    /**
     * Instantiate TaukWatcher with API URL, API Token, and Project ID passed from command line.
     * When invoking tests from adb you can pass arguments using '-e' parameter.
     * Ex: -e taukApiToken YOUR_API_TOKEN
     *
     * @throws TaukException Tauk Exception
     */
    public TaukWatcher() throws TaukException {
        this.taukContext = new TaukContext();
    }

    /**
     * Instantiate TaukWatcher with custom API URL, API Token, and Project ID.
     *
     * @param apiUrl    Tauk API URL
     * @param apiToken  Tauk API token
     * @param projectId Tauk Project ID
     * @throws TaukException Tauk Exception
     */
    public TaukWatcher(String apiUrl, String apiToken, String projectId) throws TaukException {
        if (apiToken == null || apiToken.isEmpty() || projectId == null || projectId.isEmpty()) {
            this.taukContext = new TaukContext();
        } else {
            this.taukContext = new TaukContext(apiUrl, apiToken, projectId);
        }

    }

    /**
     * Invoked when a test is about to start
     */
    @Override
    protected void starting(Description description) {
        log("[" + description.getDisplayName() + "] starting");
        testStartTime = System.currentTimeMillis();
        taukContext.newTest(description.getClassName(), description.getMethodName());
        super.starting(description);
    }

    /**
     * Invoked when a test method finishes (whether passing or failing)
     */
    @Override
    protected void finished(Description description) {
        log("[" + description.getDisplayName() + "] finished");
        taukContext.setElapsedTime(System.currentTimeMillis() - testStartTime);

        try {
            String screenshot = Util.getBase64Bitmap(getInstrumentation().getUiAutomation().takeScreenshot());
            taukContext.setScreenshot(screenshot);
        } catch (Exception ex) {
            log("Failed to capture screenshot: " + ex.getMessage());
            Util.logExceptionToConsole(ex);
        }

        try {
            taukContext.upload();
        } catch (Exception ex) {
            logException("Failed to upload results: " + ex.getMessage(), ex);
        }
        super.finished(description);
    }

    /**
     * Invoked when a test succeeds
     */
    @Override
    protected void succeeded(Description description) {
        log("[" + description.getDisplayName() + "] succeeded");
        taukContext.setTestStatus(TestStatus.PASSED.value);
        super.succeeded(description);
    }

    /**
     * Invoked when a test fails
     */
    @Override
    protected void failed(Throwable e, Description description) {
        log("[" + description.getDisplayName() + "] failed");
        taukContext.setTestStatus(TestStatus.FAILED.value);
        try {
            taukContext.setViewHierarchy(Util.getViewHierarchyFromWindow());
        } catch (Exception ex) {
            logException("Failed to capture view hierarchy: " + ex.getMessage(), ex);
        }

        buildError(e);
        try {
            taukContext.setLog(Util.getLogs());
        } catch (IOException ex) {
            logException("Failed to fetch device logs: " + ex.getMessage(), ex);
        }

        super.failed(e, description);
    }

    /**
     * Invoked when a test is skipped due to a failed assumption.
     */
    @Override
    protected void skipped(AssumptionViolatedException e, Description description) {
        log("[" + description.getDisplayName() + "] skipped");
        super.skipped(e, description);
    }


    //#############################################################################################
    //######### Private Methods ###################################################################
    //#############################################################################################

    private void log(String msg) {
        Util.logToConsole("TaukWatcher", msg);
    }

    private void logException(String msg, Exception ex) {
        log(msg);
        Util.logExceptionToConsole(ex);
    }

    /**
     * Construct Tauk Error from Throwable
     *
     * @param e Throwable
     */
    private void buildError(Throwable e) {
        String errorMessage = "";
        long lineNumber = -1;
        String invokedFunctionName = "";
        String codeExecuted = "";
        StackTraceElement[] stackTrace = e.getStackTrace();
        for (StackTraceElement trace : stackTrace) {
            if (trace.getClassName().equals(taukContext.getTestFileName())) {
                lineNumber = trace.getLineNumber();
                invokedFunctionName = trace.getMethodName();
                codeExecuted = trace.toString();
                break;
            }
        }

        if (e.getClass() == NoMatchingViewException.class) {
            errorMessage = Objects.requireNonNull(e.getMessage())
                    .substring(0, e.getMessage().indexOf("View Hierarchy:"))
                    .trim();
        }

        taukContext.setError(
                e.getClass().getSimpleName(),
                errorMessage,
                lineNumber,
                invokedFunctionName,
                codeExecuted
        );
    }


}
