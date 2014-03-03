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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.MergeView;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.blocks.locking.LockService;
import org.jgroups.logging.Log;
import org.jgroups.logging.LogFactory;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;
import org.jgroups.util.Util;

import static org.sisto.dpcm.DPCM.ConfigService;
import org.sisto.dpcm.group.GroupEvent.EventType;
import org.sisto.dpcm.group.GroupMessage;

public class Group implements RPCQuery { 
	
	public static String GROUP_LOCK = "dpcm-group-lock";
	private static Log log = LogFactory.getLog(Group.class.getName());
	private JChannel channel;
	private boolean lockingServiceRequired = false;
	private boolean followsChannel = false;
	private String groupName; // aka cluster
	private ArrayList<GroupMember> groupMembers = new ArrayList<GroupMember>();
	private GroupReceiver groupReceiver = new GroupReceiver();
	private GroupState groupState = new GroupState();
	private LockService lockService;
	private RpcDispatcher rpcDispatcher;
	
	public Group(String name) {
		this.groupName = (name == null || name.isEmpty()) ? "dpcm-group-default" : new String("dpcm-group-"+name);
	}
	
	public void setChannel(JChannel jch) {
		this.channel = jch;
	}
	
	public RPCResult queryGroupHasDomainController() {
		final String RPC_METHOD = "memberHasDomainController";
		final int RPC_TIMEOUT_MS = 10000;
		int howMany = 0;
		RPCResult result = RPCResult.QUERY_UNKNOWN;
		Method method;
		MethodCall call;
		RequestOptions options = new RequestOptions(ResponseMode.GET_ALL, RPC_TIMEOUT_MS);
		RspList<Boolean> responses = new RspList<Boolean>();
		Iterator<Entry<Address, Rsp<Boolean>>> i;
		
		if (this.rpcDispatcher == null) {
			result = RPCResult.QUERY_UNKNOWN;
		} else {
			try {
				method = this.getClass().getMethod(RPC_METHOD, (Class<?>[]) null);
				call = new MethodCall(method);
				responses = this.rpcDispatcher.callRemoteMethods(null, call, options); // NOTE! callRemoteMethods in plural!
				i = responses.entrySet().iterator();
				while (i.hasNext()) {
					Entry<Address, Rsp<Boolean>> e = i.next();
					Address a = e.getKey();
					Boolean b = e.getValue().getValue();
					howMany++;
					log.debug(String.format("Address (%s) %s the DC process...", a, (b.booleanValue()) ? "has" : "does not have"));
				}
			} catch (SecurityException e) {
				log.error("RPC security exception", e);
				result = RPCResult.QUERY_UNKNOWN;
			} catch (NoSuchMethodException e) {
				log.error("RPC invocation failed", e);
				result = RPCResult.QUERY_UNKNOWN;
			} catch (Exception e) {
				log.error("RPC invocation failed: "+e.getMessage(), e);
				result = RPCResult.QUERY_UNKNOWN;
			} finally {
				log.debug(String.format("RPC call returned: group has %s DC processes...", howMany));
				result = (howMany > 0) ? RPCResult.QUERY_OK_PASS : RPCResult.QUERY_OK_FAIL;
			}
		}
		
		return result;
	}
	
	public Boolean memberHasDomainController() {
		boolean memberHas = false;
		
		for (GroupMember gm : groupMembers) {
			memberHas = (memberHas || gm.memberIsDomainController());
		}
		log.info(String.format("RPC call result: member %s the domain controller role" , (memberHas ? "has" : "does not have")));
		
		return memberHas;
	}
	
	public Integer groupSize() {
		View v = null;
		int size = 0;
		
		if (this.channel == null) {
			size = 0;
		} else if (this.channel.isConnected()) {
			v = this.channel.getView();
			size = v.getMembers().size();
		} else {
			size = 0;
		}
		
		return size;
	}
	
