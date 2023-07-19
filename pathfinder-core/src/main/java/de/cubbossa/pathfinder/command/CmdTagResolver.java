package de.cubbossa.pathfinder.command;

import java.util.Queue;
import java.util.function.Function;

public interface CmdTagResolver {

  String getKey();

  String resolve(Queue<String> argumentQueue);

  static CmdTagResolver tag(String key, Function<Queue<String>, String> resolver) {
    return new CmdTagResolver() {
      @Override
      public String getKey() {
        return key;
      }

      @Override
      public String resolve(Queue<String> argumentQueue) {
        return resolver.apply(argumentQueue);
      }
    };
  }
}
