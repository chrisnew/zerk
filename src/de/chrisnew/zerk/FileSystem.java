package de.chrisnew.zerk;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * simple wrapper for IO methods
 *
 * I want to allow the engine mods later
 *
 * SO PLEASE USE THIS WRAPPER FOR ANY IO PURPOSES
 *
 * @author CR
 *
 */
public class FileSystem {
	public static String getCurrentGameDirectory() {
		return "base";
	}

	private static String makeNativePath(String inputPath) {
		// if current game dir differs from base dir, then
		//  1. check if there
		// if not, then
		//  2. check base
		// if not, then
		//  3. throw exception

		return getCurrentGameDirectory() + "/" + inputPath;
	}

	public static FileInputStream read(String filename) throws IOException {
		return new FileInputStream(makeNativePath(filename));
	}

	public static FileOutputStream write(String filename) throws IOException {
		return new FileOutputStream(makeNativePath(filename));
	}
}
