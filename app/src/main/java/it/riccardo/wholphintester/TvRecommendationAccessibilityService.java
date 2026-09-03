package it.riccardo.wholphintester;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class TvRecommendationAccessibilityService
        extends AccessibilityService {

    private static final String TAG = "WholphinBridge";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (event == null) {
            return;
        }

        Log.d(TAG,
                "EVENT type=" + event.getEventType()
                + " package=" + event.getPackageName()
                + " class=" + event.getClassName()
                + " text=" + event.getText());

        AccessibilityNodeInfo source = event.getSource();

        if (source != null) {
            inspectNode(source);
        }
    }

    private void inspectNode(AccessibilityNodeInfo node) {

        if (node == null) {
            return;
        }

        CharSequence text = node.getText();
        CharSequence description = node.getContentDescription();

        if (text != null || description != null) {

            Log.d(TAG,
                    "NODE text=" + text
                    + " description=" + description
                    + " class=" + node.getClassName());
        }

        for (int i = 0; i < node.getChildCount(); i++) {

            AccessibilityNodeInfo child =
                    node.getChild(i);

            if (child != null) {
                inspectNode(child);
            }
        }
    }

    @Override
    protected void onServiceConnected() {

        super.onServiceConnected();

        Log.d(TAG,
                "WholphinBridge Accessibility Service CONNECTED");
    }

    @Override
    public void onInterrupt() {

        Log.d(TAG,
                "WholphinBridge Accessibility Service INTERRUPTED");
    }
        }
