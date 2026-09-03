package it.riccardo.wholphintester;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

public class TvRecommendationAccessibilityService
        extends AccessibilityService {

    private long lastToast = 0;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (event == null) return;

        String packageName =
                String.valueOf(event.getPackageName());

        // Ci interessano gli eventi del launcher Google TV
        if (!"com.android.google.apps.tv.launcherx"
                .equals(packageName)) {
            return;
        }

        AccessibilityNodeInfo root =
                getRootInActiveWindow();

        if (root == null) return;

        StringBuilder result =
                new StringBuilder();

        collectText(root, result);

        if (result.length() == 0) {
            return;
        }

        if (System.currentTimeMillis() - lastToast < 2000) {
            return;
        }

        Toast.makeText(
                this,
                result.toString(),
                Toast.LENGTH_LONG
        ).show();

        lastToast =
                System.currentTimeMillis();
    }

    private void collectText(
            AccessibilityNodeInfo node,
            StringBuilder result) {

        if (node == null) return;

        CharSequence text = node.getText();

        if (text != null) {

            String value =
                    text.toString().trim();

            if (!value.isEmpty()) {

                if (result.length() > 0) {
                    result.append("\n");
                }

                result.append("TEXT: ")
                      .append(value);
            }
        }

        CharSequence description =
                node.getContentDescription();

        if (description != null) {

            String value =
                    description.toString().trim();

            if (!value.isEmpty()) {

                if (result.length() > 0) {
                    result.append("\n");
                }

                result.append("DESC: ")
                      .append(value);
            }
        }

        for (int i = 0;
             i < node.getChildCount();
             i++) {

            AccessibilityNodeInfo child =
                    node.getChild(i);

            collectText(child, result);
        }
    }

    @Override
    protected void onServiceConnected() {

        super.onServiceConnected();

        Toast.makeText(
                this,
                "WHOLPHIN BRIDGE ATTIVO",
                Toast.LENGTH_LONG
        ).show();
    }

    @Override
    public void onInterrupt() {
    }
        }
