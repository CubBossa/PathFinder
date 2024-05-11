package de.cubbossa.pathfinder.util;

import java.io.File;
import java.nio.file.Files;
import lombok.SneakyThrows;

public class FileUtils {

  public static boolean renameTo(File file, File other) {
    copy(file, other);
    return deleteDir(file);
  }

  @SneakyThrows
  public static void copy(File file, File target) {
    File[] files = file.listFiles();
    Files.copy(file.toPath(), target.toPath());
    if (files != null) { //some JVMs return null for empty dirs
      for (File f : files) {
        copy(new File(file, f.getName()), new File(target, f.getName()));
      }
    }
  }

  public static boolean deleteDir(File file) {
    File[] files = file.listFiles();
    if (files != null) { //some JVMs return null for empty dirs
      for (File f : files) {
        if (f.isDirectory()) {
          deleteDir(f);
        } else {
          if (!f.delete()) {
            return false;
          }
        }
      }
    }
    return file.delete();
  }
}
