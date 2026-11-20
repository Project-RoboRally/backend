package dk.dtu.roborally.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

// Reads a file under resources as text, for tests that need a level the way an
// owner would upload it.

public final class TestResources {

	private TestResources() {
	}

	public static String read(String path) {
		try (InputStream input = TestResources.class.getClassLoader()
				.getResourceAsStream(path)) {
			if (input == null) {
				throw new IllegalArgumentException(path + " not found");
			}
			return new String(input.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
