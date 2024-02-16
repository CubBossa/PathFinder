package de.cubbossa.pathfinder.command.util;

import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.tinytranslations.MessageTranslator;
import de.cubbossa.tinytranslations.util.FormattableBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

import java.util.ArrayList;
import java.util.List;

public class CommandHelpBuilder {

  private final String format;
  private final List<Entry> entries;

  public CommandHelpBuilder(String format) {
    this.format = format;
    this.entries = new ArrayList<>();
  }

  public CommandHelpBuilder withCmd(String cmd, String desc) {
    this.entries.add(new Entry(cmd, desc, null, ""));
    return this;
  }

  public CommandHelpBuilder withClickCmd(String cmd, String desc) {
    this.entries.add(new Entry(cmd, desc, ClickEvent.Action.RUN_COMMAND, cmd));
    return this;
  }

  public CommandHelpBuilder withClickCmd(String cmd, String clicked, String desc) {
    this.entries.add(new Entry(cmd, desc, ClickEvent.Action.RUN_COMMAND, clicked));
    return this;
  }

  public CommandHelpBuilder withSuggestCmd(String cmd, String desc) {
    this.entries.add(new Entry(cmd, desc, ClickEvent.Action.SUGGEST_COMMAND, cmd));
    return this;
  }

  public CommandHelpBuilder withSuggestCmd(String cmd, String suggest, String desc) {
    this.entries.add(new Entry(cmd, desc, ClickEvent.Action.SUGGEST_COMMAND, suggest));
    return this;
  }

  public List<Component> build() {
    MessageTranslator mm = PathFinderProvider.get().getTranslations();
    List<Component> result = new ArrayList<>();
    for (Entry entry : entries) {
      result.add(mm.translate(format, FormattableBuilder.builder()
          .insertString("cmd", entry.cmd())
          .insertString("desc", entry.desc())
          .toResolver()));
    }
    return result;
  }

  private record Entry(String cmd, String desc, ClickEvent.Action action, String actionInput) {
  }
}
