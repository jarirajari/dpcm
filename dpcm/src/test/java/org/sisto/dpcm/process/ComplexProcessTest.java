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

import static org.junit.Assert.*;

import org.junit.Test;

public class ComplexProcessTest {
	
	@Test
	public void testComplexProcessControls() {
		ComplexProcess cp = new ComplexProcess();
		boolean started = false;
		boolean running = false;
		boolean stopped = false;
		
		started = cp.processExists();
		cp.createProcess();
		running = cp.processExists();
		cp.destroyProcess();
		stopped = cp.processExists();
		
		assertEquals(false, started);
		assertEquals(true, running);
		assertEquals(false, stopped);
	}
}
