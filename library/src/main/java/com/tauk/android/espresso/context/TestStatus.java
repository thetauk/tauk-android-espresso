package com.tauk.android.espresso.context;

public enum TestStatus {
    PASSED("passed"),
    FAILED("failed"),
    EXCLUDED("excluded"); // TODO: change to undetermined

    public final String value;

    private TestStatus(String val) {
        this.value = val;
    }
}
