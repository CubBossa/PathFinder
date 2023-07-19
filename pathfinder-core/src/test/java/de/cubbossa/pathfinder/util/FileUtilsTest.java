package de.cubbossa.pathfinder.util;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

class FileUtilsTest {

    private File resources = new File("src/test/java/resources");

    @SneakyThrows
    @Test
    void renameTo() {

        File a = new File(resources, "a/b.txt");
        a.mkdirs();
        a.createNewFile();

        File target = new File(resources, "c/");
        FileUtils.renameTo(new File(resources, "a/"), target);

        Assertions.assertTrue(target.exists());
        Assertions.assertTrue(target.isDirectory());
        Assertions.assertTrue(target.listFiles().length > 0);
    }

    @Test
    void copy() {
    }

    @Test
    void deleteDir() {
    }
}