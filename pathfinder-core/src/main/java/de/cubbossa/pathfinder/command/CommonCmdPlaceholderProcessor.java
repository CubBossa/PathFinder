package de.cubbossa.pathfinder.command;

import java.util.*;

public class CommonCmdPlaceholderProcessor implements CommandPlaceholderProcessor {

  private final List<CmdTagResolver> resolvers = new ArrayList<>();

  @Override
  public void addResolver(CmdTagResolver resolver) {
    resolvers.add(resolver);
  }

  @Override
  public String process(String command, CmdTagResolver... resolvers) {

    List<CmdTagResolver> resolverMap = new ArrayList<>();
    resolverMap.addAll(this.resolvers);
    resolverMap.addAll(Arrays.stream(resolvers).toList());

    Collections.reverse(resolverMap);
    return recursiveProcess(command, resolverMap)
        .replaceAll("\\\\>", ">")
        .replaceAll("\\\\<", "<");
  }

  private String recursiveProcess(String command, Collection<CmdTagResolver> resolvers) {
    for (int i = command.length() - 1; i >= 0; i--) {
      if (command.charAt(i) != '<') {
        continue;
      }
      if (command.charAt(i - 1) == '\\') {
        continue;
      }
      command = processWithStartIndex(command, resolvers, i);
      i = command.length() - 1;
    }
    return command;
  }

  private String processWithStartIndex(String command, Collection<CmdTagResolver> resolvers, int openIndex) {
    for (int i = openIndex; i < command.length(); i++) {
      if (command.charAt(i) == '\\') {
        i++;
        continue;
      }
      if (command.charAt(i) != '>') {
        continue;
      }
      Queue<String> queue = toQueue(command.substring(openIndex + 1, i));
      String key = queue.poll();
      Optional<CmdTagResolver> resolver = resolvers.stream().filter(r -> r.getKey().equalsIgnoreCase(key)).findFirst();
      if (resolver.isEmpty()) {
        return replaceChar(command, "\\>", i);
      }
      String parsedPlaceholder = resolver.get().resolve(queue);
      return command.substring(0, openIndex) + parsedPlaceholder + command.substring(i + 1);
    }
    return command;
  }

  private Queue<String> toQueue(String arg) {
    return new LinkedList<>(Arrays.stream(arg.split(":")).toList());
  }

  private String replaceChar(String str, String insert, int index) {
    return str.substring(0, index) + insert + str.substring(index + 1);
  }
}
