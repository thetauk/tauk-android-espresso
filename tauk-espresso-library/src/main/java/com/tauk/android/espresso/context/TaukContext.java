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

package com.tauk.android.espresso.context;


import static androidx.test.platform.app.InstrumentationRegistry.getArguments;

import android.os.Build;

import androidx.test.platform.app.InstrumentationRegistry;

import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.tauk.android.espresso.TaukException;
import com.tauk.android.espresso.Util;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

public class TaukContext {
    private final transient String apiToken;
    private final transient String projectId;
    private transient String apiUrl = "https://www.tauk.com/api/v1/session/upload";
    @Json(name = "test_status")
    private String testStatus;
    @Json(name = "test_name")
    private String testName;
    @Json(name = "test_filename")
    private String testFileName;

    private final Map<String, Object> tags = new HashMap<>();
    private List<LogLine> log;
    private String screenshot;
    private String view;
    private Map<String, Object> error = new HashMap<>();
    @Json(name = "automation_type")
    private final String automationType = "Espresso";
    private final String language = "Java/Kotlin";
    private final String platform = "Android";
    @Json(name = "platform_version")
    private String platformVersion;
    @Json(name = "elapsed_time_ms")
    private long elapsedTime;

    /**
     * Initialize TaukContext
     * Also build tags and collect needed device information
     *
     * @param apiUrl    Tauk API URL
     * @param apiToken  Tauk API Token
     * @param projectId Tauk Project ID
     * @throws TaukException Tauk Exception
     */
    private TaukContext(String apiUrl, String apiToken, String projectId) throws TaukException {
        if (projectId == null || projectId.isEmpty()) {
            throw new TaukException("Tauk Project ID was not specified or invalid");
        }

        if (apiToken == null || apiToken.isEmpty()) {
            throw new TaukException("Tauk API Token was not specified or invalid");
        }

        // TODO: Load api URL from environment variable
        if (apiUrl != null && !apiUrl.isEmpty()) {
            this.apiUrl = apiUrl;
        }
        this.apiToken = apiToken;
        this.projectId = projectId;

        // TODO: Whether to validate API token and Project ID with the server

        buildTags();
        this.setPlatformVersion(Build.VERSION.RELEASE);
    }

    /**
     * Initialize TaukContext with production API URL
     *
     * @param apiToken
     * @param projectId
     * @throws TaukException
     */
    public TaukContext(String apiToken, String projectId) throws TaukException {
        this(null, apiToken, projectId);
    }


    /**
     * Build TaukContext from the provided command line arguments
     *
     * @throws TaukException
     */
    public TaukContext() throws TaukException {
        this(
                getArguments().getString("taukApiUrl"),
                getArguments().getString("taukApiToken"),
                getArguments().getString("taukProjectId")
        );
    }

    private void buildTags() {
        addTag("sdkVersion", Build.VERSION.SDK_INT);
        addTag("manufacturer", Build.MANUFACTURER);
        addTag("model", Build.MODEL);
        addTag("platformName", this.platform);
        addTag("automationType", this.automationType);
        addTag("packageName", InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName());
    }


    public String getTestFileName() {
        return testFileName;
    }

    public void setTestFileName(String testFileName) {
        this.testFileName = testFileName;
    }

    public void setTestStatus(String testStatus) {
        this.testStatus = testStatus;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public void setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public boolean hasScreenshot() {
        return this.screenshot != null && this.screenshot.length() > 0;
    }

    public void setScreenshot(String screenshot) {
        this.screenshot = screenshot;
    }

    public boolean hasViewHierarchy() {
        return this.view != null && this.view.length() > 0;
    }

    public void setViewHierarchy(String view) {
        this.view = view;
    }

    public void addTag(String tagName, Object value) {
        this.tags.put(tagName, value);
    }

    public void setError(String type, String msg, long lineNumber, String invokedFunction, String codeExecuted) {
        error.put("error_type", type);
        error.put("error_msg", msg);
        error.put("line_number", lineNumber);
        error.put("invoked_func", invokedFunction);
        error.put("code_executed", codeExecuted);
    }

    public void setLog(List<LogLine> logLine) {
        this.log = logLine;
    }

    public String toJson() {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<TaukContext> jsonAdapter = moshi.adapter(TaukContext.class);
        return jsonAdapter.toJson(this);
    }

    public void print() {
        Util.logToConsole(toJson());
    }

    public void upload() throws IOException {
        Util.logToConsole("upload: Posting to : [" + apiUrl + "]");
        URL url = new URL(apiUrl);
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(toJson(), JSON);
        Util.logToConsole("upload: Body length : [" + body.contentLength() + "]");


        Buffer buffer = new Buffer();
        body.writeTo(buffer);
//        Util.logToConsole("upload: Body  : [" + buffer.readUtf8()+ "]");
        Util.logToConsole("API_TOKEN: [" + apiToken + "] Project ID: [" + projectId + "]");

        Request request = new Request.Builder()
                .url(url)
                .header("api_token", apiToken)
                .header("project_id", projectId)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        String responseBody = response.body() == null ? "null" : response.body().string();
        Util.logToConsole("upload: Response Body: [" + responseBody + "]");
    }

    public void newTest(String testFileName, String testName) {
        this.setTestFileName(testFileName);
        this.setTestName(testName);
        log = null;
        screenshot = "";
        view = "";
        error = new HashMap<>();
        elapsedTime = 0;
    }
}
