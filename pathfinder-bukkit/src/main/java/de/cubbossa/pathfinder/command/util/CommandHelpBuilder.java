package de.cubbossa.pathfinder.command.util;

import de.cubbossa.tinytranslations.nanomessage.NanoMessage;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

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
    NanoMessage mm = NanoMessage.nanoMessage();
    List<Component> result = new ArrayList<>();
    for (Entry entry : entries) {
      TagResolver resolver = TagResolver.builder()
          .resolver(Placeholder.component("cmd", Component.text(entry.cmd())
              .clickEvent(ClickEvent.clickEvent(entry.action(), entry.actionInput()))))
          .resolver(Placeholder.parsed("desc", entry.desc()))
          .build();
      result.add(mm.deserialize(format, resolver));
    }
    return result;
  }

  private record Entry(String cmd, String desc, ClickEvent.Action action, String actionInput) {
  }
}
