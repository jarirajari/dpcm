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
package org.sisto.dpcm;

import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StandaloneProcess implements Runnable {
	
	private static int WAIT_CONTINUE_MS = 15*1000; //Integer.MAX_VALUE
	private static boolean logging = true;
	private static Boolean just = true;
	
	public static void main(String[] args) {
		runProcess();
	}
	
	public static void runProcess() {
		new Thread(new StandaloneProcess()).start();
	}
	
	private static void printline(String line) {
		final String NEWLINE = System.getProperty("line.separator");
		DateFormat df = new SimpleDateFormat();
		String date = df.format(new Date());
		FileWriter writer = null;
		try {
			writer = new FileWriter("dpcm-default.log", true);
			writer.write(String.format("%s: %s%s", date, line, NEWLINE));
			writer.flush();
			writer.close();
		} catch (Exception e) {
			
		} finally {
			close(writer);
		}
	}
	
	private static void close(Closeable c) {
		if (c == null) 
			return;
		try {
			c.close();
		} catch (IOException ioe) { }
	}

	@Override
	public void run() {
		while (true) {
			synchronized (just) {
				try {
					if (logging) {
						printline("DPCM standalone process is running...");
					}
					StandaloneProcess.just.wait(WAIT_CONTINUE_MS);
				} catch (InterruptedException e) {
					
				}
			}
		}
	}
}
