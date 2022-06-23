package de.bossascrew.pathfinder.util;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SelectionParser<T, C extends SelectionParser.Context> {

	@RequiredArgsConstructor
	public static class Context {
		private final String value;

		public String value() {
			return value;
		}
	}

	public record Filter<N, C extends SelectionParser.Context>(String key, Pattern value,
							   BiFunction<Collection<N>, C, Collection<N>> filter,
							   String... completions) {
	}

	private static final Pattern SELECT_PATTERN = Pattern.compile("@[a-zA-Z0-9]+(\\[((.+=.+,)*(.+=.+))?])?");

	private final Collection<String> classifiers;
	private final Collection<Filter<T, C>> filters;
	@Getter
	@Setter
	private Function<String, C> contextSupplier;

	public SelectionParser(Function<String, C> contextSupplier, String... classifier) {
		this.classifiers = Lists.newArrayList(classifier);
		this.contextSupplier = contextSupplier;
		this.filters = new ArrayList<>();
	}

	public SelectionParser(Collection<Filter<T, C>> filters, Function<String, C> contextSupplier, String... classifier) {
		this.classifiers = Lists.newArrayList(classifier);
		this.contextSupplier = contextSupplier;
		this.filters = new ArrayList<>(filters);
	}

	public void addSelector(Filter<T, C> filter) {
		filters.add(filter);
	}

	public <S extends Collection<T>> S parseSelection(Collection<T> scope, String input, Supplier<S> resultFactory) {
		Matcher matcher = SELECT_PATTERN.matcher(input);
		if (!matcher.matches()) {
			throw new IllegalArgumentException("Select String must be of format @<classifier>[<key>=<value>,...]");
		}
		if (matcher.groupCount() < 2) {
			S res = resultFactory.get();
			res.addAll(scope);
			return res;
		}
		String argumentString = matcher.group(2);

		Map<Filter<T, C>, String> arguments = new HashMap<>();
		while (argumentString.length() > 0) {
			int len = argumentString.length();
			for (Filter<T, C> filter : filters) {
				if (!argumentString.startsWith(filter.key())) {
					continue;
				}
				argumentString = argumentString.substring(filter.key().length() + 1);
				Matcher m = filter.value().matcher(argumentString);
				if (!m.find() || m.start() != 0) {
					throw new IllegalArgumentException("Illegal value for key '" + filter.key() + "': " + argumentString);
				}
				arguments.put(filter, argumentString.substring(0, m.end()));
				argumentString = argumentString.substring(m.end());
				if (argumentString.startsWith(",")) {
					argumentString = argumentString.substring(1);
				}
			}
			if (len <= argumentString.length()) {
				throw new IllegalArgumentException("Illegal selection argument: " + argumentString);
			}
		}

		S result = resultFactory.get();
		result.addAll(scope);
		for (Map.Entry<Filter<T, C>, String> entry : arguments.entrySet()) {
			Collection<T> x = entry.getKey().filter().apply(result, contextSupplier.apply(entry.getValue()));
			if (x.getClass().equals(result.getClass())) {
				result = (S) x;
			} else {
				result.clear();
				result.addAll(x);
			}
		}
		return result;
	}
}
