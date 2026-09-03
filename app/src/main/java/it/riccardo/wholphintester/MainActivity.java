package it.riccardo.wholphintester;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    private static final String WHOLPHIN_PACKAGE =
            "com.github.damontecres.wholphin";

    public static final String JELLYFIN_URL =
            "https://jellybrick.duckdns.org";

    public static final String JELLYFIN_API_KEY =
            BuildConfig.JELLYFIN_API_KEY;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        status = new TextView(this);
        status.setTextSize(18);
        status.setPadding(40, 40, 40, 40);

        setContentView(status);

        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {

        if (intent == null) {
            status.setText("Bridge avviato\n\nIntent: NULL");
            return;
        }

        Uri data = intent.getData();

        StringBuilder info = new StringBuilder();

        info.append("BRIDGE ATTIVO\n\n");

        info.append("Action:\n");
        info.append(intent.getAction());
        info.append("\n\n");

        info.append("Data / URI:\n");
        info.append(data);
        info.append("\n\n");

        if (data == null) {
            info.append("TMDB: NESSUNO");
            status.setText(info.toString());
            return;
        }

        info.append("Scheme: ");
        info.append(data.getScheme());
        info.append("\n");

        info.append("Host: ");
        info.append(data.getHost());
        info.append("\n\n");

        String tmdb = data.getQueryParameter("tmdb");

        info.append("TMDB: ");
        info.append(tmdb);
        info.append("\n\n");

        status.setText(info.toString());

        if (tmdb != null && !tmdb.isEmpty()) {
            findAndLaunchWholphin(tmdb);
        }
    }

    private void findAndLaunchWholphin(String tmdbId) {

        status.setText(
                status.getText()
                        + "\n\nRicerca TMDB "
                        + tmdbId
                        + " su Jellyfin..."
        );

        executor.execute(() -> {

            try {

                JellyfinApi api =
                        new JellyfinApi(
                                JELLYFIN_URL,
                                JELLYFIN_API_KEY
                        );

                String itemId =
                        api.findItemByTmdb(tmdbId);

                runOnUiThread(() -> {

                    if (itemId == null) {

                        status.setText(
                                status.getText()
                                        + "\n\nTMDB NON TROVATO SU JELLYFIN"
                        );

                        return;
                    }

                    status.setText(
                            status.getText()
                                    + "\n\nJellyfin itemId:\n"
                                    + itemId
                                    + "\n\nApro Wholphin..."
                    );

                    launchWholphin(itemId);
                });

            } catch (Exception e) {

                runOnUiThread(() ->
                        status.setText(
                                status.getText()
                                        + "\n\nERRORE:\n"
                                        + e.getMessage()
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

        } catch (Exception e) {

            status.setText(
                    status.getText()
                            + "\n\nERRORE WHOLPHIN:\n"
                            + e.getMessage()
            );
        }
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
