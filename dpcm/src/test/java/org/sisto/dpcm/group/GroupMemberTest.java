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

public class GroupMemberTest {
	
	@Test
	public void testDefaultRoles() {
		GroupMember gm = new GroupMember("test-group-member");
		boolean isGL = gm.isGroupLeader();
		boolean isGF = gm.isGroupFollower();
		
		assertFalse(isGL);
		assertTrue(isGF);
	}
	
	@Test
	public void testRestartAllowedOnStart() {
		GroupMember gm = new GroupMember("test-group-member");
		boolean restartAllowed = gm.isRestartAllowed();
		
		assertFalse(restartAllowed);
	}
	
	@Test
	public void testMembersGroupHasDomainController() {
		GroupMember gm = new GroupMember("test-group-member");
		boolean has = gm.membersGroupHasDomainController();
		
		assertFalse(has);
	}
	
	@Test
	public void testMemberIsDomainController() {
		GroupMember gm = new GroupMember("test-group-member");
		boolean is = gm.memberIsDomainController();
		
		assertFalse(is);
	}
}