	public boolean isOwnAddress(Address addr) {
		final int EQUALS = 0;
		boolean own = false;
		
		own = (this.followsChannel && (this.channel.getAddress().compareTo(addr) == EQUALS)) ? true : false;
		
		return own;
	}
	
	public boolean isGroupLeader() {
		final int JGROUPS_CLUSTER_COORDINATOR = 0;
		boolean leader = false;
		View view = (this.followsChannel) ? this.channel.getView() : null;
		
		if (view != null) {
			List<Address> rest = view.getMembers();
			final Address coord = rest.get(JGROUPS_CLUSTER_COORDINATOR);
			final Address own = this.channel.getAddress(); 
			
			if (own.equals(coord)) {
				leader = true;
			} else {
				leader = false;
			}
			log.info(String.format("Checking if group leader -> %s", leader));
		}
		
		return leader;
	}
	
	public boolean isGroupFollower() {
		boolean follower = true;
		boolean leader = isGroupLeader();
		
		if (leader) {
			follower = false;
		} else {
			follower = true;
		}
		log.info(String.format("Checking if group follower -> %s", follower));
		
		return follower;
	}
	
	public void send(GroupMessage msg) {
		
		if (canSend()) {
			try {
				Message chm = new Message(null, null, msg.encodeMessage());
				this.channel.send(chm);
				log.debug("Sent msg to channel: " + msg.encodeMessage());
			} catch (Exception e) {
				log.warn("Could not send msg to channel: " + e.getMessage());
			}
		}
	}
	
	private void receive(GroupMessage msg) {
		log.info("received group message = "+msg.decodeMessage());
	}
	
	public void handleConfigurationChange(GroupState state) {
		GroupMessage notificate = new GroupMessage();
		notificate.setType(EventType.NOTIFICATION);
		notificate.setMessage("synch-request-from-coordinator");
		this.handleStateChange(state);
		this.send(notificate);
	}
	
	private void handleStateChange(GroupState state) {
		boolean leader = isGroupLeader();
		boolean follower = isGroupFollower();
		boolean connected = this.followsChannel;
		boolean statenotnull = (state == null) ? false : true;
		
		log.debug("Handling state change");
		synchronized (state) {
			if (statenotnull && connected && (leader || follower)) {
				if (leader) {
					handleStateChangeAsLeader(state);
				} else {
					handleStateChangeAsFollower(state);
				}
			}
		}
		this.handleConfigurationInitialization();
	}
	
	private void handleConfigurationInitialization() {
		GroupMessage gm = new GroupMessage();
		gm.setType(EventType.INITIALIZE);
		gm.setMessage("leader has initialized");
		this.forwardToMember(gm);
		log.debug("Forwareded initialization to member");
	}
	
	private void handleStateChangeAsLeader(GroupState state) {
		synchronized (this.groupState) {
			GroupState temp = GroupState.cloneGroupState(this.groupState);
			try {
				log.debug("Leader state change");
				this.groupState = GroupState.cloneGroupState(state);
				this.channel.startFlush(false); // startFlush's automatic_resume calls stopFlush() after the flush	
			} catch (Exception e) {
				log.error("Leader could not change state: "+e.getMessage());
				this.groupState = temp;
			} finally {
				this.channel.stopFlush();
			}
		}
	}
	
	private void handleStateChangeAsFollower(GroupState state) {
		synchronized (this.groupState) {
			GroupState temp = GroupState.cloneGroupState(this.groupState);
			try {
				log.debug("Follower state change");
				this.groupState = GroupState.cloneGroupState(state);
			} catch (Exception e) {
				log.error("Group could not change state: "+e.getMessage());
				this.groupState = temp;
			}
		}
	}
	
	private void register(GroupMember gm) {
		if (! (this.groupMembers.contains(gm))) {
			this.groupMembers.add(gm);
		}
	}
	
	private void unregister(GroupMember gm) {
		if (! (this.groupMembers.contains(gm))) {
			this.groupMembers.remove(gm);
		}
	}
	
