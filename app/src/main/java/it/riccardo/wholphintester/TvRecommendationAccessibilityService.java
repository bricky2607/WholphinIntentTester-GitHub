package it.riccardo.wholphintester;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.net.Uri;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

public class TvRecommendationAccessibilityService
        extends AccessibilityService {

    private String lastTitle = "";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (event == null) return;

        int type = event.getEventType();

        if (type != AccessibilityEvent.TYPE_VIEW_FOCUSED &&
            type != AccessibilityEvent.TYPE_VIEW_SELECTED &&
            type != AccessibilityEvent.TYPE_VIEW_CLICKED) {
            return;
        }

        AccessibilityNodeInfo node = event.getSource();

        if (node == null) return;

        CharSequence description =
                node.getContentDescription();

        if (description == null) return;

        String raw = description.toString().trim();

        if (raw.isEmpty()) return;

        String title = cleanTitle(raw);

        if (title.isEmpty()) return;

        if (title.equals(lastTitle)) return;

        lastTitle = title;

        Toast.makeText(
                this,
                "FILM:\n" + title,
                Toast.LENGTH_LONG
        ).show();

        // Per ora NON apriamo Wholphin.
        // Abbiamo appena verificato che il titolo
        // viene estratto correttamente.
    }

    private String cleanTitle(String text) {

        String title = text;

        // Rimuove informazioni sul prezzo
        int priceIndex =
                title.toLowerCase().indexOf("costo");

        if (priceIndex >= 0) {
            title = title.substring(0, priceIndex);
        }

        // Rimuove eventuali informazioni dopo una virgola
        int commaIndex = title.indexOf(",");

        if (commaIndex >= 0) {
            title = title.substring(0, commaIndex);
        }

        return title.trim();
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
