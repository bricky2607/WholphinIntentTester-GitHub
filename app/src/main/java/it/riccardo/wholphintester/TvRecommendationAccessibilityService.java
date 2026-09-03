package it.riccardo.wholphintester;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

public class TvRecommendationAccessibilityService
        extends AccessibilityService {

    private String lastTitle = "";
    private boolean processing = false;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (event == null) {
            return;
        }

        int type = event.getEventType();

        if (type == AccessibilityEvent.TYPE_VIEW_FOCUSED ||
            type == AccessibilityEvent.TYPE_VIEW_SELECTED) {

            AccessibilityNodeInfo node = event.getSource();

            if (node == null) {
                return;
            }

            String title = getTitleFromNode(node);

            if (!title.isEmpty()) {
                lastTitle = title;
            }

            return;
        }

        if (type != AccessibilityEvent.TYPE_VIEW_CLICKED) {
            return;
        }

        AccessibilityNodeInfo node = event.getSource();

        String title = "";

        if (node != null) {
            title = getTitleFromNode(node);
        }

        if (title.isEmpty()) {
            title = lastTitle;
        }

        if (title.isEmpty()) {
            showMessage("TITOLO NON RILEVATO");
            return;
        }

        if (processing) {
            return;
        }

        processing = true;

        final String finalTitle = title;

        showMessage("CERCO:\n" + finalTitle);

        new Thread(() -> {

            try {

                JellyfinApi api =
                        new JellyfinApi(
                                MainActivity.JELLYFIN_URL,
                                MainActivity.JELLYFIN_API_KEY
                        );

                String itemId =
                        api.findItemByTitle(finalTitle);

                if (itemId == null) {

                    showMessage(
                            "NON TROVATO:\n" + finalTitle
                    );

                    processing = false;
                    return;
                }

                showMessage(
                        "TROVATO:\n" + finalTitle
                );

                playInWholphin(itemId);

            } catch (Exception e) {

                showMessage(
                        "ERRORE:\n" + e.getMessage()
                );

            } finally {

                processing = false;
            }

        }).start();
    }

    private String getTitleFromNode(
            AccessibilityNodeInfo node) {

        CharSequence description =
                node.getContentDescription();

        if (description == null) {
            return "";
        }

        String raw =
                description.toString().trim();

        if (raw.isEmpty()) {
            return "";
        }

        return cleanTitle(raw);
    }

    private String cleanTitle(String text) {

        String title = text.trim();

        String lower = title.toLowerCase();

        String[] separators = {
                "richiede l'abbonamento",
                "richiede abbonamento",
                "disponibile con l'abbonamento",
                "disponibile con abbonamento",
                "guarda con l'abbonamento",
                "guarda con abbonamento",
                "acquista",
                "noleggia",
                "noleggio",
                "acquisto",
                "costo"
        };

        int cutIndex = -1;

        for (String separator : separators) {

            int index = lower.indexOf(separator);

            if (index >= 0 &&
                    (cutIndex == -1 || index < cutIndex)) {

                cutIndex = index;
            }
        }

        if (cutIndex >= 0) {
            title = title.substring(0, cutIndex);
        }

        title = title.trim();

        while (title.endsWith(",")) {
            title = title.substring(
                    0,
                    title.length() - 1
            ).trim();
        }

        return title;
    }

    private void playInWholphin(String itemId) {

        try {

            Intent intent =
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(
                                    "wholphin://play?itemId="
                                            + Uri.encode(itemId)
                            )
                    );

            intent.setPackage(
                    "com.github.damontecres.wholphin"
            );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            startActivity(intent);

        } catch (Exception e) {

            showMessage(
                    "ERRORE WHOLPHIN:\n"
                            + e.getMessage()
            );
        }
    }

    private void showMessage(String message) {

        new Handler(
                Looper.getMainLooper()
        ).post(() ->
                Toast.makeText(
                        this,
                        message,
                        Toast.LENGTH_LONG
                ).show()
        );
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