	private void produceEvent(GroupEvent ge) {
		if (ge.isGroupEvent()) {
			if (ge.isNotification()) {
				if (this.isGroupFollower()) {
					requestStateTransfer();
				} else if (this.isGroupLeader()) {
					requestStateTransfer();
				} else {
					this.consumeEvent(ge);
				}
			}
		} else {
			this.consumeEvent(ge);
		}
		
	}
	
	private void forwardToMember(GroupMessage msg) {
		for (GroupMember gm : this.groupMembers) {
			gm.receive(msg);
		}
	}
	
	private void requestStateTransfer() {
		final int WAIT_UNTIL_STATE_TRANSFERRED = 0;
		final Address FROM_COORDINATOR = null;
		
		try {
			this.channel.getState(FROM_COORDINATOR, WAIT_UNTIL_STATE_TRANSFERRED);
		} catch (Exception e) {
			log.error("Received notification, but could not update state: "+e.getMessage(), e.getCause());
		}
	}
	
	private void consumeEvent(GroupEvent ge) {
		org.sisto.dpcm.util.Util.noop();
	}
	
	private final class GroupReceiver extends ReceiverAdapter {
		
		/**
		 * From JGroups docs (org.jgroups.ReceiverAdapter): 
		 * Called (usually by the FLUSH protocol), as an indication that the member should stop sending messages.
		 */
		public void block() {
			if (log.isDebugEnabled()) {
				log.debug("JGroups block(...)");
			}
		}
		
		/**
		 * From JGroups docs (org.jgroups.ReceiverAdapter): 
		 * Allows an application to write a state through a provided OutputStream.
		 */
		public void getState(OutputStream output) throws Exception {
			if (log.isDebugEnabled()) {
				log.debug("JGroups getState(...)");
			}
			try {
				synchronized (Group.this.groupState) {
					GroupState st = Group.this.groupState;
					handleStateChange(st);
					Util.objectToStream(st, new DataOutputStream(output));
				}
			} catch (Exception e) {
				throw e;
			}
		}
		
		/**
		 * From JGroups docs (org.jgroups.ReceiverAdapter): 
		 * Called when a message is received.
		 */
		public void receive(Message m) {
			if (log.isDebugEnabled()) {
				log.debug("JGroups receive(...)");
			}
			GroupEvent.EventType et = GroupMessage.isGroupMessage(m);
			if (et != null) {
				Group.this.produceEvent(new GroupEvent(et, m));
			}
		}
		
		/**
		 * From JGroups docs (org.jgroups.ReceiverAdapter): 
		 * Allows an application to read a state through a provided InputStream.
		 * 
		 * This method is called on the state requester, i.e. the instance which
		 * called JChannel.getState() (here the requester has follower role)
		 */
		public void setState(InputStream input) throws Exception {
			if (log.isDebugEnabled()) {
				log.debug("JGroups setState(...)");
			}
			try {
				synchronized (Group.this.groupState) {
					GroupState st = null;
					st = (GroupState) Util.objectFromStream(new DataInputStream(input));
					handleStateChange(st);
				}
			} catch (Exception e) {
				throw e;
			}
		}
		
		/**
		 * From JGroups docs (org.jgroups.ReceiverAdapter): 
		 * Called whenever a member is suspected of having crashed, but has not yet been excluded.
		 * 
		 * TODO Cluster partition handling
		 */
		public void suspect(Address suspected) {
			if (log.isDebugEnabled()) {
				log.debug("JGroups suspect(...)");
			}
		}
		
		/**
		 * From JGroups docs (org.jgroups.ReceiverAdapter): 
		 * Called after the FLUSH protocol has unblocked previously blocked senders, and messages can be sent again.
		 */
		public void unblock() {
			if (log.isDebugEnabled()) {
				log.debug("JGroups unblock(...)");
			}
		}
		
