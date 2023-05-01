package de.cubbossa.pathfinder.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.cubbossa.pathfinder.command.util.CommandUtils;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.CommandAPIArgumentType;
import dev.jorel.commandapi.nms.NMS;

public class CommandArgument<S, T extends Argument<S>> extends Argument<S> {

  private final T argument;
  private String wiki;
  private String description;
  private boolean optional;

  public CommandArgument(final T argument) {
    super(argument.getNodeName(), argument.getRawType());
    this.argument = argument;
  }

  static <S, T extends Argument<S>> CommandArgument<S, T> arg(T argument) {
    return new CommandArgument<>(argument);
  }

  @Override
  public String getNodeName() {
    return argument.getNodeName();
  }

  @Override
  public ArgumentType<?> getRawType() {
    return argument.getRawType();
  }

  public CommandArgument<S, T> withGeneratedHelp() {
    executes((sender, args) -> {
      CommandUtils.sendHelp(sender, this);
    });
    return this;
  }

  public CommandArgument<S, T> withGeneratedHelp(int depth) {
    executes((sender, args) -> {
      CommandUtils.sendHelp(sender, this, depth);
    });
    return this;
  }

  public CommandArgument<S, T> withWiki(String url) {
    this.wiki = url;
    return this;
  }

  public CommandArgument<S, T> withDescription(String description) {
    this.description = description;
    return this;
  }

  public CommandArgument<S, T> displayAsOptional() {
    this.optional = true;
    return this;
  }

  public boolean isOptional() {
    return optional;
  }

  public String getWiki() {
    return this.wiki;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public Class<S> getPrimitiveType() {
    return argument.getPrimitiveType();
  }

  @Override
  public CommandAPIArgumentType getArgumentType() {
    return argument.getArgumentType();
  }

  @Override
  public <CommandSourceStack> S parseArgument(NMS<CommandSourceStack> nms,
                                              CommandContext<CommandSourceStack> cmdCtx, String key,
                                              Object[] previousArgs) throws CommandSyntaxException {
    return argument.parseArgument(nms, cmdCtx, key, previousArgs);
  }
}
