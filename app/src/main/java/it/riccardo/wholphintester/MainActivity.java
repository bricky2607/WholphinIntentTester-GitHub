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

public class MainActivity extends Activity {

    private static final String ITEM_ID =
            "608ec62c2aea09425cf83b0f62dbcb5a";

    private static final String WHOLPHIN_PACKAGE =
            "com.github.damontecres.wholphin";

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
                "\nTest integrazione Google TV → Wholphin"
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
        test.setOnClickListener(v -> testDeepLink());

        LinearLayout.LayoutParams tp =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        tp.topMargin = 48;
        layout.addView(test, tp);

        Button play = new Button(this);
        play.setText("RIPRODUCI CON WHOLPHIN");
        play.setTextSize(18);
        play.setOnClickListener(v -> launchWholphinWithItemId(ITEM_ID));

        LinearLayout.LayoutParams bp =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        bp.topMargin = 24;
        layout.addView(play, bp);

        setContentView(layout);
        test.requestFocus();
    }

    private void testDeepLink() {

        Uri uri = Uri.parse(
                "wholphinbridge://play?tmdb=524"
        );

        Intent intent = new Intent(
                Intent.ACTION_VIEW,
                uri
        );

        intent.addCategory(Intent.CATEGORY_BROWSABLE);

        try {
            startActivity(intent);
        } catch (Exception e) {
            toast("Errore apertura deep link: " + e.getMessage());
        }
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
        String imdb = data.getQueryParameter("imdb");

        if (tmdb == null && imdb == null) {
            toast("Deep link ricevuto, ma nessun ID trovato.");
            return;
        }

        String message = "Deep link ricevuto!";

        if (tmdb != null) {
            message += "\nTMDB: " + tmdb;
        }

        if (imdb != null) {
            message += "\nIMDb: " + imdb;
        }

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();

        /*
         * Per ora utilizziamo l'ID ricevuto dal deep link.
         *
         * ATTENZIONE:
         * Wholphin normalmente necessita del proprio Jellyfin
         * itemId per aprire direttamente un elemento.
         *
         * Quindi il prossimo passaggio sarà collegare TMDB/IMDb
         * al relativo itemId Jellyfin.
         */

        if (tmdb != null) {
            toast("TMDB ricevuto: " + tmdb);
        }
    }

    private void launchWholphinWithItemId(String itemId) {

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
                                "wholphin://play?itemId=" + itemId
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

            toast("Errore: " + e.getMessage());
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
}