		/**
		 * From JGroups docs (org.jgroups.ReceiverAdapter): 
		 * Called when a change in membership has occurred.
		 * 
		 * TODO Cluster partition handling
		 */
		public void viewAccepted(View v) {
			if (log.isDebugEnabled()) {
				log.debug("JGroups viewAccepted(...)");
			}
			if (v instanceof MergeView) {
				new Thread() {
					public void run() {
						Group.this.lockService.unlockAll();
					}
				}.start();
			} else {
				// normal view
			}
		}
		
		
	} /* GroupReceiver class */
	
	@SuppressWarnings("unused")
	private void executeLocked() {
		Lock lock = this.lockService.getLock(GROUP_LOCK);
		
		lock.lock();
		try {
			; // do something with the locked resource
		} finally {
			lock.unlock();
		}
	}
	
	private boolean canSend() {
		if (this.followsChannel)
			return true;
		else
			return false;
	}
	
	private void initAdditionalServices() {
		boolean additionalInitialized = false;
		
		if (lockingServiceRequired) {
			this.lockService = new LockService(this.channel);
		}
		this.rpcDispatcher = new RpcDispatcher(this.channel, this);
		this.rpcDispatcher.start();
		
		additionalInitialized = (lockingServiceRequired && this.lockService != null) || (this.rpcDispatcher != null);
		log.debug(String.format("Additional JGroups services %s initialized", ((additionalInitialized) ? "were" : "were not")));
	}
	
	private void lizeAdditionalServices() {
		if (this.rpcDispatcher != null) {
			this.rpcDispatcher.stop();
			this.rpcDispatcher = null;
		}
		if (lockingServiceRequired) {
			this.lockService = null;
		}
	}
	
	private void initChannel() throws Exception {
		String file = ConfigService.resolveJgroups3Config();
		boolean isDefault = (file == null) ? true : false;
		
		try {
			if (isDefault) {
				this.channel = new JChannel();
				log.info("JGroups configuration: default");
			} else {
				this.channel = new JChannel(file);
				log.info("JGroups configuration: ".concat(file));
			}
			this.channel.setName(this.groupName);
			this.channel.setDiscardOwnMessages(false);
			this.channel.setReceiver(this.groupReceiver);
			log.info(String.format("Created JGroups channel '%s'", this.groupName));
		} catch (Exception e) {
			log.error(String.format("Could not create JGroups channel or locking service with %s: %s", file, e.getMessage()));
			throw e;
		}
	}
	
	private void lizeChannel() {
		lizeAdditionalServices();
		if (this.channel != null) {
			if (this.channel.isOpen()) {
				this.channel.close();
			}
			this.channel = null;
		}
	}
	
	public boolean joinGroup(GroupMember gm) {
		boolean joined = this.followsChannel;
		
		this.register(gm);
		if (joined == false) {
			joined = doJoinGroup();
		}
		
		return joined;
	}
	
	private boolean doJoinGroup() {
		boolean join = false;
		
		log.info("Joining group ".concat(this.groupName));
		try {
			initChannel();
			initAdditionalServices();
			if (!(this.channel.isConnected())) {
				this.channel.connect(this.groupName);
				this.channel.getState(null, 10000);
			}
			this.followsChannel = true;
		} catch (Exception e) {
			log.error("Could not join group. "+e.getMessage());
			this.followsChannel = false;
		}
		join = this.followsChannel;
		
		return join;
	}
	
	public boolean leaveGroup(GroupMember gm) {
		boolean left = !(this.followsChannel);
		
		if (left == false) {
			left = doLeaveGroup();
		}
		this.unregister(gm);
		
		return left;
	}
	
	private boolean doLeaveGroup() {
		boolean leave = false;
		
		if (this.channel.isConnected()) {
			this.channel.disconnect();
		}
		log.info("Left group ".concat(this.groupName));
		this.followsChannel = true;
		leave = this.followsChannel;
		lizeChannel();
		
		return leave;
	}
	
	@SuppressWarnings("unused")
	final private void setGroupName(String name) {}
	
	public String getGroupName() {
		return this.groupName;
	}
}
