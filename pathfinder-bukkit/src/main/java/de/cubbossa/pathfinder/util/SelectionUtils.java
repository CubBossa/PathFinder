package de.cubbossa.pathfinder.util;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathfinder.command.util.CommandUtils;
import de.cubbossa.pathfinder.node.attribute.DistanceSelectionAttribute;
import de.cubbossa.pathfinder.node.attribute.GroupSelectionAttribute;
import de.cubbossa.pathfinder.node.attribute.IdSelectionAttribute;
import de.cubbossa.pathfinder.node.attribute.LimitSelectionAttribute;
import de.cubbossa.pathfinder.node.attribute.OffsetSelectionAttribute;
import de.cubbossa.pathfinder.node.attribute.SortSelectionAttribute;
import de.cubbossa.pathfinder.node.selection.AbstractNodeSelectionParser;
import de.cubbossa.pathfinder.node.selection.BukkitNodeSelectionParser;
import de.cubbossa.pathfinder.node.selection.attribute.WorldSelectionAttribute;
import dev.jorel.commandapi.SuggestionInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SelectionUtils {

  public static final List<AbstractNodeSelectionParser.NodeSelectionArgument<?>> SELECTORS = List.of(
      new IdSelectionAttribute(),
      new OffsetSelectionAttribute(),
      new LimitSelectionAttribute(),
      new DistanceSelectionAttribute(),
      new SortSelectionAttribute(),
      new WorldSelectionAttribute(),
      new GroupSelectionAttribute()
  );
  private static final AbstractNodeSelectionParser<CommandSender, ?> parser = new BukkitNodeSelectionParser("node", "n", "nodes");

  static {
    SELECTORS.forEach(parser::addResolver);
  }

  public static NodeSelection getNodeSelection(Player player, String selectString)
      throws ParseCancellationException {

    List<Node> nodes = new ArrayList<>(PathFinderProvider.get().getStorage().loadNodes().join());
    NodeSelection selection = new NodeSelection(parser.parse(selectString, nodes, player));
    selection.setMeta(new NodeSelection.Meta(selectString, new HashMap<>()));
    return selection;
  }

  public static CompletableFuture<Suggestions> getNodeSelectionSuggestions(SuggestionInfo suggestionInfo, SuggestionsBuilder suggestionsBuilder) {
    if (!(suggestionInfo.sender() instanceof Player player)) {
      return suggestionsBuilder.buildFuture();
    }
    int offset = suggestionInfo.currentInput().length() - suggestionInfo.currentArg().length();

    return parser
        // remove quotation from input
        .applySuggestions(player, suggestionInfo.currentArg(),
            suggestionInfo.currentArg().length() > 0
                ? suggestionInfo.currentArg().substring(1)
                : "")
        //  add quotations to suggestions
        .thenApply(s -> CommandUtils.wrapWithQuotation(suggestionInfo.currentArg(), s,
            suggestionInfo.currentArg(), offset))
        // shift suggestions toward actual command argument offset
        .thenApply(s -> CommandUtils.offsetSuggestions(suggestionInfo.currentArg(), s, offset));
  }
}
