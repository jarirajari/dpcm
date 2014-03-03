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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.lang.IllegalStateException;

public class ConfigUnit {
	
	private static final int MILLISECONDS = 1000;
	
	private ConfigUnit() {}
	
	public static Pattern intervalQtty = Pattern.compile("[0-9]+");
	public static Pattern intervalUnit = Pattern.compile("(h|m|s)");
	public static Pattern memoryUnitValid = Pattern.compile("[0-9]+[m|M]");
	
	public static int timeInSeconds(String interval) {
		return (timeInMilliseconds(interval)/MILLISECONDS);
	}
	
	public static int timeInMilliseconds(String interval) throws RuntimeException {
		int time = 0;
		Matcher mq = intervalQtty.matcher(interval);
		Matcher mu = intervalUnit.matcher(interval);
		String timeQtty = "0";
		String timeUnit = "s";
		
		if (mq.find() && mu.find()) {
			try {
				timeQtty = mq.group();
				timeUnit = mu.group();
				int t = Integer.valueOf(timeQtty);
				
				if (timeUnit.equals("s")) {
					time = (t * 1 * MILLISECONDS);
				} else if (timeUnit.equals("m")) {
					time = (t * 1 * 60 * MILLISECONDS);
				} else if (timeUnit.equals("h")) {
					time = (t * 1 * 60 * 60 * MILLISECONDS);
				} else {
					time = 0;
				}
				
			} catch (IllegalStateException ise) {
				throw new RuntimeException("Could not convert interval to time units!");
			} catch (NumberFormatException nfe) {
				throw new RuntimeException("Could not convert interval to time units!");
			}
		} else {
			throw new RuntimeException("Could not convert interval to time units!");
		}
		
		return time;
	}
	
	public static boolean memoryInMegaBytes(String MB) {
		Matcher mu = intervalUnit.matcher(MB);
		boolean valid = false;
		
		if (mu.find()) {
			valid = true;
		} else {
			valid = false;
		}
		
		return valid;
	}
}
