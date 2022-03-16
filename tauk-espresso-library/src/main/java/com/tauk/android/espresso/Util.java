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
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import com.tauk.android.espresso.context.LogLine;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    private static final UiDevice device = UiDevice.getInstance(getInstrumentation());

    public static void logToConsole(String text) {

        Bundle bundle = new Bundle();
        bundle.putString(Instrumentation.REPORT_KEY_STREAMRESULT, "Tauk: " + text + "\n");
        InstrumentationRegistry.getInstrumentation().sendStatus(0, bundle);
    }

    public static void logToConsole(String prefix, String text) {
        logToConsole(prefix + ": " + text);
    }

    public static void logExceptionToConsole(Exception ex) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        ex.printStackTrace(printWriter);
        logToConsole(stringWriter.toString());
    }

    public static String getBase64Bitmap(Bitmap screenshot) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        screenshot.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
    }

    public static String getViewHierarchyFromWindow() throws IOException {
        OutputStream out = new ByteArrayOutputStream();
        device.dumpWindowHierarchy(out);
        return out.toString();
    }

    public static List<LogLine> getLogs() throws IOException {
        ArrayList<LogLine> logLines = new ArrayList<>();
        String stringLogs = device.executeShellCommand("logcat -v epoch brief -t 15");
        BufferedReader bufReader = new BufferedReader(new StringReader(stringLogs));
        String line;
        while ((line = bufReader.readLine()) != null) {
            logLines.add(parseLogLine(line));
        }
        return logLines;
    }

    private static LogLine parseLogLine(String line) {
        Log.d("tauk", line);
        LogLine logLine = new LogLine();
        Pattern pattern = Pattern.compile("\\s([0-9]+\\.[0-9]+)\\s+(\\d*)\\s+(\\d*)\\s+([DWEVI])\\s+(.+)");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            if (matcher.groupCount() == 5) {
                logLine.setTimestamp(Double.valueOf(
                        Double.parseDouble(
                                Objects.requireNonNull(
                                        matcher.group(1)
                                )
                        )
                ).longValue());
                logLine.setLevel(matcher.group(4));
                logLine.setType("Logcat");
                logLine.setMessage(matcher.group(5));
            } else {
                Util.logToConsole("testFailure: Not considering log line: " + line);

            }
        }
        return logLine;
    }
}
