package de.bossascrew.pathfinder.data;

public class DataStorageException extends RuntimeException {

	public DataStorageException(String message) {
		super(message);
	}

	public DataStorageException(String message, Exception cause) {
		super(message, cause);
	}
}
