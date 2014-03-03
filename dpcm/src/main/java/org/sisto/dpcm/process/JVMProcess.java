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

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class JVMProcess implements LifetimeProcess {
	
	public enum ProcessType { SIMPLE, COMPLEX };
	protected java.lang.Process proc;
	protected volatile AtomicBoolean running = new AtomicBoolean(false);
	protected ProcessType type;
	protected Thread processRunner;
	
	public JVMProcess(ProcessType type) {
		this.type = type;
	}
	
	public boolean processExists() {
		return (this.running.get());
	}
	
	public abstract void createProcess();
	
	public abstract void destroyProcess();
	
	public ProcessType getType() {
		return this.type;
	}
	
	public void setProcess(java.lang.Process p) {
		this.proc = p;
	}
	
	protected void runProcess(String name) {
		this.processRunner = new Thread(new ProcessRunner(), name);
		this.processRunner.start();
	}
	
	protected void cancelProcess() {
		this.running.set(false);
		this.processRunner.interrupt();
	}
	
	protected class ProcessRunner implements Runnable {
		public void run() {
			running.set(true);
			while(running.get()) {
				try {
					proc.waitFor();
					running.set(false);
				} catch (InterruptedException e) {
					
				}
			}
		}
	}
}
