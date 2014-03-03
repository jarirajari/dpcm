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

import org.sisto.dpcm.group.GroupMember;
import org.sisto.dpcm.util.Config;
import org.sisto.dpcm.util.Util;

public class DPCM {
	
	public static final String MAJOR_VERSION 	=	"1"; // major version updates
	public static final String MINOR_VERSION 	= 	"0"; // minor updates
	public static final String MAINT_VERSION 	= 	"0"; // for maintenance updates
	public static final Config ConfigService = Config.getConfig();
	private static final int EXIT_SUCCESS = 0;
	private static final String EMPTY_ARG = "";
	private static GroupMember gm;
	
	public static void main (String[] args) {
		if (printVersion(args)) {
			System.exit(DPCM.EXIT_SUCCESS);
		} else if (printHelp(args)) {
			System.exit(DPCM.EXIT_SUCCESS);
		} else {
			DPCM.shutdownHook();
		}
		init((args.length == 1) ? new String(args[0]) : Util.randomName());
	}
	
	private static void init(String name) {
		ConfigService.loadProperties();
		gm = new GroupMember(name);
		gm.initMember();
		System.out.println(String.format("Running DPCM instance %s...", name));
	}
	
	private static void lize() {
		gm.lizeMember();
	}
	
	private static void shutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() {
		    	lize();
		    	System.out.println("\nShutting down! Bye!\n");
		    }
		 });
	}
	
	private static boolean printVersion(String[] args) { 
		boolean exit = false;
		String arg = (args.length == 1) ? new String(args[0]) : EMPTY_ARG;
		
		if (arg.equals("-v") || args.equals("--version")) {
			final String version = String.format("Domain Process Controller Manager version %s", dpcmVersion());
			System.out.println(version);
			exit = true;
		} else {
			exit = false;
		}
		
		return exit;
	}
	
	private static boolean printHelp(String[] args) {
		boolean exit = false;
		String arg = (args.length == 1) ? new String(args[0]) : EMPTY_ARG;
		
		if (arg.equals("-h") || args.equals("--help")) {
			final String help = String.format("Usage: java -jar dpcm.jar [<group-member-name>]");
			System.out.println(help);
			exit = true;
		} else {
			exit = false;
		}
		
		return exit;
	}
	
	private static String dpcmVersion() {
		final String version = String.format("%s.%s.%s", DPCM.MAJOR_VERSION, DPCM.MINOR_VERSION, DPCM.MAINT_VERSION);
		
		return version;
	}
}
