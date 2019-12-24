package net.termer.rtflc.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Utility I/O class
 * @author termer
 * @since 1.0
 */
public class IOUtils {
	/**
	 * Reads an entire file into a String
	 * @param path The file path
	 * @return The file's contents as a String
	 * @throws IOException if reading the file fails
	 * @since 1.0
	 */
	public static String readFile(String path) throws IOException {
	    return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
	}
	
	/**
	 * Writes a String to a file
	 * @param path The file path
	 * @param content The String to write to the file
	 * @param append Whether to append rather than overwriting
	 * @throws IOException if writing to the file fails
	 * @since 1.0
	 */
	public static void writeFile(String path, String content, boolean append) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(path, append));
	    writer.write(content);
	    
	    writer.close();
	}
}