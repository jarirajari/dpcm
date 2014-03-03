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
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

public class ConfigTest {
	
	private static Config c = Config.getConfig();
	
	@BeforeClass
	public static void load() {
		c.loadProperties();
	}
	
	@Test
	public void propertiesAreEvaluated() {
		final String template = "TEST%s/ENV";
		final String envVar = "$ENV";
		final String replace = "///";
		final String testProp = String.format(template, envVar);
		final Matcher m = Config.isEnvironmentVariable.matcher(testProp);
		final String actual = c.evaluate(testProp, envVar, replace);
		final String expected = String.format(template, replace);
		assertEquals("Property was not evaluated, no environment", expected, actual);
	}
	
	@Test
	public void getSingleProperty() {
		String groupName = this.c.getProperty("group.key", null);
		
		assertEquals("groupName", groupName);
	}
	
	@Test
	public void groupedPropertiesAreEvaluatedCorrectlyByKey() {
		String groupName = this.c.getProperty("group.key", null);
		String[] groupPropertyKeys = this.c.getConfigurations(groupName);
		String gk1 = groupPropertyKeys[0];
		String gk2 = groupPropertyKeys[1];
		String groupPropertyValue1 = this.c.getProperty(gk1, null);
		String groupPropertyValue2 = this.c.getProperty(gk2, null);
		
		assertEquals("groupPropertyValue1", groupPropertyValue1);
		assertEquals("groupPropertyValue2", groupPropertyValue2);
	}
}
