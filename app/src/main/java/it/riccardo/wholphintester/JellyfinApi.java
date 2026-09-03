package it.riccardo.wholphintester;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class JellyfinApi {

    private final String serverUrl;
    private final String apiKey;

    public JellyfinApi(String serverUrl, String apiKey) {
        this.serverUrl = serverUrl.replaceAll("/$", "");
        this.apiKey = apiKey;
    }

    public String findItemByTmdb(String tmdbId) throws Exception {

        String encodedTmdb =
                URLEncoder.encode(tmdbId, "UTF-8");

        String url =
                serverUrl
                + "/Items?Recursive=true"
                + "&IncludeItemTypes=Movie,Series"
                + "&Fields=ProviderIds"
                + "&AnyProviderId="
                + URLEncoder.encode(
                        "Tmdb=" + tmdbId,
                        "UTF-8"
                );

        HttpURLConnection connection =
                (HttpURLConnection)
                        new URL(url).openConnection();

        connection.setRequestMethod("GET");

        connection.setRequestProperty(
                "X-Emby-Token",
                apiKey
        );

        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        int responseCode =
                connection.getResponseCode();

        if (responseCode != 200) {
            throw new Exception(
                    "Jellyfin HTTP " + responseCode
            );
        }

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream()
                        )
                );

        StringBuilder result =
                new StringBuilder();

        String line;

        while ((line = reader.readLine()) != null) {
            result.append(line);
        }

        reader.close();

        JSONObject json =
                new JSONObject(result.toString());

        JSONArray items =
                json.optJSONArray("Items");

        if (items == null || items.length() == 0) {
            return null;
        }

        // Controlliamo esplicitamente il TMDB ID
        // di ogni risultato.
        for (int i = 0; i < items.length(); i++) {

            JSONObject item =
                    items.getJSONObject(i);

            JSONObject providerIds =
                    item.optJSONObject("ProviderIds");

            if (providerIds == null) {
                continue;
            }

            String foundTmdb =
                    providerIds.optString(
                            "Tmdb",
                            ""
                    );

            if (tmdbId.equals(foundTmdb)) {

                return item.getString("Id");
            }
        }

        return null;
    }
public String findItemByTitle(String title) throws Exception {

    String encodedTitle =
            URLEncoder.encode(title, "UTF-8");

    String url =
            serverUrl
            + "/Items?Recursive=true"
            + "&IncludeItemTypes=Movie"
            + "&Fields=ProviderIds"
            + "&SearchTerm="
            + encodedTitle;

    HttpURLConnection connection =
            (HttpURLConnection)
                    new URL(url).openConnection();

    connection.setRequestMethod("GET");

    connection.setRequestProperty(
            "X-Emby-Token",
            apiKey
    );

    connection.setConnectTimeout(10000);
    connection.setReadTimeout(10000);

    int responseCode =
            connection.getResponseCode();

    if (responseCode != 200) {
        throw new Exception(
                "Jellyfin HTTP " + responseCode
        );
    }

    BufferedReader reader =
            new BufferedReader(
                    new InputStreamReader(
                            connection.getInputStream()
                    )
            );

    StringBuilder result =
            new StringBuilder();

    String line;

    while ((line = reader.readLine()) != null) {
        result.append(line);
    }

    reader.close();

    JSONObject json =
            new JSONObject(result.toString());

    JSONArray items =
            json.optJSONArray("Items");

    if (items == null || items.length() == 0) {
        return null;
    }

    // Prima cerchiamo un titolo esattamente uguale
    for (int i = 0; i < items.length(); i++) {

        JSONObject item =
                items.getJSONObject(i);

        String foundName =
                item.optString("Name", "");

        if (title.equalsIgnoreCase(foundName.trim())) {

            JSONObject providerIds =
                    item.optJSONObject("ProviderIds");

            if (providerIds != null &&
                    providerIds.has("Tmdb")) {

                return item.getString("Id");
            }
        }
    }

    return null;
}
}
