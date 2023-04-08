package de.cubbossa.pathfinder.util;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.storage.ApplicationLayer;
import de.cubbossa.translations.Message;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.ArgumentTreeLike;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;

@UtilityClass
public class CommandUtils {

  private CommandHelpGenerator generator = new CommandHelpGenerator();

  public void sendHelp(CommandSender sender, ArgumentTreeLike<?, ?> tree) {
    sendHelp(sender, tree, -1);
  }

  public void sendHelp(CommandSender sender, ArgumentTreeLike<?, ?> tree, int depth) {
    Audience audience = TranslationHandler.getInstance().getAudiences().sender(sender);
    TranslationHandler.getInstance().sendMessage(Messages.CMD_INCOMPLETE, audience);
    generator
        .format(tree, depth).stream()
        .map(c -> Messages.CMD_INCOMPLETE_LINE.format(Placeholder.component("cmd", c)))
        .forEach(audience::sendMessage);
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

  public <T> void printList(CommandSender sender, ApplicationLayer.Pagination pagination, Function<ApplicationLayer.Pagination, CompletableFuture<List<T>>> supplier,
                            Consumer<T> print, Message header, Message footer) {

    /*supplier.apply(pagination).thenAccept(elements -> {
      int maxPage = (int) Math.ceil(elements.size() / (float));
      if (maxPage == 0) {
        maxPage = 1;
      }
      page = Integer.min(page, maxPage);
      int prevPage = Integer.max(page - 1, 1);
      int nextPage = Integer.min(page + 1, maxPage);

      TagResolver resolver = TagResolver.builder()
          .resolver(Placeholder.parsed("page", page + ""))
          .resolver(Placeholder.parsed("prev-page", prevPage + ""))
          .resolver(Placeholder.parsed("next-page", nextPage + ""))
          .resolver(Placeholder.parsed("pages", maxPage + ""))
          .build();


      TranslationHandler.getInstance().sendMessage(header.format(resolver), sender);
      for (T element : CommandUtils.subListPaginated(elements, page - 1, pageSize)) {
        print.accept(element);
      }
      TranslationHandler.getInstance().sendMessage(footer.format(resolver), sender);
    });*/
  }

  public <T> void printList(CommandSender sender, int page, int pageSize, List<T> elements,
                            Consumer<T> print, Message header, Message footer) {

    int maxPage = (int) Math.ceil(elements.size() / (float) pageSize);
    if (maxPage == 0) {
      maxPage = 1;
    }
    page = Integer.min(page, maxPage);
    int prevPage = Integer.max(page - 1, 1);
    int nextPage = Integer.min(page + 1, maxPage);

    TagResolver resolver = TagResolver.builder()
        .resolver(Placeholder.parsed("page", page + ""))
        .resolver(Placeholder.parsed("prev-page", prevPage + ""))
        .resolver(Placeholder.parsed("next-page", nextPage + ""))
        .resolver(Placeholder.parsed("pages", maxPage + ""))
        .build();


    TranslationHandler.getInstance().sendMessage(header.format(resolver), sender);
    for (T element : CommandUtils.subListPaginated(elements, page - 1, pageSize)) {
      print.accept(element);
    }
    TranslationHandler.getInstance().sendMessage(footer.format(resolver), sender);
  }

  /**
   * Offsets a list by a certain count of elements by dropping the first elements of the list.
   *
   * @param list   The list to modify
   * @param offset The amount of elements to drop.
   * @param <T>    The element type of the list class.
   * @return A new list instance that is similar to the old list except for the first n elements
   * that were dropped
   */
  public <T> List<T> subList(List<T> list, int offset) {
    return list.subList(Integer.min(offset, list.size()), list.size());
  }

  /**
   * Offsets a list by a certain count of elements by dropping the first elements of the list
   * and truncating the result to the given limit.
   *
   * @param list   The list to modify
   * @param offset The amount of elements to drop.
   * @param limit  The limit of elements that the return list can have.
   * @param <T>    The element type of the list class.
   * @return A new list instance that is similar to the old list except for the first n elements
   * that were dropped
   */
  public <T> List<T> subList(List<T> list, int offset, int limit) {
    return list.subList(Integer.min(offset, list.size() == 0 ? 0 : list.size()),
        Integer.min(limit + offset, list.size()));
  }

  /**
   * Paginates a list by cutting it in slices of same size.
   *
   * @param list     The list with elements to paginate.
   * @param page     The page index, starting by 0.
   * @param pageSize The element count that makes up one page.
   * @param <T>      The element type of the list class.
   * @return A new list instance that is similar to the old list but only contains the given page.
   */
  public <T> List<T> subListPaginated(List<T> list, int page, int pageSize) {
    return subList(list, page * pageSize, pageSize);
  }
}
