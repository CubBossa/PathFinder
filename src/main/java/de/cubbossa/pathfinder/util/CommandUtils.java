package de.cubbossa.pathfinder.util;

import de.cubbossa.pathfinder.core.roadmap.RoadMap;
import de.cubbossa.pathfinder.core.roadmap.RoadMapHandler;
import de.cubbossa.translations.Message;
import de.cubbossa.translations.TranslationHandler;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.function.Consumer;

@UtilityClass
public class CommandUtils {

    public <T> void printList(CommandSender sender, int page, int pageSize, List<T> elements, Consumer<T> print, Message header, Message footer) {

        int maxPage = (int) Math.ceil(elements.size() / (float) pageSize);
        if (maxPage == 0) {
            maxPage = 1;
        }
        page = Integer.min(page, maxPage);
        int nextPage = Integer.min(page + 1, maxPage);
        int prevPage = Integer.max(page - 1, 1);

        TagResolver resolver = TagResolver.builder()
                .resolver(Placeholder.parsed("page", page + ""))
                .resolver(Placeholder.parsed("prev-page", nextPage + ""))
                .resolver(Placeholder.parsed("next-page", prevPage + ""))
                .resolver(Placeholder.parsed("pages", maxPage + ""))
                .build();


        TranslationHandler.getInstance().sendMessage(header.format(resolver), sender);
        for (T element : CommandUtils.subList(elements, page - 1, pageSize)) {
            print.accept(element);
        }
        TranslationHandler.getInstance().sendMessage(footer.format(resolver), sender);
    }

    public <T> List<T> subList(List<T> list, int page, int pageSize) {
        return list.subList(Integer.min(page * pageSize, list.size() == 0 ? 0 : list.size() - 1), Integer.min((page + 1) * pageSize, list.size()));
    }
}
