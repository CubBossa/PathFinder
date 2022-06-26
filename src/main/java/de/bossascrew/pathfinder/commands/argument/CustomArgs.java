package de.bossascrew.pathfinder.commands.argument;

import de.bossascrew.pathfinder.node.NodeGroup;
import de.bossascrew.pathfinder.roadmap.RoadMap;
import de.bossascrew.pathfinder.util.CommandUtils;
import dev.jorel.commandapi.arguments.*;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class CustomArgs {

	private static final Pattern MINI_FINISH = Pattern.compile(".*</?[^<>]+");
	private static final Pattern MINI_CLOSE = Pattern.compile(".*<([^/<>]+)>[^/<>]+");

	public Argument<String> miniMessageArgument(String nodeName) {
		return new CustomArgument<>(new GreedyStringArgument(nodeName),
				CustomArgument.CustomArgumentInfo::currentInput
		).includeSuggestions(ArgumentSuggestions.strings(info -> {
			if (MINI_FINISH.matcher(info.currentInput()).matches()) {
				return new String[]{">"};
			}
			Matcher matcher = MINI_CLOSE.matcher(info.currentInput());
			if (matcher.matches()) {
				return new String[]{"</" + matcher.group(1) + ">"};
			}
			if (info.currentInput().endsWith("<")) {
				return NamedTextColor.NAMES.keys().stream()
						.map(s -> "<" + s + ">")
						.toArray(String[]::new);
			}
			return new String[0];
		}));
	}

	public Argument<NodeGroup> nodeGroupArgument(String nodeName) {
		return new CustomArgument<>(new NamespacedKeyArgument(nodeName), info -> {
			RoadMap roadMap = CommandUtils.getSelectedRoadMap(info.sender());
			NodeGroup group = roadMap.getNodeGroup(info.currentInput());
			if (group == null) {
				throw new CustomArgument.CustomArgumentException("abc");
			}
			return group;
		}).includeSuggestions(ArgumentSuggestions.strings(info -> {
			RoadMap roadMap = CommandUtils.getSelectedRoadMap(info.sender(), false);
			if (roadMap == null) {
				return new String[0];
			}
			return roadMap.getGroups().values().stream().map(NodeGroup::getKey).map(NamespacedKey::toString).toArray(String[]::new);
		}));
	}

}
