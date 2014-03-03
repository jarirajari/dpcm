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
package org.sisto.dpcm.domain;

import org.jgroups.logging.Log;
import org.jgroups.logging.LogFactory;
import org.sisto.dpcm.DPCM;
import org.sisto.dpcm.configuration.MonitoredConfiguration;
import org.sisto.dpcm.group.GroupMember;
import org.sisto.dpcm.group.GroupState;
import org.sisto.dpcm.manager.ProcessManager;
import org.sisto.dpcm.monitor.Monitor;
import org.sisto.dpcm.monitor.MonitorType;
import org.sisto.dpcm.monitor.Monitorable;
import org.sisto.dpcm.monitor.MonitorableOwner;
import org.sisto.dpcm.process.ManagedProcess;
import org.sisto.dpcm.util.ConfigUnit;

public class DomainCoordinator implements MonitorableOwner<Monitorable> {
	private static Log log = LogFactory.getLog(DomainCoordinator.class.getName());
	private ProcessManager procManager;
	private MonitoredConfiguration configuration;
	private Monitor<MonitoredConfiguration> confMonitor;
	private Monitor<ManagedProcess> procMonitor;
	private GroupMember groupMember;
	
	public DomainCoordinator(GroupMember gm){
		this.procManager = new ProcessManager(this, true);
		this.groupMember = gm;
	}
	
	public DomainCoordinator(GroupMember gm, boolean load){
		this.procManager = new ProcessManager(this, load);
		this.groupMember = gm;
	}
	
	public void setGroupMember(GroupMember gm) {
		this.groupMember = gm;
	}
	
	public void setMonitoredConfiguration(MonitoredConfiguration mc) {
		this.configuration = mc;
	}
	
	public void setProcessManager(ProcessManager pm) {
		this.procManager = pm;
	}
	
	public void start() {
		String time = DPCM.ConfigService.getConfiguration("dpcm.configuration.change.check.interval", "10(s)");
		int interval = ConfigUnit.timeInMilliseconds(time);
		
		log.debug("Starting domain coordinator");
		this.procManager.start();
		this.startConfigurationMonitor("dpcm-configuration", interval);
		this.startProcessMonitor("dpcm-process", interval);
	}
	
	public void stop() {
		log.debug("Stopping domain coordinator");
		this.procManager.stop();
		this.stopProcessMonitor();
		this.stopConfigurationMonitor();
	}
	
	private void startConfigurationMonitor(String name, int interval){
		this.configuration = new MonitoredConfiguration();
		int time = (interval < 0) ? 0 : interval;
		this.confMonitor = new Monitor<MonitoredConfiguration>(this, this.configuration, MonitorType.Type.CONFIG, name);
		this.confMonitor.start(time);
	}
	
	private void stopConfigurationMonitor() {
		if (this.confMonitor != null) {
			this.confMonitor.stop();
			this.confMonitor = null;
		}
	}
	
	private void startProcessMonitor(String name, int interval) {
		int time = (interval < 0) ? 0 : interval; 
		this.procMonitor = new Monitor<ManagedProcess>(this, this.procManager , MonitorType.Type.PROCESS, name);
		this.procMonitor.start(time);
	}
	
	private void stopProcessMonitor() {
		if (this.procMonitor != null) {
			this.procMonitor.stop();
			this.procMonitor = null;
		}
	}
	
	public boolean processIsDomainController() {
		ManagedProcess mp = this.procManager.getManagedProcess();
		boolean processIs = false;
		boolean isd = mp.isDomainController();
		boolean isr = mp.isRunning();
		
		processIs = (isd && isr) ? true : false;
		log.debug(String.format("Domain info about managed process: DomainController=%s and Running=%s", isd, isr));
		
		return processIs;
	}
	
	@Override
	public void handleCheck(MonitorType.Type type) {
		oneDomainControllerCheck();
		handleCheckType(type);
	}
	
	private void handleCheckType(MonitorType.Type type) {
		if (type == MonitorType.Type.CONFIG) {
			configurationCheck();
		} else if (type == MonitorType.Type.PROCESS) {
			processCheck();
		} else {
			defaultCheck();
		}
	}
	
	private synchronized void oneDomainControllerCheck() {
		boolean hasDC = this.groupMember.membersGroupHasDomainController();
		boolean isGL = this.groupMember.isGroupLeader();
		boolean restartAllowed = this.groupMember.isRestartAllowed();
		
		log.debug("Checking that rule 1 DC (and 1 GL) is satisfied...");
		if (! hasDC) {
			log.info("No domain controller was detected!");
			if (isGL) {
				log.info("Member is group leader");
				if (restartAllowed) {
					log.info("Group leader becomes the domain controller");
					this.procManager.restartAsDC();
				}
			}
		}
	}
	
	private void configurationCheck() {
		GroupState newState = new GroupState(this.configuration);
		log.debug("Domain configuration check");
		this.groupMember.handleConfigurationChange(newState);
	}
	
	private void processCheck() {
		log.debug("Domain process check");
		this.procManager.deadMansSwitch();
	}
	
	private void defaultCheck() {
		log.debug("Default domain check that does nothing...");
	}
}
