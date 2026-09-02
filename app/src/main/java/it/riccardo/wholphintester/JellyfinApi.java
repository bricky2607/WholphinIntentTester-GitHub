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

        String encoded =
                URLEncoder.encode(tmdbId, "UTF-8");

        String url =
                serverUrl
                + "/Items?Recursive=true"
                + "&IncludeItemTypes=Movie,Series"
                + "&AnyProviderId="
                + URLEncoder.encode("Tmdb=" + encoded, "UTF-8");

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

        JSONObject item =
                items.getJSONObject(0);

        return item.getString("Id");
    }
}
