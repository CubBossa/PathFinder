package de.cubbossa.pathfinder.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.PatternReplacementResult;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class StringCompass implements ComponentLike {

	private record Marker(String key, ComponentLike display, Supplier<Double> angle) {
	}

	private static final MiniMessage miniMessage = MiniMessage.miniMessage();

	private Component background;
	private int backgroundLength = 8;
	private int radius;
	private Supplier<Double> angle;
	private Map<String, Marker> markers;

	public StringCompass(String background, int radius, Supplier<Double> angle) {
		this.background = miniMessage.deserialize(background);
		this.backgroundLength = miniMessage.stripTags(background).length();
		this.radius = radius;
		this.angle = angle;
		this.markers = new HashMap<>();
	}

	public void addMarker(String key, String displayMiniMessage, double angle) {
		addMarker(key, miniMessage.deserialize(displayMiniMessage), () -> angle);
	}

	public void addMarker(String key, String displayMiniMessage, Supplier<Double> angle) {
		addMarker(key, miniMessage.deserialize(displayMiniMessage), angle);
	}

	public void addMarker(String key, ComponentLike display, double angle) {
		addMarker(key, display, () -> angle);
	}

	public void addMarker(String key, ComponentLike display, Supplier<Double> angle) {
		markers.put(key, new Marker(key, display, angle));
	}

	public Component asComponent() {

		// insert markers
		Map<Integer, Component> fixedMarkers = new TreeMap<>();
		for (Marker marker : markers.values()) {
			fixedMarkers.put((int) (marker.angle().get() / 360 * backgroundLength), marker.display().asComponent());
		}
		Component r = background;
		for (Map.Entry<Integer, Component> entry : fixedMarkers.entrySet()) {
			r = r.replaceText(builder -> builder
					.match(Pattern.compile("."))
					.condition((result, matchCount, replaced) -> {
						int index = matchCount - 1;
						if (entry.getKey() < index) {
							return PatternReplacementResult.STOP;
						} else if (entry.getKey() > index) {
							return PatternReplacementResult.CONTINUE;
						}
						return PatternReplacementResult.REPLACE;
					})
					.replacement(entry.getValue()));
		}

		// offset
		double angle = this.angle.get();
		int offset = (int) (angle / 360 * backgroundLength) + backgroundLength - radius;

		// repeat thrice to get the overlapping parts with radius
		Component repeated = Component.empty().append(r).append(r).append(r);

		return repeated.replaceText(builder -> builder
				.match(Pattern.compile("."))
				.condition((result, matchCount, replaced) -> matchCount <= offset || matchCount > (offset + radius * 2 + 1)
						? PatternReplacementResult.REPLACE
						: PatternReplacementResult.CONTINUE).replacement(""));

		/*


				.replaceText(builder -> builder
						.match(Pattern.compile(".{" + (backgroundLength * 3 - offset - 2 * radius - 1) + "}"))
						.once().replacement(""))
		 */
	}
}
