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

        if (event == null) {
            return;
        }

        AccessibilityNodeInfo source =
                event.getSource();

        StringBuilder result =
                new StringBuilder();

        // Testo direttamente associato all'evento
        if (event.getText() != null &&
                !event.getText().isEmpty()) {

            result.append("EVENT: ")
                    .append(event.getText());
        }

        // Content description del nodo selezionato
        if (source != null) {

            CharSequence description =
                    source.getContentDescription();

            if (description != null &&
                    description.length() > 0) {

                if (result.length() > 0) {
                    result.append("\n");
                }

                result.append("DESC: ")
                        .append(description);
            }

            CharSequence text =
                    source.getText();

            if (text != null &&
                    text.length() > 0) {

                if (result.length() > 0) {
                    result.append("\n");
                }

                result.append("NODE: ")
                        .append(text);
            }
        }

        if (result.length() == 0) {
            result.append("EVENT RICEVUTO\n")
                    .append("Tipo: ")
                    .append(event.getEventType());
        }

        // Evita troppi messaggi consecutivi
        if (System.currentTimeMillis() - lastToast < 1500) {
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
