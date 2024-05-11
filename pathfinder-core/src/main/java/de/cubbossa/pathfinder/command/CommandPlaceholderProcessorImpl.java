package de.cubbossa.pathfinder.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandPlaceholderProcessorImpl implements CommandPlaceholderProcessor {

  private final List<CmdTagResolver> resolvers = new ArrayList<>();
  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([a-zA-Z0-9._\s-]+?)}");

  @Override
  public void addResolver(CmdTagResolver resolver) {
    resolvers.add(resolver);
  }

  @Override
  public String process(String command, CmdTagResolver... resolvers) {

    List<CmdTagResolver> resolverCollection = new ArrayList<>();
    resolverCollection.addAll(this.resolvers);
    resolverCollection.addAll(Arrays.stream(resolvers).toList());

    Collections.reverse(resolverCollection);

    String c = command;

    Matcher matcher = PLACEHOLDER_PATTERN.matcher(c);
    while (matcher.find()) {
      int begin = matcher.start();
      int end = matcher.end();

      c = c.substring(0, begin) + parsePlaceholder(matcher.group(1), resolverCollection) + c.substring(end);
      matcher = PLACEHOLDER_PATTERN.matcher(c);
    }
    return c;
  }

  private String parsePlaceholder(String arg, Collection<CmdTagResolver> resolvers) {
    Queue<String> queue = toQueue(arg);
    String key = queue.poll().trim();
    Optional<CmdTagResolver> resolver = resolvers.stream().filter(r -> r.getKey().equalsIgnoreCase(key)).findFirst();
    if (resolver.isEmpty()) {
      return "";
    }
    return resolver.get().resolve(queue);
  }

  private Queue<String> toQueue(String arg) {
    return new LinkedList<>(Arrays.stream(arg.split("\\.")).toList());
  }
}
