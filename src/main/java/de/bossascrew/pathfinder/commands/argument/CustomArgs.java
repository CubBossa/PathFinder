package de.bossascrew.pathfinder.commands.argument;

import com.google.common.collect.Lists;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import de.bossascrew.pathfinder.node.NodeGroup;
import de.bossascrew.pathfinder.roadmap.RoadMap;
import de.bossascrew.pathfinder.util.CommandUtils;
import dev.jorel.commandapi.SuggestionInfo;
import dev.jorel.commandapi.arguments.*;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@UtilityClass
public class CustomArgs {

	private static final Collection<String> TAGS = Lists.newArrayList("<rainbow>", "<gradient>", "<click>", "<hover>",
			"<rainbow:", "<gradient:", "<click:", "<hover:");

	static {
		TAGS.addAll(NamedTextColor.NAMES.keys().stream()
				.map(s -> "<" + s + ">").collect(Collectors.toList()));
		TAGS.addAll(TextDecoration.NAMES.keys().stream()
				.map(s -> "<" + s + ">").collect(Collectors.toList()));
	}

	private static final Pattern MINI_FINISH = Pattern.compile(".*(</?[^<>]*)");
	private static final Pattern MINI_CLOSE = Pattern.compile(".*<([^/<>:]+)(:[^/<>]+)?>[^/<>]*");

	public Argument<String> miniMessageArgument(String nodeName, Function<SuggestionInfo, Collection<String>> supplier) {
		return new GreedyStringArgument(nodeName).replaceSuggestions((info, builder) -> {

			int offset = builder.getInput().length();
			String[] splits = info.currentInput().split(" ", -1);
			String in = splits[splits.length - 1];
			StringRange range = StringRange.between(offset - in.length(), offset);

			List<Suggestion> suggestions = new ArrayList<>();
			StringRange fRange = range;
			supplier.apply(info).stream()
					.filter(string -> string.startsWith(in))
					.map(string -> new Suggestion(fRange, string))
					.forEach(suggestions::add);

			Matcher m = MINI_FINISH.matcher(info.currentInput());
			if (m.matches()) {
				MatchResult result = m.toMatchResult();
				range = StringRange.between(result.start(1), result.end(1));
				String filter = result.group(1);
				StringRange finalRange = range;
				TAGS.stream()
						.filter(s -> s.startsWith(filter))
						.map(s -> new Suggestion(finalRange, s))
						.forEach(suggestions::add);

				suggestions.add(new Suggestion(range, m.group(1) + ">"));
			} else {
				Matcher matcher = MINI_CLOSE.matcher(info.currentArg());
				if (matcher.matches()) {
					suggestions.add(new Suggestion(StringRange.at(offset), "</" + matcher.group(1) + ">"));
				}
			}
			return CompletableFuture.completedFuture(Suggestions.create(builder.getInput(), suggestions));
		});
	}

	public ArgumentSuggestions suggestNamespacedKeys(Function<CommandSender, Collection<NamespacedKey>> keysSupplier) {
		return (suggestionInfo, suggestionsBuilder) -> {

			Collection<NamespacedKey> keys = keysSupplier.apply(suggestionInfo.sender());
			String[] splits = suggestionInfo.currentInput().split(" ", -1);
			String in = splits[splits.length - 1];
			int sLen = suggestionInfo.currentInput().length();
			StringRange range = StringRange.between(sLen - in.length(), sLen);

			List<Suggestion> suggestions = keys.stream()
					.filter(key -> key.getKey().startsWith(in) || key.getNamespace().startsWith(in))
					.map(NamespacedKey::toString)
					.map(s -> new Suggestion(range, s))
					.collect(Collectors.toList());

			return CompletableFuture.completedFuture(Suggestions.create(suggestionsBuilder.getInput(), suggestions));
		};
	}

	public Argument<String> suggestCommaSeparatedList(String node) {
		return new GreedyStringArgument(node).replaceSuggestions((suggestionInfo, suggestionsBuilder) -> {

			StringRange range = StringRange.at(suggestionInfo.currentInput().length());
			List<Suggestion> suggestions = Lists.newArrayList("abc", "def", "ghi").stream()
					.map(s -> new Suggestion(range, s))
					.collect(Collectors.toList());

			return CompletableFuture.completedFuture(Suggestions.create(suggestionsBuilder.getInput(), suggestions));
		});
		/*return (suggestionInfo, suggestionsBuilder) -> {
			if (!suggestionInfo.currentArg().matches("[0-9\\w,_]")) {
				throw new SimpleCommandExceptionType(() -> "Comma separated lists may only contain alphanumeric values and commas.").create();
			}
			StringRange range = StringRange.at(suggestionsBuilder.getInput().length());
			List<Suggestion> suggestions = new ArrayList<>();
			if (!suggestionInfo.currentArg().endsWith(",")) {
				suggestions.add(new Suggestion(range, ","));
			}
			return CompletableFuture.completedFuture(Suggestions.create(suggestionsBuilder.getInput(), suggestions));
		};*/
	}

	public Argument<NodeGroup> nodeGroupArgument(String nodeName) {
		return new CustomArgument<>(new NamespacedKeyArgument(nodeName), info -> {
			RoadMap roadMap = CommandUtils.getSelectedRoadMap(info.sender());
			NodeGroup group = roadMap.getNodeGroup(info.currentInput());
			if (group == null) {
				throw new CustomArgument.CustomArgumentException("abc");
			}
			return group;
		}).replaceSuggestions(suggestNamespacedKeys(sender -> {
			RoadMap roadMap = CommandUtils.getSelectedRoadMap(sender, false);
			if (roadMap == null) {
				return new HashSet<>();
			}
			return roadMap.getGroups().values().stream().map(NodeGroup::getKey).collect(Collectors.toList());
		}));
	}

}
