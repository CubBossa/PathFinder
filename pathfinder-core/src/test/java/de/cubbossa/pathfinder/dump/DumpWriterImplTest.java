package de.cubbossa.pathfinder.dump;

import com.google.gson.Gson;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DumpWriterImplTest {


  @BeforeEach
  void beforeEach() {
    new DumpWriterImpl(false);
  }

  @Test
  void property1() {
    DumpWriterProvider.get().addProperty("a", () -> "abc");
    Assertions.assertEquals(new Gson().toJson(Map.of("a", "abc")), DumpWriterProvider.get().toString());
    DumpWriterProvider.get().removeProperty("a");
    Assertions.assertEquals("{}", DumpWriterProvider.get().toString());
  }

  @Test
  void property2() {
    DumpWriterProvider.get().addProperty("a", () -> 1);
    Assertions.assertEquals(new Gson().toJson(Map.of("a", 1)), DumpWriterProvider.get().toString());
  }

  class Obj {
    int x = 1;
    String a = "abc";
  }

  @Test
  void property3() {
    DumpWriterProvider.get().addProperty("a", Obj::new);
    Assertions.assertEquals(new Gson().toJson(Map.of("a", new Obj())), DumpWriterProvider.get().toString());
  }
}