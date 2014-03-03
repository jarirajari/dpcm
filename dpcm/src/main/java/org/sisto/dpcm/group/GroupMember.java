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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jgroups.logging.Log;
import org.jgroups.logging.LogFactory;

import org.sisto.dpcm.domain.DomainCoordinator;
import org.sisto.dpcm.group.RPCQuery.RPCResult;

public class GroupMember implements GroupMessageListener, MemberRole {
	
	private static Log log = LogFactory.getLog(GroupMember.class.getName());
	public static enum Role { LEADER, FOLLOWER, MEMBER };
	private Group cluster;
	private String clusterName = "dpcm-cluster";
	private DomainCoordinator domainCoord;
	private AtomicBoolean initialized = new AtomicBoolean(false);
	
	public GroupMember(String name) {
		this.clusterName = name;
		this.domainCoord = new DomainCoordinator(this);
	}
	
	public boolean isGroupLeader() { 
		boolean is = (this.cluster == null) ? false : this.cluster.isGroupLeader();
		
		return is;
	}
	
	public boolean isGroupFollower() {
		boolean is = (this.cluster == null) ? true : this.cluster.isGroupFollower();
		
		return is;
	}
	
	private int getClusterGroupSize() {
		int size = (this.cluster == null) ? 0 : this.cluster.groupSize();
		
		return size;
	}
	
	public boolean isRestartAllowed() {
		final int ATLEAST_ONE_RESERVE_HOST = 2;
		boolean allowed = false;
		int size = this.getClusterGroupSize();
		
		if (size >= ATLEAST_ONE_RESERVE_HOST) {
			allowed = true;
		} else {
			allowed = false;
		}
		
		return allowed;
	}
	
	public boolean membersGroupHasDomainController() {
		final int EMPTY = 0;
		int size = this.getClusterGroupSize();
		boolean hasdc = false;
		boolean initd = initialized.get();
		
		if (initd && (size > EMPTY)) {
			RPCResult result = this.cluster.queryGroupHasDomainController();
			
			if (result == RPCResult.QUERY_OK_FAIL) {
				hasdc = false;
			} else {
				hasdc = true;
			}
		}
		
		return hasdc;
	}
	
	public boolean memberIsDomainController() {
		return (this.domainCoord.processIsDomainController());
	}
	
	public void handleConfigurationChange(GroupState newState) {
		if (this.isGroupLeader()) {
			this.cluster.handleConfigurationChange(newState);
			log.info("Configuraton changed and member is leader -> update group");
		} else {
			log.info("Configuraton changed but member not leader -> ignore change");
		}
	}
	
	private void createDomain() {
		this.domainCoord.start();
		if (this.isGroupFollower()) {
			synchronized (this.initialized) {
				while (! this.initialized.get()) {
					try {
						log.debug("Configuration waiting for initialization");
						this.initialized.wait();
					} catch (InterruptedException ie) {
						
					}
				}
			}
		}
		log.debug("Configuration initialization...");
		this.domainCoord.start();
	}
	
	private void destroyDomain() {
		this.domainCoord.stop();
		this.initialized.set(false);
	}
	
	public void receive(GroupMessage gm) {
		boolean follower = this.isGroupFollower();
		boolean initialization = gm.isInitialization();
		
		if (initialization) {
			log.debug("Received initialization");
			this.initialized.set(true);
			if (follower) {
				synchronized (this.initialized) {
					this.initialized.notify();
				}
			}
		}
	}
	
	public void send(GroupMessage gm) {
		this.cluster.send(gm);
	}
	
	public Group getGroup() {
		return this.cluster;
	}
	
	public void initMember() {
		boolean initialized = false;
		
		cluster = new Group(this.clusterName);
		initialized = cluster.joinGroup(this);
		
		if (initialized) {
			this.createDomain();
			log.debug("Initialized and created domain...");
		} else {
			this.destroyDomain();
			log.debug("Could not initialize. Destroying domain...");
		}
	}
	
	public void lizeMember() {
		this.destroyDomain();
		cluster.leaveGroup(this);
	}
	
	@SuppressWarnings("unused")
	private void stdinBroadcastLoop() {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		OutputStreamWriter out = new OutputStreamWriter(System.out);
		
		while(true) {
			try {
				out.append("> ");
				out.flush();
				String line = in.readLine().toLowerCase();
				if (line.startsWith("quit")) {
					break;
				} else if (line.startsWith("help")) {
					out.append("use 'quit' to quit");
				} else {
					line = String.format("[ %s ]: ", this.clusterName, line);
				}
				GroupMessage gm = new GroupMessage();
				gm.setType(GroupEvent.EventType.NORMAL_MESSAGE);
				gm.setMessage(line);
				send(gm);
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}
}
