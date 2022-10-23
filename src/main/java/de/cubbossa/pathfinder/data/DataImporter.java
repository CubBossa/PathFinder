package de.cubbossa.pathfinder.data;

import java.io.IOException;

public interface DataImporter {

	void load(DataStorage storage) throws IOException;
}
