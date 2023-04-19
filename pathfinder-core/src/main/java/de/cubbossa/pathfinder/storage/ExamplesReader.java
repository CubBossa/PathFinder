package de.cubbossa.pathfinder.storage;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.cubbossa.pathfinder.PathPlugin;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class ExamplesReader {

  public CompletableFuture<Collection<ExampleFile>> getExamples(String link) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        URL url = new URL(link);
        HttpURLConnection http = (HttpURLConnection) url.openConnection();

        http.setRequestMethod("GET");
        http.setRequestProperty("Accept", "application/vnd.github+json");

        http.setConnectTimeout(5000);

        int status = http.getResponseCode();
        if (status != 200) {
          PathPlugin.getInstance().getLogger().log(Level.SEVERE,
              "Bad response: " + status + ", could not fetch example visualizers.");
          return new HashSet<>();
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
          content.append(inputLine);
        }
        in.close();

        Collection<ExampleFile> files = new HashSet<>();
        JsonArray array = JsonParser.parseString(content.toString()).getAsJsonArray();
        array.forEach(jsonElement -> {
          JsonObject obj = jsonElement.getAsJsonObject();
          String name = obj.get("name").getAsString();
          if (!name.endsWith(".yml")) {
            return;
          }
          files.add(new ExampleFile(name, obj.get("download_url").getAsString()));
        });
        http.disconnect();

        return files;

      } catch (Throwable t) {
        PathPlugin.getInstance().getLogger().log(Level.SEVERE, t, t::getMessage);
        return new HashSet<>();
      }
    });
  }

  public CompletableFuture<String> read(String link) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        URL url = new URL(link);
        HttpURLConnection http = (HttpURLConnection) url.openConnection();

        InputStream stream = http.getInputStream();
        if (stream != null) {
          Writer writer = new StringWriter();
          char[] buffer = new char[2048];
          try {
            Reader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            int counter;
            while ((counter = reader.read(buffer)) != -1) {
              writer.write(buffer, 0, counter);
            }
          } finally {
            stream.close();
          }
          return writer.toString();
        }
        return "";
      } catch (Throwable t) {
        PathPlugin.getInstance().getLogger().log(Level.SEVERE, t, t::getMessage);
        return "";
      }
    });
  }

  public record ExampleFile(String name, String fetchUrl) {
  }
}
