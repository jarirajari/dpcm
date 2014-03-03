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

import org.jgroups.Message;
import org.junit.Test;
import org.sisto.dpcm.group.GroupEvent.EventType;

public class GroupEventTest {
	
	@Test
	public void testNotification() {
		GroupEvent ge = new GroupEvent(EventType.NOTIFICATION, new Message());
		boolean notification = ge.isNotification();
		
		assertTrue(notification);
	}
	
	@Test
	public void testSetMessage() {
		Message m = new Message();
		Object o = null;
		GroupEvent ge = new GroupEvent(EventType.NORMAL_MESSAGE, null);
		
		ge.setEvent(m);
		o = ge.getEvent();
		
		assertTrue((o == m));
		assertTrue((o instanceof Message));
	}
	
	@Test
	public void testIsGroupEvent() {
		GroupEvent ge = new GroupEvent(EventType.GROUP_MESSAGE, new Message());
		boolean group = ge.isGroupEvent();
		
		assertTrue(group);
	}
}
