/*
 * DPCM
 * Copyright (C) 2014 Jari Kuusisto
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.sisto.dpcm.util;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Random;

public class Util {
	
	public static void printClassPath(boolean single) {
		ClassLoader cl = ClassLoader.getSystemClassLoader();
		URLClassLoader urlcl = (URLClassLoader) cl;
		URL[] urls = urlcl.getURLs();
		if (single) {
			String cp = System.getProperty("java.class.path");
			System.out.println(cp);
		} else {
			for (URL url : urls) {
				String file = url.getFile();
				System.out.println(file);
			}
		}
	}
	
	public static String randomName() {
		Random generator = new Random();
		Integer rand = generator.nextInt();
		Integer sign = Integer.signum(rand);
		String seed = String.format("%s", sign*rand);
		
		return seed;
	}
	
	public static void noop() { }
	
	public static String parseScriptCommand(File f) {
		String cmd;
		String os = System.getProperty("os.name");
		String name = f.getAbsolutePath();
		boolean isBat = name.contains(".bat");
		boolean isWin = os.contains("windows");
		
		if (isWin && isBat) {
			cmd = String.format("%s", name);
		} else {
			cmd = String.format("sh %s", name);
		}
		
		return cmd;
	}
	
}
