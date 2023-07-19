package de.cubbossa.pathfinder.command;

public interface CommandPlaceholderProcessor {

  void addResolver(CmdTagResolver resolver);

  String process(String command, CmdTagResolver... resolvers);
}
