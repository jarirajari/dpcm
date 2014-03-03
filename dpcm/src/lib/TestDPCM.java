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
class TestDPCM {
	public static void main(String[] args) {
		final int DEFAULT_LOOP = 5;
		final int DEFAULT_SLEEP = 5000;
		int many = args.length;
		int loop = DEFAULT_LOOP;
		int sleep = DEFAULT_SLEEP;

		try {
			if (many == 1) {
				loop = java.lang.Integer.valueOf(args[0]).intValue();
				sleep = DEFAULT_SLEEP;
			} else if (many == 2) {
				loop = java.lang.Integer.valueOf(args[0]).intValue();
				sleep = java.lang.Integer.valueOf(args[1]).intValue();
			} else {
				loop = DEFAULT_LOOP;
				sleep = DEFAULT_SLEEP;
			}
		} catch (java.lang.NumberFormatException nfe) {
				loop = DEFAULT_LOOP;
				sleep = DEFAULT_SLEEP;
		}
		
		for (int i=1; i<=loop; i++) {
			try {
				System.out.println(String.format("Test dpcm process running (%s)!", i)); //Display the string.
				Thread.sleep(sleep);
			} catch (java.lang.InterruptedException ie) {}
		}
	}
}

