package it.riccardo.wholphintester;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    private static final String WHOLPHIN_PACKAGE =
            "com.github.damontecres.wholphin";

    private static final String JELLYFIN_URL =
            "https://jellybrick.duckdns.org";

    private static final String JELLYFIN_API_KEY =
            BuildConfig.JELLYFIN_API_KEY;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {

        if (intent == null) {
            return;
        }

        Uri data = intent.getData();

        if (data == null) {
            return;
        }

        // Google TV deve chiamare:
        // wholphinbridge://play?tmdb=12345
        if (!"wholphinbridge".equals(data.getScheme())) {
            return;
        }

        if (!"play".equals(data.getHost())) {
            return;
        }

        String tmdbId = data.getQueryParameter("tmdb");

        if (tmdbId == null || tmdbId.trim().isEmpty()) {
            showError("TMDB ID non ricevuto.");
            return;
        }

        findAndLaunchWholphin(tmdbId);
    }

    private void findAndLaunchWholphin(String tmdbId) {

        if (JELLYFIN_API_KEY.startsWith("INSERISCI")) {
            showError("API key Jellyfin non configurata.");
            return;
        }

        executor.execute(() -> {

            try {

                JellyfinApi api =
                        new JellyfinApi(
                                JELLYFIN_URL,
                                JELLYFIN_API_KEY
                        );

                String itemId =
                        api.findItemByTmdb(tmdbId);

                if (itemId == null) {

                    runOnUiThread(() ->
                            showError(
                                    "Film non trovato su Jellyfin."
                            )
                    );

                    return;
                }

                runOnUiThread(() ->
                        launchWholphin(itemId)
                );

            } catch (Exception e) {

                runOnUiThread(() ->
                        showError(
                                "Errore durante la ricerca Jellyfin."
                        )
                );
            }
        });
    }

    private void launchWholphin(String itemId) {

        try {

            Intent intent = new Intent(
                    "com.github.damontecres.wholphin.PLAYBACK"
            );

            intent.setPackage(WHOLPHIN_PACKAGE);
            intent.putExtra("itemId", itemId);

            startActivity(intent);

        } catch (ActivityNotFoundException e) {

            try {

                Intent fallback = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                                "wholphin://play?itemId="
                                        + Uri.encode(itemId)
                        )
                );

                fallback.setPackage(WHOLPHIN_PACKAGE);

                startActivity(fallback);

            } catch (Exception ignored) {

                showError(
                        "Impossibile aprire Wholphin."
                );
            }

        } catch (Exception e) {

            showError(
                    "Errore durante l'apertura di Wholphin."
            );
        }
    }

    private void showError(String message) {

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
