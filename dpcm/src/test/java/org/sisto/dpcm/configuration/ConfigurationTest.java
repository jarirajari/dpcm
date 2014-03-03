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
package org.sisto.dpcm.configuration;

import org.junit.Test;

public class ConfigurationTest {
	
	@Test
	public void toProcessConfiguration() {
		MockConfiguration mc = new MockConfiguration();
		mc.toProcessConfiguration();
	}
	
	@Test
	public void saveConfiguration() {
		MockConfiguration mc = new MockConfiguration();
		mc.saveConfiguration();
	}
	
	@Test
	public void loadConfiguration() {
		MockConfiguration mc = new MockConfiguration();
		mc.loadConfiguration();
	}
	
	private class MockConfiguration extends Configuration {
		
		public MockConfiguration() {
			super();
		}

		@Override
		public boolean monitorCheck() {
			return false;
		}
	}
}
