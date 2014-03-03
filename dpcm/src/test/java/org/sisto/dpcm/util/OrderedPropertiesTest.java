package org.sisto.dpcm.util;

import static org.junit.Assert.*;

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
import java.util.Enumeration;

import org.junit.BeforeClass;
import org.junit.Test;

public class OrderedPropertiesTest {
	
	private static OrderedProperties manualop;
	private static String keys[] = {"xkey1", "0key2", "Akey3", "Zkey4", "9key5", "zkey6"};
	
	@BeforeClass
	public static void setupProperties(){
		manualop = new OrderedProperties();
		manualop.put(keys[0], "xvalue1");
		manualop.put(keys[1], "0value2");
		manualop.put(keys[2], "avalue3");
		manualop.put(keys[3], "Zvalue4");
		manualop.put(keys[4], "9value5");
		manualop.put(keys[5], "zvalue6");
	}
	
	@Test
	public void testPropertiesAreInOriginalOrder() {
		Enumeration<?> objs = manualop.propertyNames();
		String s1 = (String) objs.nextElement();
		String s2 = (String) objs.nextElement();
		String s3 = (String) objs.nextElement();
		String s4 = (String) objs.nextElement();
		String s5 = (String) objs.nextElement();
		String s6 = (String) objs.nextElement();
		
		assertEquals(s1, keys[0]);
		assertEquals(s2, keys[1]);
		assertEquals(s3, keys[2]);
		assertEquals(s4, keys[3]);
		assertEquals(s5, keys[4]);
		assertEquals(s6, keys[5]);
	}
}
