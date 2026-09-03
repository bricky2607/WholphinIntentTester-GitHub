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

    // USA GLI STESSI VALORI CHE HAI GIÀ NEL TUO JELLYFINAPI
    private static final String JELLYFIN_URL =
            "https://jellybrick.duckdns.org";

    private static final String JELLYFIN_API_KEY =
            "INCOLLA_QUI_LA_STESSA_API_KEY";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (event == null) {
            return;
        }

        int type = event.getEventType();

        if (type != AccessibilityEvent.TYPE_VIEW_FOCUSED &&
            type != AccessibilityEvent.TYPE_VIEW_SELECTED &&
            type != AccessibilityEvent.TYPE_VIEW_CLICKED) {
            return;
        }

        AccessibilityNodeInfo node =
                event.getSource();

        if (node == null) {
            return;
        }

        CharSequence description =
                node.getContentDescription();

        if (description == null) {
            return;
        }

        String raw =
                description.toString().trim();

        if (raw.isEmpty()) {
            return;
        }

        String title =
                cleanTitle(raw);

        if (title.isEmpty()) {
            return;
        }

        if (title.equals(lastTitle)) {
            return;
        }

        lastTitle = title;

        Toast.makeText(
                this,
                "CERCO:\n" + title,
                Toast.LENGTH_SHORT
        ).show();

        new Thread(() -> {

            try {

                JellyfinApi api =
                        new JellyfinApi(
                                JELLYFIN_URL,
                                JELLYFIN_API_KEY
                        );

                String itemId =
                        api.findItemByTitle(title);

                if (itemId == null) {

                    showMessage(
                            "NON TROVATO:\n" + title
                    );

                    return;
                }

                showMessage(
                        "TROVATO:\n" + title
                );

                playInWholphin(itemId);

            } catch (Exception e) {

                showMessage(
                        "ERRORE:\n"
                        + e.getMessage()
                );
            }

        }).start();
    }

    private String cleanTitle(String text) {

        String title = text;

        int priceIndex =
                title.toLowerCase()
                        .indexOf("costo");

        if (priceIndex >= 0) {
            title =
                    title.substring(
                            0,
                            priceIndex
                    );
        }

        int commaIndex =
                title.indexOf(",");

        if (commaIndex >= 0) {
            title =
                    title.substring(
                            0,
                            commaIndex
                    );
        }

        return title.trim();
    }

    private void playInWholphin(String itemId) {

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
    }

    private void showMessage(String message) {

        new android.os.Handler(
                android.os.Looper.getMainLooper()
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
