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
import org.sisto.dpcm.manager.ProcessManager;

public class ManagedProcessTest {
	
	@Test(timeout = 10000)
	public void testManagedProcessControls() {
		boolean started = false;
		boolean running = false;
		boolean stopped = false;
		ManagedProcess mp = new ManagedProcess(new ProcessManager(), false);
		
		started = mp.isRunning();
		mp.start();
		mp.start();
		running = mp.isRunning();
		mp.stop();
		mp.stop();
		stopped = mp.isRunning();
		 
		assertFalse(started);
		assertTrue(running);
		assertFalse(stopped);
	}
	
	@Test
	public void restartAsDC() {
		boolean isPC = false;
		boolean isDC = false;
		ManagedProcess mp = new ManagedProcess(new ProcessManager(), false);
		
		isPC = mp.isDomainController();
		mp.start();
		mp.stop();
		mp.restart(true);
		isDC = mp.isDomainController();
		
		assertFalse(isPC);
		assertFalse(isDC); // Only when client service is available process can become DC!
	}
	
	@Test
	public void restartAsPC() {
		boolean isPC = false;
		boolean isDC = false;
		ManagedProcess mp = new ManagedProcess(new ProcessManager(), false);
		
		isPC = mp.isDomainController();
		mp.start();
		mp.restart(false);
		isDC = mp.isDomainController();
		mp.stop();
		
		assertFalse(isPC);
		assertFalse(isDC);
	}
	
	@Test
	public void checkPCDCDefaults() {
		boolean isPC = false;
		boolean isDC = false;
		ManagedProcess mp = new ManagedProcess(new ProcessManager(), false);
		
		mp.start();
		isPC = mp.isProcessController();
		isDC = mp.isDomainController();
		mp.stop();
		
		assertTrue(isPC);
		assertFalse(isDC);
	}
}
