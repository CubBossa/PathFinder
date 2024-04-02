package de.cubbossa.pathfinder.migration;

import de.cubbossa.disposables.Disposable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.logging.Logger;
import org.flywaydb.core.Flyway;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

public class Migrator implements Disposable {

  private final File file;
  private final Flyway flyway;

  public Migrator(File pluginDirectory, Logger logger) {

    this.file = new File(pluginDirectory, ".flyway.sqlite");
    if (!file.exists()) {
      try {
        if (!file.createNewFile()) {
          throw new IOException("Could not create Flyway storage file.");
        }
        Files.setAttribute(file.toPath(), "dos:hidden", Boolean.TRUE, LinkOption.NOFOLLOW_LINKS);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    SQLiteConfig config = new SQLiteConfig();
    config.setJournalMode(SQLiteConfig.JournalMode.OFF);
    config.setSynchronous(SQLiteConfig.SynchronousMode.OFF);

    SQLiteDataSource dataSource = new SQLiteDataSource(config);
    dataSource.setUrl("jdbc:sqlite:" + file.getAbsolutePath());

    flyway = Flyway.configure()
        .dataSource(dataSource)
        //.callbacks((String[]) null /* Callbacks here ... */)
        .load();
  }

  public void migrate() {
    flyway.migrate();
  }
}