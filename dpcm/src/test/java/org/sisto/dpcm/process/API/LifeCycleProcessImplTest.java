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
package org.sisto.dpcm.process.API;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.sisto.dpcm.process.API.LifeCycleProcess.Phase;

public class LifeCycleProcessImplTest {
	
	private LifeCycleProcessImpl lcp;
	
	@Before
	public void instantiate() {
		 this.lcp = new LifeCycleProcessImpl();
	}
	
	@Test
	public void testNaming() {
		final String newName = "new Name";
		
		this.lcp.setName(newName);
		
		assertEquals(newName, this.lcp.getName());
	}
	
	@Test
	public void testIsProcessRunning() {
		boolean is = this.lcp.isProcessRunning();
		
		assertTrue(is);
	}
	
	@Test
	public void testIsDomainController() {
		boolean is = this.lcp.isDomainController();
		
		assertFalse(is);
	}
	
	@Test
	public void testDomainControllerBootHook() {
		boolean hooked = this.lcp.domainControllerBootHook(Phase.NONE);
		boolean is = this.lcp.isDomainController();
		
		assertTrue(is);
		assertTrue(hooked);
	}

	@Test
	public void testCommitSuicideManDying() {
		boolean commited = this.lcp.commitSuicideMan(true);
		
		assertFalse(commited);
	}
	
	@Test
	public void testCommitSuicideManKilling() {
		boolean commited = this.lcp.commitSuicideMan(false);
		
		assertFalse(commited);
	}
}
