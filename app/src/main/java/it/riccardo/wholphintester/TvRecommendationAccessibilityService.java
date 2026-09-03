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

        /*
         * Quando ci spostiamo tra le locandine,
         * memorizziamo il titolo ma NON apriamo Wholphin.
         */
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

        /*
         * La ricerca su Jellyfin parte SOLO quando
         * viene effettivamente cliccata la locandina.
         */
        if (type != AccessibilityEvent.TYPE_VIEW_CLICKED) {
            return;
        }

        AccessibilityNodeInfo node = event.getSource();

        String title = "";

        if (node != null) {
            title = getTitleFromNode(node);
        }

        /*
         * Se il click non contiene il titolo,
         * utilizziamo l'ultimo titolo rilevato durante il focus.
         */
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

        /*
         * Google TV può aggiungere dopo il titolo
         * informazioni provenienti dai vari servizi.
         *
         * Esempi:
         *
         * "Magari, RaiPlay"
         * "Magari, valutazione Rotten Tomatoes"
         * "Magari, richiede l'abbonamento a HBO Plus"
         * "Magari, costo 11,99€"
         */

        String[] metadataMarkers = {

                "richiede l'abbonamento",
                "richiede abbonamento",

                "disponibile con l'abbonamento",
                "disponibile con abbonamento",

                "guarda con l'abbonamento",
                "guarda con abbonamento",

                "valutazione rotten tomatoes",
                "rotten tomatoes",

                "disponibile su",
                "guarda su",

                "costo",

                "acquista",
                "acquisto",

                "noleggia",
                "noleggio",

                "prezzo"
        };

        int cutIndex = -1;

        /*
         * Cerchiamo i marcatori espliciti di metadati.
         */
        for (String marker : metadataMarkers) {

            int index = lower.indexOf(marker);

            if (index >= 0 &&
                    (cutIndex == -1 || index < cutIndex)) {

                cutIndex = index;
            }
        }

        /*
         * Se Google TV indica semplicemente un provider
         * dopo una virgola, riconosciamo alcuni provider comuni.
         */
        String[] providers = {

                "raiplay",
                "netflix",
                "prime video",
                "amazon prime video",
                "disney+",
                "disney plus",
                "max",
                "hbo max",
                "hbo plus",
                "paramount+",
                "paramount plus",
                "apple tv+",
                "apple tv",
                "sky",
                "now",
                "mediaset infinity",
                "pluto tv"
        };

        for (String provider : providers) {

            String marker = ", " + provider;

            int index = lower.indexOf(marker);

            if (index >= 0 &&
                    (cutIndex == -1 || index < cutIndex)) {

                cutIndex = index;
            }
        }

        /*
         * Tagliamo la parte di metadati.
         */
        if (cutIndex >= 0) {

            title =
                    title.substring(
                            0,
                            cutIndex
                    );
        }

        /*
         * Pulizia finale.
         */
        title = title.trim();

        while (title.endsWith(",")) {

            title =
                    title.substring(
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
