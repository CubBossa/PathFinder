package de.cubbossa.pathfinder.util;

import de.cubbossa.translations.Message;
import de.cubbossa.translations.TranslationHandler;
import java.util.List;
import java.util.function.Consumer;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;

@UtilityClass
public class CommandUtils {

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

  public <T> List<T> subList(List<T> list, int offset) {
    return list.subList(Integer.min(offset, list.size()), list.size());
  }

  public <T> List<T> subList(List<T> list, int offset, int limit) {
    return list.subList(Integer.min(offset, list.size() == 0 ? 0 : list.size() - 1),
        Integer.min(limit + offset, list.size()));
  }

  public <T> List<T> subListPaginated(List<T> list, int page, int pageSize) {
    return subList(list, page * pageSize, pageSize);
  }
}
