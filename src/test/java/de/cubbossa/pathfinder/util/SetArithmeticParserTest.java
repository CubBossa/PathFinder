package de.cubbossa.pathfinder.util;

import com.google.common.collect.Lists;
import de.bossascrew.pathfinder.util.SetArithmeticParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SetArithmeticParserTest {

	private SetArithmeticParser<Collection<String>> parser;
	private final Collection<String> abc = Lists.newArrayList("A", "B", "C", "D", "E");
	private final Collection<String> numbers = Lists.newArrayList("1", "2", "3", "4", "5");
	private final Collection<String> mix = Lists.newArrayList("1", "B", "3", "D", "5", "E");
	private final Collection<Collection<String>> scope = Lists.newArrayList(abc, numbers, mix);

	@Before
	public void setupParser() {
		parser = new SetArithmeticParser<>(scope, strings -> strings);
	}

	@Test
	public void onParse0() {
		Assert.assertEquals(Lists.newArrayList("A", "B", "&", "C", "!", "D", "&", "|", "!"),
				parser.toRPN(parser.tokenize("!(A&B|!C&D)")).stream()
						.map(SetArithmeticParser.TokenMatch::match)
						.collect(Collectors.toList()));
	}

	@Test
	public void onParse1() {

		List<Collection<String>> expected = new ArrayList<>();
		expected.add(abc);
		Assert.assertEquals(expected, parser.parse("A"));
	}

	@Test
	public void onParse2() {

		List<Collection<String>> expected = new ArrayList<>();
		expected.add(numbers);
		expected.add(mix);
		Assert.assertEquals(expected, parser.parse("!A"));
	}

	@Test
	public void onParse3() {

		List<Collection<String>> expected = new ArrayList<>();
		expected.add(abc);
		expected.add(mix);
		Assert.assertEquals(expected, parser.parse("B&D&E"));
	}

	@Test
	public void onParse4() {

		List<Collection<String>> expected = new ArrayList<>();
		expected.add(mix);
		Assert.assertEquals(expected, parser.parse("E&1"));
	}
}