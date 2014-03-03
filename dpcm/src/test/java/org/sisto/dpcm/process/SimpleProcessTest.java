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
package org.sisto.dpcm.process;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SimpleProcessTest {
	
	@SuppressWarnings("deprecation")
	@Test
	public void testSimpleProcessControls() {
		
		SimpleProcess cp = new SimpleProcess();
		boolean started = false;
		boolean running = false;
		boolean stopped = false;
		started = cp.processExists();
		cp.createProcess();
		running = cp.processExists();
		cp.destroyProcess();
		stopped = cp.processExists();
		
		assertEquals(false, started);
		assertEquals(false, running); // The process cannot be started because it is not found in 'lib'
		assertEquals(false, stopped);
	}
}
