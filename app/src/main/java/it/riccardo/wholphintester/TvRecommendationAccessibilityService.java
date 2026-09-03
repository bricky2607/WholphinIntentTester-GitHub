package it.riccardo.wholphintester;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Color;
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

        String text =
                String.valueOf(event.getText());

        String description = "";

        AccessibilityNodeInfo source = event.getSource();

        if (source != null &&
                source.getContentDescription() != null) {

            description =
                    source.getContentDescription().toString();
        }

        // Mostra solo eventi provenienti da altre app,
        // evitando di bombardare lo schermo con eventi nostri.
        if (!packageName.equals(getPackageName())) {

            String message =
                    "APP: " + packageName
                    + "\nTEXT: " + text
                    + "\nDESC: " + description;

            if (System.currentTimeMillis() - lastToast > 1500) {

                Toast toast =
                        Toast.makeText(
                                this,
                                message,
                                Toast.LENGTH_LONG
                        );

                toast.show();

                lastToast =
                        System.currentTimeMillis();
            }
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
