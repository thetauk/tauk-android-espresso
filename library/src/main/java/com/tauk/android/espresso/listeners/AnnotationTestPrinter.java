package com.tauk.android.espresso.listeners;

import androidx.test.internal.runner.listener.InstrumentationRunListener;

import org.junit.runner.Description;

public class AnnotationTestPrinter extends InstrumentationRunListener {

    @Override
    public void testStarted(Description description) throws Exception {
        super.testStarted(description);
        System.out.println("##########$$$$$$$$");
    }


}
