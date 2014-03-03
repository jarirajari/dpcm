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
import org.jgroups.View;
import org.jgroups.logging.Log;
import org.jgroups.logging.LogFactory;

public class GroupEvent extends GenericEvent<GroupEvent.EventType> {
	
	private static Log log = LogFactory.getLog(GroupEvent.class.getName());
	public enum EventType { INITIALIZE, NOTIFICATION, GROUP_MESSAGE, NORMAL_MESSAGE, MERGE_VIEW, NORMAL_VIEW } ;
	private Message notification = null;
	private Message group_message = null;
	private Message normal_message = null;
	private View merge_view = null;
	private View normal_view = null;
	
	public GroupEvent(EventType et, Object o) {
		super(et);
		this.setEvent(o);
	}
	
	public Object getEvent() {
		EventType et = super.getType();
		
		switch (et) {
		case NOTIFICATION:
			return (this.notification);
		case GROUP_MESSAGE:
			return (this.group_message);
		case NORMAL_MESSAGE:
			return (this.normal_message);
		case MERGE_VIEW:
			return (this.merge_view);
		case NORMAL_VIEW:
			return (this.normal_view);
		default:
			return (null);
		}
	}
	
	public void setEvent(Object content) {
		EventType et = super.getType();
		
		switch (et) {
		case NOTIFICATION:
			this.notification = (Message) content;
			break;
		case GROUP_MESSAGE:
			this.notification = (Message) content;
			break;
		case NORMAL_MESSAGE:
			this.normal_message = (Message) content;
			break;
		case MERGE_VIEW:
			this.merge_view = (View) content;
			break;
		case NORMAL_VIEW:
			this.normal_view = (View) content;
			break;
		default:
			break;
		}
	}
	
	public boolean isGroupEvent() {
		boolean event = false;
		
		if (this.getType() == null) {
			event = false;
		} else {
			event = true;
		}
		
		return event;
	}
	
	public static boolean isGroupEvent(String name) {
		boolean event = false;
		
		for (GroupEvent.EventType et : GroupEvent.EventType.values()) {
			if (name.equals(et.name())) {
				event = true;
				break;
			}
		}
		
		return event;
	}
	
	public boolean isNotification() {
		boolean notificate = false;
		EventType type = super.getType();
		
		if (type == EventType.NOTIFICATION) {
			notificate = true;
		} else {
			notificate = false;
		}
		
		return notificate;
	}
}
