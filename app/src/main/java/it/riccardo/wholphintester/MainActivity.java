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
                "\nQuesta app può ricevere un Playback URI.\n\n" +
                "Esempio:\n" +
                "wholphinbridge://play?tmdb=12345"
        );
        info.setTextSize(18);
        info.setGravity(Gravity.CENTER);

        layout.addView(info,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

        Button play = new Button(this);
        play.setText("RIPRODUCI CON WHOLPHIN");
        play.setTextSize(18);
        play.setOnClickListener(v -> launchWholphin());

        LinearLayout.LayoutParams bp =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        bp.topMargin = 48;
        layout.addView(play, bp);

        setContentView(layout);
        play.requestFocus();
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

        String message = "Deep link ricevuto";

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
    }

    private void launchWholphin() {

        try {

            Intent intent = new Intent(
                    "com.github.damontecres.wholphin.PLAYBACK");

            intent.setPackage(WHOLPHIN_PACKAGE);
            intent.putExtra("itemId", ITEM_ID);

            startActivity(intent);

        } catch (ActivityNotFoundException e) {

            try {

                startActivity(
                        new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                        "wholphin://play?itemId=" + ITEM_ID
                                )
                        )
                );

            } catch (Exception ignored) {

                toast(
                        "Wholphin non ha accettato l'intent di test."
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
}
