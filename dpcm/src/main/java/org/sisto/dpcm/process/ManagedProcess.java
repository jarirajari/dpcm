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
package org.sisto.dpcm.process;

import java.util.ServiceLoader;

import org.jgroups.logging.Log;
import org.jgroups.logging.LogFactory;
import org.sisto.dpcm.manager.ProcessManager;
import org.sisto.dpcm.process.API.LifeCycleProcess;

public class ManagedProcess {
	
	private static Log log = LogFactory.getLog(ManagedProcess.class.getName());
	private static String defaultService = "org.sisto.dpcm.process.API.LifeCycleProcessImpl";
	private LifetimeProcess jvmProcess; // hard control:  create and destroy, no communication with the new process
	private LifeCycleProcess clientProcess; // soft control: more sophisticated queries for the new process
	private boolean serviceLoaded = false;
	private ProcessManager manager;
	private ProcessState processState;
	private String APIName = null;
	private boolean loadService = true;
	
	public ManagedProcess(ProcessManager manager, boolean loadService) {
		this.manager = manager;
		this.jvmProcess = new ComplexProcess();
		this.processState = new ProcessState(this);
		this.APIName = this.manager.loadCorrectAPI();
		this.loadService = loadService;
		this.clientProcess = (this.loadService) ? this.loadLifeCycleProcessImplementation(APIName) : null;
	}
	
	public void start() {
		boolean startOnlyStopped = (! this.jvmProcess.processExists());
		boolean serviceOK = (this.clientProcess == null) ? false : true;
		
		if (startOnlyStopped) {
			log.debug("Staring managed process...");
			this.serviceLoaded = serviceOK;
			this.jvmProcess.createProcess();
			if (this.serviceLoaded) {
				this.processState.bootProcessStart();
				this.clientProcess = this.loadLifeCycleProcessImplementation(APIName);
			}
		}
	}
	
	public void stop() {
		if (this.serviceLoaded) {
			this.processState.bootProcessStop();
			this.clientProcess.commitSuicideMan(true);
		}
		this.jvmProcess.destroyProcess();
		this.serviceLoaded = false;
	}
	
	private void hookBoot(LifeCycleProcess.Phase p) {
		if (this.clientProcess != null) {
			this.clientProcess.domainControllerBootHook(p);
		}
	}
	
	public void restart(boolean restartAsDomainController) {
		boolean startOnlyStopped = (! this.jvmProcess.processExists());
		
		if (restartAsDomainController) {
			this.hookBoot(LifeCycleProcess.Phase.PRE_STOP);
			stop();
			this.hookBoot(LifeCycleProcess.Phase.POST_STOP);
			log.info("restart-dc-stop");
			if (startOnlyStopped) {
				this.hookBoot(LifeCycleProcess.Phase.PRE_START);
				start();
				this.hookBoot(LifeCycleProcess.Phase.POST_START);
				log.info("restart-dc-start");
			}
		} else {
			stop();
			log.info("restart-pc-stop");
			if (startOnlyStopped) {
				start();
				log.info("restart-pc-start");
			}
		}
	}
	
	public boolean isRunning() {
		boolean running = false;
		boolean jvmProcessOK = (this.jvmProcess.processExists());
		boolean cliProcessOK = (this.clientProcess != null && this.clientProcess.isProcessRunning()) ? true : false;
		
		log.debug(String.format("Managed process JVM=%s and CLI=%s", jvmProcessOK, cliProcessOK));
		if (jvmProcessOK && (this.loadService == false)) {
			running = true;
		} else if (jvmProcessOK && cliProcessOK) {
			running = true;
		} else {
			running = false;
		}
		
		return running;
	}
	
	public boolean isProcessController() {
		boolean isPC = false;
		
		isPC = (! isDomainController());
		
		return isPC;
	}
	
	public boolean isDomainController() {
		boolean isDC = false;
		
		isDC = (this.clientProcess != null && this.clientProcess.isDomainController()) ? true : false;
		
		return isDC;
	}
	
	private LifeCycleProcess loadLifeCycleProcessImplementation(String correctAPI) {
		LifeCycleProcess lcp = null;
		String name;
		
		if (lcp == null) {
			log.debug("Loader trying to load API implementation");
			if (clientProcess == null) {
				lcp = loadService(LifeCycleProcess.class, correctAPI);
			} else {
				lcp = clientProcess;
			}
		}
		name = (lcp == null) ? "N/A" : lcp.getName();
		log.info(String.format("Loader loaded the life cycle process implementation: %s", name));
		
		return lcp;
	}
	
	private <T> T loadService(Class<T> clazz, String correctAPI) {
		T serviceImpl = null;
		T defaultImpl = null;
		T usedService = null;
		ServiceLoader<T> impls = ServiceLoader.load(clazz);
		boolean defaultFound = false;
		boolean serviceFound = false;
		
		for (T impl : impls) {
			String cn = impl.getClass().getCanonicalName();
			log.debug(String.format("Loader found %s", cn));
			
			if (cn.equals(correctAPI)) {
				serviceFound = true;
				serviceImpl = impl;
			} else if (cn.equals(defaultService)) {
				defaultFound = true;
				defaultImpl = impl;
			} else {
				continue;
			}
		}
		if (serviceFound) {
			usedService = serviceImpl; 
		} else {
			usedService = defaultImpl;
		}
		
		return usedService;
	}
}
