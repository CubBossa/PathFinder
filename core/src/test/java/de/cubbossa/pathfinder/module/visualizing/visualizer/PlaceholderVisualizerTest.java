package de.cubbossa.pathfinder.module.visualizing.visualizer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.junit.jupiter.api.Test;

class PlaceholderVisualizerTest {

	@Test
	void resolveDistance() {

		MiniMessage resolver = MiniMessage.builder().tags(TagResolver.empty()).build();

		assertEquals("1.2Test", resolver.serialize(resolver.deserialize("<number:'en-US':#.#>Test", Formatter.number("number", 1.23d))));
		assertEquals("<green>Test", resolver.serialize(resolver.deserialize("<green>Test")).replace("\\<", "<"));

	}
}