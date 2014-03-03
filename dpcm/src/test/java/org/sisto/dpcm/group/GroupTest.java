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
import static org.hamcrest.CoreMatchers.*;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;
import org.sisto.dpcm.group.RPCQuery.RPCResult;

public class GroupTest {
	
	final Mockery context = new JUnit4Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
	
	@Test
	public void testRPCUninitialized() {
		Group g = new Group("dpcm-group-testing");
		RPCResult r = g.queryGroupHasDomainController();
		
		assertThat(r, instanceOf(RPCResult.class));
		assertTrue((r == RPCResult.QUERY_UNKNOWN));
	}
	
	@Test
	public void testGroupBasicProcedures() {
		boolean joined = false;
		boolean left = false;
		Group g = new Group("dpcm-group-testing");
		GroupMember gm = new GroupMember("just-another-group-member");
		
		joined = g.joinGroup(gm);
		this.block();
		left = g.leaveGroup(gm);
		
		assertTrue(joined);
		assertTrue(left);
	}
	
	private void block() {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) { }
	}
	
	@Test
	public void testMemberHasDomainControllerDefault() {
		Group g = new Group("dpcm-group-testing");
		boolean has = g.memberHasDomainController();
		
		assertFalse(has);
	}
	
	@Test
	public void testGroupSizeDefault() {
		Group g = new Group("dpcm-group-testing");
		int size = g.groupSize();
		
		assertEquals(0, size);
	}
	
	@Test
	public void testIsGroupLeader() {
		Group g = new Group("dpcm-group-testing");
		boolean is = g.isGroupLeader();
		
		assertFalse(is);
	}
	
	@Test
	public void testIsGroupFollower(){
		Group g = new Group("dpcm-group-testing");
		boolean is = g.isGroupFollower();
		
		assertTrue(is);
	}
}
