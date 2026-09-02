package it.riccardo.wholphintester;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

private static final String WHOLPHIN_PACKAGE =
        "com.github.damontecres.wholphin";

private static final String JELLYFIN_URL =
        "https://jellybrick.duckdns.org";

private static final String JELLYFIN_API_KEY =
        "INSERISCI_API_KEY";

private final ExecutorService executor =
        Executors.newSingleThreadExecutor();

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    handleIntent(getIntent());

    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setGravity(Gravity.CENTER);

    int pad = 48;
    layout.setPadding(pad, pad, pad, pad);

    TextView title = new TextView(this);
    title.setText("Wholphin Intent Tester");
    title.setTextSize(28);
    title.setGravity(Gravity.CENTER);

    layout.addView(title,
            new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

    TextView info = new TextView(this);
    info.setText(
            "\nGoogle TV → TMDB → Jellyfin → Wholphin"
    );
    info.setTextSize(18);
    info.setGravity(Gravity.CENTER);

    layout.addView(info,
            new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

    Button test = new Button(this);
    test.setText("TEST TMDB 524");
    test.setTextSize(18);
    test.setOnClickListener(v ->
            findAndLaunchWholphin("524"));

    LinearLayout.LayoutParams tp =
            new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

    tp.topMargin = 48;
    layout.addView(test, tp);

    setContentView(layout);
    test.requestFocus();
}

private void handleIntent(Intent intent) {

    if (intent == null) {
        return;
    }

    Uri data = intent.getData();

    if (data == null) {
        return;
    }

    String tmdb = data.getQueryParameter("tmdb");

    if (tmdb != null && !tmdb.isEmpty()) {

        toast("TMDB ricevuto: " + tmdb);

        findAndLaunchWholphin(tmdb);
    }
}

private void findAndLaunchWholphin(String tmdbId) {

    if (JELLYFIN_API_KEY.startsWith("INSERISCI")) {
        toast("API key Jellyfin non configurata.");
        return;
    }

    toast("Cerco TMDB " + tmdbId + " su Jellyfin...");

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

                    toast(
                            "TMDB " + tmdbId
                            + " non trovato su Jellyfin."
                    );

                    return;
                }

                toast(
                        "Trovato Jellyfin itemId: "
                        + itemId
                );

                launchWholphin(itemId);
            });

        } catch (Exception e) {

            runOnUiThread(() ->
                    toast(
                            "Errore Jellyfin: "
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

    } catch (ActivityNotFoundException e) {

        try {

            Intent fallback = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                            "wholphin://play?itemId="
                                    + itemId
                    )
            );

            fallback.setPackage(WHOLPHIN_PACKAGE);

            startActivity(fallback);

        } catch (Exception ignored) {

            toast(
                    "Wholphin non ha accettato l'intent."
            );
        }

    } catch (Exception e) {

        toast(
                "Errore apertura Wholphin: "
                        + e.getMessage()
        );
    }
}

private void toast(String message) {
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
