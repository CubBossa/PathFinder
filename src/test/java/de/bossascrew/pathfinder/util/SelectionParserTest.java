package de.bossascrew.pathfinder.util;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SelectionParserTest {

	public static final SelectionParser.Filter<String, SelectionParser.Context> LENGTH = new SelectionParser.Filter<>("length", Pattern.compile("(\\.\\.)?[0-9]+(\\.\\.)?"), (in, context) -> {
		boolean smaller = context.value().startsWith("..");
		boolean larger = context.value().endsWith("..");
		if (smaller && larger) {
			return in;
		}
		String arg = context.value();
		if (smaller) {
			arg = arg.substring(2);
		}
		if (larger) {
			arg = arg.substring(0, arg.length() - 2);
		}
		float req = Integer.parseInt(arg);
		return in.stream().filter(words -> {
			int len = words.length();
			return smaller && len <= req || larger && len >= req || len == req;
		}).collect(Collectors.toList());
	}, "..1", "3", "2..");

	public static final SelectionParser.Filter<String, SelectionParser.Context> TYPE = new SelectionParser.Filter<>("type", Pattern.compile("letter|number"), (strings, context) -> {
		return switch (context.value()) {
			case "letter" -> strings.stream().filter(string -> string.matches("[a-zA-Z]+")).collect(Collectors.toList());
			case "number" -> strings.stream().filter(string -> string.matches("[0-9]+")).collect(Collectors.toList());
			default -> strings;
		};
	}, "letter", "number");

	private static final List<String> SCOPE = Lists.newArrayList(
			"A", "B", "C", "D", "E",
			"Word", "OtherWord", "XYZ",
			"123", "00000000",
			"            ", " ", "",
			"More words than one", "Another sentence"
	);
	private SelectionParser<String, SelectionParser.Context> parser;

	@Before
	public void setup() {
		parser = new SelectionParser<>(SelectionParser.Context::new, "s");
		parser.addSelector(LENGTH);
		parser.addSelector(TYPE);
	}

	@Test
	public void testParseSelection1() {

		Assert.assertEquals(
				Lists.newArrayList("A", "B", "C", "D", "E", " ", ""),
				parser.parseSelection(SCOPE, "@s[length=..1]", ArrayList::new));
	}

	@Test
	public void testParseSelection2() {

		Assert.assertEquals(
				Lists.newArrayList("OtherWord", "00000000", "            ", "More words than one", "Another sentence"),
				parser.parseSelection(SCOPE, "@s[length=5..]", ArrayList::new));
	}

	@Test
	public void testParseSelection3() {

		Assert.assertEquals(
				Lists.newArrayList("OtherWord"),
				parser.parseSelection(SCOPE, "@s[length=5..,type=letter]", ArrayList::new));
	}

	@Test
	public void testParseSelection4() {

		Assert.assertEquals(
				Lists.newArrayList("123"),
				parser.parseSelection(SCOPE, "@s[length=..5,type=number]", ArrayList::new));
	}

	@Test
	public void testCompletion1() {
		Assert.assertEquals(
				Lists.newArrayList("@s[length=..5,type=letter", "@s[length=..5,type=number"),
				parser.completeSelectionString("@s[length=..5,type="));
	}

	@Test
	public void testCompletion2() {
		Assert.assertEquals(
				Lists.newArrayList("@s"),
				parser.completeSelectionString(""));
	}

	@Test
	public void testCompletion3() {
		Assert.assertEquals(
				Lists.newArrayList("@s[length=..5,length=", "@s[length=..5,type="),
				parser.completeSelectionString("@s[length=..5,"));
	}
}