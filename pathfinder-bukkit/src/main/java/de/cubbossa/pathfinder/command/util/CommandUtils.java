package de.cubbossa.pathfinder.command.util;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import de.cubbossa.pathfinder.misc.Pagination;
import de.cubbossa.pathfinder.util.BukkitUtils;
import de.cubbossa.pathfinder.util.CollectionUtils;
import de.cubbossa.translations.Message;
import dev.jorel.commandapi.arguments.Argument;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;

@UtilityClass
public class CommandUtils {

//  private CommandHelpGenerator generator = new CommandHelpGenerator();

  public void sendHelp(CommandSender sender, Argument<?> tree) {
    sendHelp(sender, tree, -1);
  }

  public void sendHelp(CommandSender sender, Argument<?> tree, int depth) {
//    Audience audience = TranslationHandler.getInstance().getAudiences().sender(sender);
//    TranslationHandler.getInstance().sendMessage(Messages.CMD_INCOMPLETE, audience);
//    generator
//            .format(tree, depth).stream()
//            .map(c -> Messages.CMD_INCOMPLETE_LINE.formatted(Placeholder.component("cmd", c)))
//            .forEach(audience::sendMessage);
  }

  /**
   * Shifts all suggestions one character to the right and adds a "" suggestion if the
   * current string is empty. If the first string is no quotation mark, it suggests a single
   * " that overrides the whole input
   *
   * @param command     The whole command string including the argument.
   * @param suggestions The suggestions to wrap with quotations.
   * @param current     The current argument string.
   * @param offset      The offset of the current argument.
   * @return A new instance of Suggestions
   */
  public Suggestions wrapWithQuotation(String command, Suggestions suggestions, String current,
                                       int offset) {
    List<Suggestion> result = new ArrayList<>();
    if (current.length() == 0) {
      result.add(new Suggestion(StringRange.at(0), "\"\""));
    } else {
      if (!current.startsWith("\"")) {
        return Suggestions.create(command, List.of(
            new Suggestion(StringRange.between(offset, offset + current.length()), "\"")
        ));
      } else {
        result.addAll(suggestions.getList().stream()
            .map(s -> new Suggestion(StringRange.between(
                s.getRange().getStart() + 1,
                s.getRange().getEnd() + 1
            ), s.getText(), s.getTooltip())).toList());
      }
    }
    return Suggestions.create(command, result);
  }

  public Suggestions offsetSuggestions(String command, Suggestions suggestions, int offset) {
    List<Suggestion> result = suggestions.getList().stream()
        .map(s -> new Suggestion(StringRange.between(
            s.getRange().getStart() + offset,
            s.getRange().getEnd() + offset
        ), s.getText(), s.getTooltip()))
        .collect(Collectors.toList());

    return Suggestions.create(command, result);
  }

  public <T> void printList(CommandSender sender, Pagination pagination, List<T> elements,
                            Consumer<T> print, Message header, Message footer) {

    int page = pagination.getPage() + 1;
    int maxPage = pagination.getPageCount(elements.size());
    int prevPage = Integer.max(page - 1, 1);
    int nextPage = Integer.min(page + 1, maxPage);

    TagResolver resolver = TagResolver.resolver(
        Placeholder.parsed("page", page + ""),
        Placeholder.parsed("prev-page", prevPage + ""),
        Placeholder.parsed("next-page", nextPage + ""),
        Placeholder.parsed("pages", maxPage + "")
    );

    BukkitUtils.wrap(sender).sendMessage(header.formatted(resolver));
    for (T element : CollectionUtils.subList(elements, pagination)) {
      print.accept(element);
    }
    BukkitUtils.wrap(sender).sendMessage(footer.formatted(resolver));
  }
}
