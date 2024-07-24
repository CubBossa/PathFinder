package de.cubbossa.pathfinder.migration;

import static de.cubbossa.pathfinder.messages.Messages.*;
import de.cubbossa.pathfinder.PathFinder;
import de.cubbossa.tinytranslations.Message;
import de.cubbossa.tinytranslations.MessageTranslator;
import de.cubbossa.tinytranslations.storage.StorageEntry;
import de.cubbossa.tinytranslations.storage.properties.PropertiesUtils;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V5_4_0__Messages extends BaseJavaMigration {

  private static final Map<String, String> GLOBAL_REPLACEMENTS = new HashMap<>();
  private static final Map<Message, Map<String, String>> MESSAGE_REPLACEMENTS = new HashMap<>();

  static {
    var map = new HashMap<String, String>();
    map.put("c-brand-light", "primary_l");
    map.put("c-brand-dark", "primary_d");
    map.put("c-brand", "primary");
    map.put("c-offset-light", "offset_l");
    map.put("c-offset-dark", "offset_d");
    map.put("c-offset", "offset");
    map.put("c-accent-light", "accent_l");
    map.put("c-accent-dark", "accent_d");
    map.put("c-accent", "accent");
    map.put("<t>", "text");
    map.put("</t>", "text");
    map.put("t-light", "text_l");
    map.put("t-dark", "text_d");
    map.put("t-warm", "text_hl");
    map.put("t-highlight", "text_hl");
    map.put("t-hl", "text_hl");
    map.put("bg-light", "bg_l");
    map.put("bg-dark", "bg_d");
    map.put("<c-empty>", "");
    map.put("</c-empty>", "");
    map.put("c-warn", "warning");
    map.put("c-negative", "prefix_negative");
    map.put("<msg:prefix>", "<prefix>");
    GLOBAL_REPLACEMENTS.putAll(map);

    MESSAGE_REPLACEMENTS.put(CMD_FORCE_FIND, Map.of("<name>", "{target}", "<discovery>", "{discovery}"));
    MESSAGE_REPLACEMENTS.put(CMD_FORCE_FORGET, Map.of("<name>", "{target}", "<discovery>", "{discovery}"));
    MESSAGE_REPLACEMENTS.put(CMD_N_CREATE, Map.of("<id>", "{node}"));
    MESSAGE_REPLACEMENTS.put(CMD_N_DELETE, Map.of("<selection>", "{selection}"));
    MESSAGE_REPLACEMENTS.put(CMD_N_UPDATED, Map.of("<selection>", "{selection}"));
    MESSAGE_REPLACEMENTS.put(CMD_N_INFO, Map.of(
        "<position>", "{node.loc}",
        "<world>", "{node.loc.world}",
        "<edges>", "{node.edges}",
        "<groups>", "{node.groups}",
        ));
  }

  @Override
  public void migrate(Context context) throws Exception {

    MessageTranslator translator;

    File dataFolder = PathFinder.get().getDataFolder();
    if (!dataFolder.exists()) {
      return;
    }
    File langFolder = new File(dataFolder, "/lang/");
    if (!langFolder.exists()) {
      return;
    }
  }

  private void fixLocaleFiles(File file) {
    try {
      File backup = file;
      while (backup.exists()) {
        backup = new File(backup.getParent(), backup.getName() + "_backup");
      }
      Files.copy(file.toPath(), backup.toPath());

      var entries = PropertiesUtils.loadProperties(new FileReader(file));

      List<StorageEntry> modifiedEntries = new ArrayList<>();
      for (StorageEntry entry : entries) {

        String s = entry.value();
        for (Map.Entry<String, String> replacement : GLOBAL_REPLACEMENTS.entrySet()) {
          s = s.replaceAll(replacement.getKey(), replacement.getValue());
        }
        modifiedEntries.add(new StorageEntry(entry.key(), s, entry.comment()));
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }


  }
}