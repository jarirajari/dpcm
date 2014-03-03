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
package org.sisto.dpcm.group;

import static org.junit.Assert.*;

import org.junit.Test;

public class GenericEventTest {
	
	public enum EnumType { GENERIC };
	
	@Test
	public void testInstantiation() {
		MockGenericEvent mge = new MockGenericEvent(EnumType.GENERIC);
		EnumType type = mge.getType();
		
		assertTrue(type == EnumType.GENERIC);
	}
	
	private class MockGenericEvent extends GenericEvent<EnumType> {
		
		public MockGenericEvent(EnumType type) {
			super(type);
		}
	}
}
