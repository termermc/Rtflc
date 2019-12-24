package net.termer.rtflc.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.termer.rtflc.RtflLibrary;

/**
 * Utility class to load jar libraries
 * @author termer
 * @since 1.0
 */
public class LibraryLoader {
	/**
	 * Loads a jar library
	 * @param jar The jar file
	 * @return The RtflLibrary object contained in the jar file
	 * @throws ClassNotFoundException If loading any jar resources fails, or there are any references to classes that are not available
	 * @throws IOException If reading the jar file fails
	 * @since 1.0
	 */
	public static RtflLibrary loadLibrary(File jar) throws ClassNotFoundException, IOException {
		RtflLibrary lib = null;
		
		String launch = null;
		ZipFile zf = new ZipFile(jar.getAbsolutePath());
		if(zf.isValidZipFile()) {
			JarFile jf = new JarFile(jar.getAbsolutePath());
			Enumeration<JarEntry> ent = jf.entries();
			while(ent.hasMoreElements()) {
				String name = ((JarEntry) ent.nextElement()).getName();
				if(name.toLowerCase().endsWith(".class")) {
					String clazz = name.replace("/", ".").replace(".class", "");
					if(clazz.endsWith("Library")) {
						launch = clazz;
						break;
					}
				}
			}
			jf.close();
		} else {
			throw new ZipException("File is not a valid jarfile");
		}
		if(launch != null) {
			@SuppressWarnings({ "resource", "deprecation" })
			URLClassLoader ucl = new URLClassLoader(new URL[] { jar.toURL() });
			try {
				lib = (RtflLibrary) ucl.loadClass(launch).newInstance();
			}
			catch (InstantiationException|IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		return lib;
	}
}
