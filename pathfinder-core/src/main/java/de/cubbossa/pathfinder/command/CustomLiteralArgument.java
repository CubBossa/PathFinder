package de.cubbossa.pathfinder.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.cubbossa.pathfinder.command.util.CommandUtils;
import dev.jorel.commandapi.arguments.CommandAPIArgumentType;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.exceptions.BadLiteralException;
import dev.jorel.commandapi.nms.NMS;

public class CustomLiteralArgument extends LiteralArgument {

  private String literal;
  private String wiki;
  private String description;
  private boolean optional;

  public CustomLiteralArgument(final String literal) {
    super(literal);

    if (literal == null) {
      throw new BadLiteralException(true);
    }
    if (literal.isEmpty()) {
      throw new BadLiteralException(false);
    }
    this.literal = literal;
    this.setListed(false);
  }

  public static dev.jorel.commandapi.arguments.LiteralArgument of(final String literal) {
    return new dev.jorel.commandapi.arguments.LiteralArgument(literal);
  }

  public static dev.jorel.commandapi.arguments.LiteralArgument literal(final String literal) {
    return new dev.jorel.commandapi.arguments.LiteralArgument(literal);
  }

  @Override
  public Class<String> getPrimitiveType() {
    return String.class;
  }

  public String getLiteral() {
    return literal;
  }

  @Override
  public CommandAPIArgumentType getArgumentType() {
    return CommandAPIArgumentType.LITERAL;
  }

  @Override
  public <CommandListenerWrapper> String parseArgument(NMS<CommandListenerWrapper> nms,
                                                       CommandContext<CommandListenerWrapper> cmdCtx,
                                                       String key, Object[] previousArgs)
      throws CommandSyntaxException {
    return literal;
  }


  public CustomLiteralArgument withGeneratedHelp() {
    executes((sender, args) -> {
      CommandUtils.sendHelp(sender, this);
    });
    return this;
  }

  public CustomLiteralArgument withGeneratedHelp(int depth) {
    executes((sender, args) -> {
      CommandUtils.sendHelp(sender, this, depth);
    });
    return this;
  }

  public CustomLiteralArgument withWiki(String url) {
    this.wiki = url;
    return this;
  }

  public CustomLiteralArgument withDescription(String description) {
    this.description = description;
    return this;
  }

  public CustomLiteralArgument displayAsOptional() {
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

}

