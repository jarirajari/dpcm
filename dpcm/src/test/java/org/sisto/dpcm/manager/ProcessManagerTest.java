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
package org.sisto.dpcm.manager;

import static org.junit.Assert.*;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.Test;

public class ProcessManagerTest {
	
	final Mockery context = new JUnit4Mockery() {{
	    setThreadingPolicy(new Synchroniser());
	}};
	
	@Test
	public void testLoadCorrectAPIDefault() {
		ProcessManager pm = new ProcessManager();
		String name = pm.loadCorrectAPI();
		
		assertEquals("org.sisto.dpcm.process.API.LifeCycleProcessImpl", name);
	}
	
	@Test
	public void testCheckIsDeadDefault() {
		ProcessManager pm = new ProcessManager();
		boolean dead = pm.checkIsProcessDead();
		
		assertTrue(dead);
	}
}
