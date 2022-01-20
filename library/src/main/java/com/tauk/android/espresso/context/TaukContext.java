package com.tauk.android.espresso.context;


import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
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

public class TaukContext {
//        static final String UPLOAD_API_URL = "https://www.tauk.com/api/v1/session/upload";
//        static final String UPLOAD_API_URL = "http://10.0.2.2:5000/api/v1/session/upload";
//    static final String UPLOAD_API_URL = "https://requestinspector.com/p/01fsns8dtycaxsfn1e62w071qr";

    private transient String apiUrl;
    private transient String apiToken;
    private transient String projectId;

    @Json(name = "test_status")
    private String testStatus = TestStatus.PASSED.value;

    @Json(name = "test_name")
    private String testName;
    @Json(name = "test_filename")
    private String testFileName;
    private Map<String, Object> tags = new HashMap<>();
    private List<Log> log;
    private String screenshot;
    private String view;
    private Map<String, Object> error = new HashMap<>();
    @Json(name = "automation_type")
    private String automationType = "Espresso";
    private String language = "Java/Kotlin";
    private String platform = "Android";
    @Json(name = "platform_version")
    private String platformVersion;
    @Json(name = "elapsed_time_ms")
    private long elapsedTime;

    public TaukContext(String apiUrl, String apiToken, String projectId) {

        this.apiUrl = apiUrl;
        this.apiToken = apiToken;
        this.projectId = projectId;
    }

    public String getTestFileName() {
        return testFileName;
    }

    public void setTestStatus(String testStatus) {
        this.testStatus = testStatus;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public void setTestFileName(String testFileName) {
        this.testFileName = testFileName;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public void setScreenshot(String screenshot) {
        this.screenshot = screenshot;
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

    public void setLog(List<Log> log) {
        this.log = log;
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
        try {
            Util.logToConsole("upload: Posting to : [" + apiUrl + "]");
            URL url = new URL(apiUrl);
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(toJson(), JSON);
            Util.logToConsole("upload: Body length : [" + body.contentLength() + "]");
            Request request = new Request.Builder()
                    .url(url)
                    .header("api_token", apiToken)
                    .header("project_id", projectId)
                    .post(body)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                Util.logToConsole("upload: Response Body: [" + response.body().string() + "]");
            }
        } catch (Exception e) {
            Util.logToConsole("upload ERROR: " + e.getMessage());
        }
    }
}
