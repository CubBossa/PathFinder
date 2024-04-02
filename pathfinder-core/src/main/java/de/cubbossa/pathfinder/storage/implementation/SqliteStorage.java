package de.cubbossa.pathfinder.storage.implementation;

import de.cubbossa.pathapi.group.ModifierRegistry;
import de.cubbossa.pathapi.node.NodeTypeRegistry;
import de.cubbossa.pathapi.visualizer.VisualizerTypeRegistry;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.jooq.SQLDialect;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

public class SqliteStorage extends SqlStorage {

  private final File file;
  @Getter
  private SQLiteDataSource dataSource;

  public SqliteStorage(File file, NodeTypeRegistry nodeTypeRegistry,
                       ModifierRegistry modifierRegistry,
                       VisualizerTypeRegistry visualizerTypeRegistry) {
    super(SQLDialect.SQLITE, nodeTypeRegistry, modifierRegistry, visualizerTypeRegistry);
    this.file = file;
  }

  @Override
  public @Nullable ExecutorService service(ThreadFactory factory) {
    return Executors.newSingleThreadExecutor(factory);
  }

  public void init() throws Exception {
    if (!file.exists()) {
      file.getParentFile().mkdirs();
      file.createNewFile();
    }
    SQLiteConfig config = new SQLiteConfig();
    config.setJournalMode(SQLiteConfig.JournalMode.OFF);
    config.setSynchronous(SQLiteConfig.SynchronousMode.OFF);

    dataSource = new SQLiteDataSource(config);
    dataSource.setUrl("jdbc:sqlite:" + file.getAbsolutePath());
    super.init();
  }

  public void shutdown() {

  }
}
