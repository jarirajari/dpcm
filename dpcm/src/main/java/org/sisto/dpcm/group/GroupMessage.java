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

import org.jgroups.Message;
import org.jgroups.logging.Log;
import org.jgroups.logging.LogFactory;
import org.sisto.dpcm.group.GroupEvent.EventType;

public class GroupMessage {
	
	private static Log log = LogFactory.getLog(GroupMessage.class.getName());
	private static Character SPLIT = ':';
	private static String PROTOCOL_ID = "dpcm";
	private static int PROTOCOL_VERSION = 1;
	private static GroupEvent.EventType PROTOCOL_TYPE = null;
	private static int PROTOCOL_COUNT = 4; // [id:version:type:msg]
	private GroupEvent.EventType eventType;
	private String groupMessage;
	
	public GroupMessage() { }
	
	public GroupMessage(String s) {
		this.groupMessage = s;
	}
	
	public GroupMessage(GroupEvent.EventType event) {
		this.eventType = event;
	}
	
	public GroupEvent.EventType getType() {
		return (this.eventType);
	}
	
	public String getMessage() {
		return (this.groupMessage);
	}
	
	public void setType(GroupEvent.EventType et) {
		this.eventType = et;
	}
	
	public void setMessage(String s) {
		this.groupMessage = s;
	}
	
	public String encodeMessage() {
		return (this.withHeader());
	}
	
	public String decodeMessage() {
		return (this.withoutHeader());
	}
	
	public static GroupMessage convertEventToMessage(GroupEvent ge) {
		GroupEvent.EventType et = ge.getType();
		Message m = (Message) ge.getEvent();
		String decoded = (m == null) ? "" : m.toStringAsObject();
		GroupMessage gm = new GroupMessage();
		gm.setType(et);
		gm.setMessage(decoded);
		
		return gm;
	}
	
	public static GroupEvent.EventType isGroupMessage(Message m) {
		String message = m.toStringAsObject();
		String[] parts = message.split(Character.toString(SPLIT));
		GroupEvent.EventType type = null;
		int size = parts.length;
		
		if (size == PROTOCOL_COUNT) {
			String pid = parts[PROTOCOL_COUNT-(size--)];
			String pversion = parts[PROTOCOL_COUNT-(size--)];
			String ptype = parts[PROTOCOL_COUNT-(size--)];
			boolean correctPid = pid.equals(PROTOCOL_ID);
			boolean correctType = GroupEvent.isGroupEvent(ptype);
			boolean correctVersion = pversion.equals(String.valueOf(PROTOCOL_VERSION));
			
			if (correctPid && correctType && correctVersion) {
				type = GroupEvent.EventType.valueOf(ptype);
			} else {
				type = null;
			}
			log.debug(String.format("is group message (%s %s %s %s)", pid, pversion, ptype, message));
		} else {
			log.debug(String.format("not a group message (%s)", message));
		}
		
		return type;
	}
	
	private String withHeader() {
		String msg = this.groupMessage.replace(SPLIT, ' ');
		String type = this.eventType.name();
		StringBuilder header = new StringBuilder("");
		
		header.append(PROTOCOL_ID);
		header.append(SPLIT);
		header.append(PROTOCOL_VERSION);
		header.append(SPLIT);
		header.append(type);
		header.append(SPLIT);
		header.append(msg);
		
		return (header.toString());
	}
	
	@Override
	public String toString() {
		return (this.withHeader());
	}
	
	private String withoutHeader() {
		String message = this.groupMessage.toString();
		String[] parts = message.split(Character.toString(SPLIT));
		int size = parts.length;
		String msg = null;
		
		if (size == PROTOCOL_COUNT) {
			String pid = parts[PROTOCOL_COUNT-(size--)];
			String pversion = parts[PROTOCOL_COUNT-(size--)];
			String ptype = parts[PROTOCOL_COUNT-(size--)];
			String pmsg = parts[PROTOCOL_COUNT-(size)];
			boolean correctPid = pid.equals(PROTOCOL_ID);
			boolean correctType = GroupEvent.isGroupEvent(ptype);
			boolean correctVersion = pversion.equals(String.valueOf(PROTOCOL_VERSION));
			
			if (correctPid && correctType && correctVersion) {
				msg = pmsg;
			} else {
				msg = null;
			}
		}
		
		return msg;
	}
	
	public boolean isInitialization() {
		boolean initialize = false;
		EventType type = this.eventType;
		
		if (type == EventType.INITIALIZE) {
			initialize = true;
		} else {
			initialize = false;
		}
		
		return initialize;
	}
}
