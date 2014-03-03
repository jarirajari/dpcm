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
package org.sisto.dpcm.monitor;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class Monitor<T> {
	private static String TIMER_NAME = "dpcm-monitor-";
	private AtomicBoolean initialized = new AtomicBoolean(false);
	private AtomicBoolean suspended = new AtomicBoolean(false);
	private MonitorTask monitor;
	private Timer timer;
	private MonitorableOwner<Monitorable> owner;
	private Monitorable monitorable;
	private MonitorType.Type type;
	private String name;
	
	public Monitor(MonitorableOwner<Monitorable> owner, Monitorable monitorable, MonitorType.Type type, String name) {
		this.owner = owner;
		this.monitorable = monitorable;
		this.type = type;
		this.name = name;
	}
	
	private String timerName() {
		return TIMER_NAME.concat(this.name);
	}
	
	private void create(int interval) {
		this.monitor = new MonitorTask();
		this.timer = new Timer(timerName(), true);
		this.timer.schedule (this.monitor, 0, interval);
	}
	
	private void destroy() {
		this.monitor.cancel();
		this.timer.purge();
		this.timer.cancel();
	}
	
	public void start(int interval) {
		boolean started = (this.initialized.get());
		
		if (! started) {
			this.create(interval);
			this.initialized.set(true);
		}
	}
	
	public void stop() {
		boolean stopped = (! this.initialized.get());
		
		if (! stopped) {
			this.destroy();
			this.initialized.set(false);
		}
	}
	
	public boolean running() {
		boolean susped = this.suspended.get();
		boolean strted = this.initialized.get();
		boolean running = false;
		
		if (!susped && strted) {
			running = true;
		} else {
			running = false;
		}
		
		return running;
	}
	
	public void pause() {
		this.suspended.set(true);
	}
	
	public void resume() {
		this.suspended.set(false);
	}
	
	final class MonitorTask extends TimerTask {
		
		@Override
		public void run() {
			try {
				if (suspended.get() == false) {
					boolean checked = Monitor.this.monitorable.monitorCheck();
					
					if (checked) {
						owner.handleCheck(type);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
}
