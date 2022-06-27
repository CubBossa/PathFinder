package de.bossascrew.pathfinder.commands.argument;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import de.bossascrew.pathfinder.node.NodeGroup;
import de.bossascrew.pathfinder.roadmap.RoadMap;
import de.bossascrew.pathfinder.util.CommandUtils;
import dev.jorel.commandapi.arguments.*;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class CustomArgs {

	private static final Pattern MINI_FINISH = Pattern.compile(".*(</?[^<>]+)");
	private static final Pattern MINI_CLOSE = Pattern.compile(".*<([^/<>:]+)(:[^/<>]+)?>[^/<>]*");

	public Argument<String> miniMessageArgument(String nodeName) {
		return new GreedyStringArgument(nodeName).replaceSuggestions((info, builder) -> {

			List<Suggestion> suggestions = new ArrayList<>();
			int offset = builder.getInput().length();

			Matcher m = MINI_FINISH.matcher(info.currentArg());
			if (m.matches()) {
				offset = info.currentInput().lastIndexOf("<");
				int finalOffset = offset;
				String filter = info.currentArg().substring(finalOffset + 1);
				NamedTextColor.NAMES.keys().stream()
						.filter(s -> s.startsWith(filter))
						.map(s -> "<" + s + ">")
						.map(s -> new Suggestion(StringRange.between(finalOffset, finalOffset + 1), s))
						.forEach(suggestions::add);

				suggestions.add(new Suggestion(StringRange.between(offset, offset + 1), m.group(1) + ">"));
			} else {
				Matcher matcher = MINI_CLOSE.matcher(info.currentArg());
				if (matcher.matches()) {
					suggestions.add(new Suggestion(StringRange.at(offset), "</" + matcher.group(1) + ">"));
				}
			}
			return CompletableFuture.completedFuture(new Suggestions(StringRange.at(offset), suggestions));
		});
	}

	public Argument<NodeGroup> nodeGroupArgument(String nodeName) {
		return new CustomArgument<>(new NamespacedKeyArgument(nodeName), info -> {
			RoadMap roadMap = CommandUtils.getSelectedRoadMap(info.sender());
			NodeGroup group = roadMap.getNodeGroup(info.currentInput());
			if (group == null) {
				throw new CustomArgument.CustomArgumentException("abc");
			}
			return group;
		}).replaceSuggestions(ArgumentSuggestions.strings(info -> {
			RoadMap roadMap = CommandUtils.getSelectedRoadMap(info.sender(), false);
			if (roadMap == null) {
				return new String[0];
			}
			return roadMap.getGroups().values().stream().map(NodeGroup::getKey).map(NamespacedKey::toString).toArray(String[]::new);
		}));
	}

}
