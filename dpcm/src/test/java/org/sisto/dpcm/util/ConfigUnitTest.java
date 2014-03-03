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

import org.junit.Assert;

import org.junit.Test;

public class ConfigUnitTest {
	
	@Test
	public void timeConversionPasses() {
		int secs101 = ConfigUnit.timeInSeconds("101 (s)");
		int secs200 = ConfigUnit.timeInSeconds("200(s)");
		
		Assert.assertEquals("Conversion failed", 101, secs101);
		Assert.assertEquals("Conversion failed", 200, secs200);
	}
	
	@Test(expected=RuntimeException.class)
	public void timeConversionFails() {
		int secs = ConfigUnit.timeInSeconds(null);
	}
}
