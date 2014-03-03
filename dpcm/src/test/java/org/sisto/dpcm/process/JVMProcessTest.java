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

import java.io.IOException;
import java.lang.Process;

public class JVMProcessTest {
	
	@Test(timeout = 10000)
	public void testJVMProcessControls() {
		MockJVMProcess mock = new MockJVMProcess();
		
		mock.setProcess(getMockProcess());
		mock.runProcess("test");
	}
	
	private Process getMockProcess() {
		Process p = null;
		
		try {
			p = Runtime.getRuntime().exec("java -version");
		} catch (IOException ioe) {
			p = null;
		}
		
		return p;
	}
	
	private class MockJVMProcess extends JVMProcess {
		
		public MockJVMProcess() {
			super(ProcessType.COMPLEX);
		}
		
		public void createProcess() { }
		
		public void destroyProcess() { }
	}
}
