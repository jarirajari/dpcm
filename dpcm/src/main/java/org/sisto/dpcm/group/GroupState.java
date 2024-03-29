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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jgroups.logging.Log;
import org.jgroups.logging.LogFactory;

import org.sisto.dpcm.configuration.MonitoredConfiguration;

public class GroupState implements Serializable {
	
	private static final long serialVersionUID = -724109193678642701L;
	private static transient Log log = LogFactory.getLog(GroupState.class.getName());
	/**
	 * Dont't remove configuration since it is the field to be Serialized!
	 **/
	private MonitoredConfiguration configuration = null;
	
	public GroupState() {
		this.configuration = null;
	}
	
	public GroupState(MonitoredConfiguration c) {
		this.configuration = c;
	}
	
	public static GroupState cloneGroupState(GroupState src) {
		GroupState dst = null;
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ByteArrayInputStream bis = null;
		
		try {
			oos = new ObjectOutputStream(bos);
			oos.writeObject(src);
			oos.flush();
		} catch (IOException ioe) {
			log.error("Could not clone (read) group state"+ioe.getMessage());
		} finally {
			close(oos);
		}
		
		byte[] bytes = bos.toByteArray();
		try {
			bis = new ByteArrayInputStream(bytes);
			ois = new ObjectInputStream(bis);
			dst = (GroupState) ois.readObject();
		} catch (IOException ioe) {
			log.error("Could not clone (write) group state"+ioe.getMessage());
		} catch (ClassNotFoundException cnfe) {
			log.error("Could not clone group state", cnfe);
		} finally {
			close(ois);
			close(bos);
			close(bis);
		}
		log.debug("Cloned group state!");
		
		return dst;
	}
	
	private static void close(Closeable c) {
		if (c == null) 
			return;
		try {
			c.close();
		} catch (IOException ioe) { }
	}
}
