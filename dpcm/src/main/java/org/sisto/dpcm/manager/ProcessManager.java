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
package org.sisto.dpcm.manager;

import static org.sisto.dpcm.DPCM.ConfigService;

import org.jgroups.logging.Log;
import org.jgroups.logging.LogFactory;
import org.sisto.dpcm.domain.DomainCoordinator;
import org.sisto.dpcm.monitor.Monitorable;
import org.sisto.dpcm.process.ManagedProcess;
import org.sisto.dpcm.process.API.LifeCycleProcessImpl;

public class ProcessManager implements Monitorable {
	
	private static Log log = LogFactory.getLog(ProcessManager.class.getName());
	private ManagedProcess managed;
	private DomainCoordinator domain;
	
	public ProcessManager() {
		this.domain = null;
		this.managed = new ManagedProcess(this, false);
	}
	
	public ProcessManager(DomainCoordinator coord, boolean load) {
		this.domain = coord;
		this.managed = new ManagedProcess(this, load);
	}
	
	public ManagedProcess getManagedProcess() {
		return (this.managed);
	}
	
	public void setManagedProcess(ManagedProcess mp) {
		this.managed = mp;
	}
	
	public String loadCorrectAPI() {
		final String correct = ConfigService.getConfiguration("dpcm.process.api", LifeCycleProcessImpl.class.getCanonicalName());
		
		log.debug(String.format("API name is '%s'", correct));
		
		return correct;
	}
	
	public void start() {
		this.managed.start();
	}
	
	public void stop() {
		this.managed.stop();
	}
	
	public void restartAsDC() {
		this.managed.restart(true);
	}
	
	public boolean checkIsProcessDead() {
		return (! this.managed.isRunning());
	}
	
	public void deadMansSwitch() {
		boolean dead = checkIsProcessDead();
		
		if (dead) {
			log.info("Process is dead and manager issues a restart...");
			this.managed.restart(false);
		}
	}
	
	@Override
	public boolean monitorCheck() {
		boolean dead = checkIsProcessDead();
		
		log.info(String.format("Process manager monitor check: process %s dead...", ((dead) ? "is" : "is not"))); 
		
		return dead;
	}
}
