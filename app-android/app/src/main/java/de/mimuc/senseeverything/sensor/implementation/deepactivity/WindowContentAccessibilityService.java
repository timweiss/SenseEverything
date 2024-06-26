package de.mimuc.senseeverything.sensor.implementation.deepactivity;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class WindowContentAccessibilityService extends AccessibilityService {

    public static final String TAG = "WindowContentAccess.Se.";
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo != null) {
            Log.i(TAG, accessibilityNodeInfo.toString());
        } else {
            Log.i(TAG,"accessibility Node was null");
        }
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i(TAG,"onServiceConnected()");
    }


    @Override
    public void onInterrupt() {
        Log.i(TAG,"onInterrupt()");
    }
}
