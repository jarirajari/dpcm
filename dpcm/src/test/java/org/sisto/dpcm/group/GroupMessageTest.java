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

import org.jgroups.Message;
import org.junit.Test;
import org.sisto.dpcm.group.GroupEvent.EventType;

public class GroupMessageTest {
	
	@Test
	public void testInitialization() {
		GroupMessage gm = new GroupMessage(EventType.INITIALIZE);
		boolean initial = gm.isInitialization();
		
		assertTrue(initial);
	}
	
	@Test
	public void testEncode() {
		String target;
		GroupMessage gm = new GroupMessage(EventType.GROUP_MESSAGE);
		
		gm.setMessage("testing-encode");
		target = gm.encodeMessage();
		
		assertEquals("dpcm:1:GROUP_MESSAGE:testing-encode", target);
	}
	
	@Test
	public void testDecode() {
		String target;
		GroupMessage gm = new GroupMessage(EventType.GROUP_MESSAGE);

		gm.setMessage("dpcm:1:GROUP_MESSAGE:testing-decode");
		target = gm.decodeMessage();
		
		assertEquals("testing-decode", target);
	}
	
	@Test
	public void testConvertEventToMessage() {
		final String target = "testing-jgroups";
		Message m = new Message(null, null, target);
		GroupEvent ge = new GroupEvent(EventType.NORMAL_MESSAGE, m);
		GroupMessage gm = GroupMessage.convertEventToMessage(ge);
		assertEquals("dpcm:1:NORMAL_MESSAGE:testing-jgroups", gm.toString());
	}
	
	@Test
	public void testIsGroupMessage() {
		final String target = "dpcm:1:GROUP_MESSAGE:testing-jgroups";
		Message m = new Message(null, null, target);
		EventType et = GroupMessage.isGroupMessage(m);
		
		assertThat(et, instanceOf(EventType.class));
	}
}
