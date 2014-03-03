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

import static org.junit.Assert.*;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MonitorTest {
	
	final Mockery context = new JUnit4Mockery() {{
	    setThreadingPolicy(new Synchroniser());
	}};
	final Sequence monseq = context.sequence("monitor-sequence");
	MonitorableOwner<Monitorable> owner = null;
	Monitor<Monitorable> monitor= null;
	Monitorable monitorable = null;
	MonitorType.Type type = null;
	String name = null;
	
	@Before
	public void initMonitor() {
		this.owner = (MonitorableOwner<Monitorable>) this.context.mock(MonitorableOwner.class);
		this.monitorable = new MockedMonitorable();
		this.type = MonitorType.Type.DEFAULT;
		this.name = "junit-test-monitor";
		this.monitor = new Monitor<Monitorable>(owner, monitorable, type, name);
	}
	
	@After
	public void lizeMonitor() {
		this.monitor = null;
	}
	
	@Test
	public void testMonitor() {
		final int execs = 5;
		final int SECOND = 1000;
		boolean running = true;
		
		this.context.checking(new Expectations() {{
			atLeast(execs).of(owner).handleCheck(with(any(MonitorType.Type.class)));
			inSequence(monseq);
		}});
		
		this.monitor.start(SECOND);
		this.block(execs*SECOND);
		this.monitor.stop();
		running = this.monitor.running();
		
		context.assertIsSatisfied();
		assertFalse(running);
	}
	
	private void block(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			
		}
	}
	
	private class MockedMonitorable implements Monitorable {

		@Override
		public boolean monitorCheck() {
			return true;
		}
		
	}
}
