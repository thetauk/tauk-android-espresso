package com.tauk.android.espresso.context;


import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
        static final String UPLOAD_API_URL = "http://localhost:5000/api/v1/session/upload";
//    static final String UPLOAD_API_URL = "https://requestinspector.com/inspect/01fgtf646m76k9q9tp1z9yyqt0";

    private transient String apiToken;
    private transient String projectId;

    @Json(name = "test_status")
    private TestStatus testStatus;
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

    public TaukContext(String apiToken, String projectId) {
        this.apiToken = apiToken;
        this.projectId = projectId;
    }

    public String toJson() {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<TaukContext> jsonAdapter = moshi.adapter(TaukContext.class);
        return jsonAdapter.toJson(this);
    }

    public void longDebug(String str) {
        if (str.length() > 4000) {
            android.util.Log.d("TaukListener", "test: Post Body : " + str.substring(0, 4000));
            longDebug(str.substring(4000));
        } else
            android.util.Log.d("TaukListener", "test: Post Body : " + str);
    }

    public void upload() throws IOException {
        android.util.Log.d("TaukListener", "test: Posting to : [" + UPLOAD_API_URL + "]");


//        longDebug(toJson());
//        screenshot = screenshot.substring(0, 100);
//        view = view.substring(0, 100);

        URL url = new URL(UPLOAD_API_URL);
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(toJson(), JSON);
        android.util.Log.d("TaukListener", "test: Body length : [" + body.contentLength() + "]");
        Request request = new Request.Builder()
                .url(url)
                .header("api_token", apiToken)
                .header("project_id", projectId)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            android.util.Log.d("TaukListener", "test: Response Body: [" + response.body().string() + "]");
        }
    }

    public String getTestFileName() {
        return testFileName;
    }

    public void setTestStatus(TestStatus testStatus) {
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
}
