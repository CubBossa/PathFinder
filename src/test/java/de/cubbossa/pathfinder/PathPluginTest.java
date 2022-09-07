package de.cubbossa.pathfinder;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PathPluginTest {

	private static ServerMock server;
	private static PathPlugin plugin;

	@BeforeAll
	static void setUp() {
		server = MockBukkit.mock(new CustomMock());
		plugin = MockBukkit.load(PathPlugin.class);
	}

	@Test
	public void test() {
		Assertions.assertEquals(1, 1);
	}

	@AfterAll
	static void tearDown() {
		MockBukkit.unmock();
	}
}
