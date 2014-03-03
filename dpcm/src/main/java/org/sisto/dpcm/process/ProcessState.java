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

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jgroups.logging.Log;
import org.jgroups.logging.LogFactory;

public class ProcessState {
	
	private static Log log = LogFactory.getLog(ProcessState.class.getName());
	public static enum States { WAITING, STARTING, RUNNING, STOPPING };
	protected Thread processControlStart;
	protected Thread processControlStop;
	private ManagedProcess managed;
	private volatile States currentState = States.WAITING;
	private final Lock lock = new ReentrantLock();
	private Condition started = lock.newCondition();
	private Condition stopped = lock.newCondition();
	private ProcessBootStart bootStart = new ProcessBootStart();
	private ProcessBootStop bootStop = new ProcessBootStop();
	
	public ProcessState() {
		this.managed = null;
	}
	
	public ProcessState(ManagedProcess mp) {
		this.managed = mp;
	}
	
	public void bootProcessStart() {
		this.lock.lock();
		this.bootStop.forceInterrupt();
		this.processControlStart = new Thread(bootStart, "process-boot-start");
		this.processControlStart.start();
		
		try {
			log.info("Starting boot sequence is waiting for startup");	
			this.started.await();
		} catch (InterruptedException e) {
			log.debug("Starting boot sequence interrupted");
			e.printStackTrace();
		} finally {
			log.info("Starting boot sequence is done and process is running...");
			this.lock.unlock();
		}
	}
	
	private void booted(boolean startstop) {
		log.debug(String.format("%s notification", (startstop) ? "Startup" : "Bootdown"));
		this.lock.lock();
		try {
			if (startstop) {
				this.started.signalAll();
			} else {
				this.stopped.signalAll();
			}
		} finally {
			this.lock.unlock();
		}
	}
	
	public void bootProcessStop() {
		this.lock.lock();
		this.bootStart.forceInterrupt();
		this.processControlStop = new Thread(bootStop, "process-boot-stop");
		this.processControlStop.start();
		
		try {
			log.info("Stopping boot sequence is waiting for shutdown");
			this.stopped.await();
		} catch (InterruptedException e) {
			log.debug("Stopping boot sequence interrupted");
			e.printStackTrace();
		} finally {
			log.info("Stopping boot sequence is done and process is waiting...");
			this.lock.unlock();
		}
	}
	
	public boolean isInRunningState() {
		boolean running = false;
		
		synchronized (this.currentState) {
			running = (this.currentState == ProcessState.States.RUNNING) ? true : false;
		}
		
		return running;
	}
	
	private boolean isProcessRunning() {
		boolean running = false;
		
		if (this.managed == null) {
			running = true;
		} else {
			running = this.managed.isRunning();
		}
		
		return running;
	}
	
	private class ProcessBootStart implements Runnable {
		
		private boolean force = false;
		
		private boolean isRunning() {
			boolean running = false;
			boolean clientStarted = ProcessState.this.isProcessRunning();
			
			running = (clientStarted);
			
			return running;
		}
		
		private void bootState(ProcessState.States s) {
			
			synchronized (ProcessState.this.currentState) {
				ProcessState.this.currentState = s;
			}
		}
		
		public void forceInterrupt() {
			this.force = true;
		}
		
		public void run() {
			final int SLEEP_MS = 1000;
			
			bootState(ProcessState.States.STARTING);
			while(! isRunning() && (force == false)) {
				log.debug("Still starting up");
				try {
					Thread.sleep(SLEEP_MS);
				} catch (InterruptedException e) {
					
				}
			}
			ProcessState.this.booted(true);
			log.debug("Process startup");
			bootState(ProcessState.States.RUNNING);
		}
	}
	
	private class ProcessBootStop implements Runnable {
		
		private boolean force = false;
		
		private boolean isRunning() {
			boolean runningProcess = ProcessState.this.isProcessRunning();
			boolean running = false;
			
			running = (runningProcess);
			
			return running;
		}
		
		private void bootState(ProcessState.States s) {
			
			synchronized (ProcessState.this.currentState) {
				ProcessState.this.currentState = s;
			}
		}
		
		public void forceInterrupt() {
			this.force = true;
		}
		
		public void run() {
			final int SLEEP_MS = 1000;
			
			bootState(ProcessState.States.STOPPING);
			while(isRunning() && (force == false)) {
				log.debug("Still shutting down");
				try {
					Thread.sleep(SLEEP_MS);
				} catch (InterruptedException e) {
					
				}
			}
			ProcessState.this.booted(false);
			log.debug("Process shutdown");
			bootState(ProcessState.States.WAITING);
		}
	}

}
