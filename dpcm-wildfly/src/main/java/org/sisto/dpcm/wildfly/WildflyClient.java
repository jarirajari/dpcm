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
package org.sisto.dpcm.wildfly;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.sisto.dpcm.process.API.LifeCycleProcess;

public class WildflyClient implements LifeCycleProcess {
	
	private static Logger log = LogManager.getLogger(WildflyClient.class.getName());
	private final String SCRIPT_OBJECT = "dpcm";
	private String name = "wildfly-client";
	private final String RHINO_ENGINE = "rhino";
	private final String SCRIPT_FILE_JS = "configuration/dpcm.js";
	private final String DEFAULT_JS = "dpcm.js";
	private ScriptEngineManager sem = new ScriptEngineManager();
	private ScriptEngine se;
	private ExecutorService executor = Executors.newFixedThreadPool(1);
	
	public WildflyClient() {
		loadJavascript();
	}
	
	private void loadJavascript() {
		InputStream is = null;
		InputStreamReader ir = null;
		
		try {
			File f = new File(SCRIPT_FILE_JS);
			boolean exists = (f.isFile() && f.exists());
			String fn = f.getAbsolutePath();
			
			log.info("Setting up client scripting");
			this.se = sem.getEngineByName(RHINO_ENGINE);
			if (exists) {
				is = (InputStream) new FileInputStream(fn);
				ir = new InputStreamReader(is);
				log.info("Loaded javascript from file: ".concat(f.getName()));
			} else {
				is = Thread.currentThread().getContextClassLoader().getResourceAsStream(DEFAULT_JS);
				ir = new InputStreamReader(is);
				log.info("Loaded javascript from: classpath");
			}
			this.se.eval(ir);
		} catch (Exception e) {
			log.error("Could not load user defined runtime properties!", e);
		} finally {
			close(ir);
			close(is);
		}
	}
	
	private void close(Closeable c) {
		if (c != null) {
			try {
				c.close();
			} catch (Exception e) {}
		}
	}
	
	@SuppressWarnings("unchecked")
	protected <T> T callFunction(String name, Class<T> returnType, Object... args) {
		Invocable inv = ((Invocable) this.se);
		T result = null;
		
		try {
			Object res = inv.invokeFunction(name, args);
			result = (res == null) ? null : (T) res;
		} catch (ScriptException e) {
			log.error("Could not invoke function: "+e.getMessage());
		} catch (NoSuchMethodException e) {
			log.error("Could not invoke unknown function: "+e.getMessage());
			throw new RuntimeException(e);
		} catch (Exception e) {
			log.error("Failed to invoke function: "+e.getMessage());
		}
		
		return result;

	}
	
	@SuppressWarnings({"unused","unchecked"})
	protected <T> T callMethod(String name, Class<T> returnType, Object... args) {
		Invocable inv = ((Invocable) this.se);
		T result = null;
		
		try {
			Object obj = this.se.get(SCRIPT_OBJECT);
			Object res = inv.invokeMethod(obj, name, args);
			result = (res == null) ? null : (T) res;
		} catch (ScriptException e) {
			log.error("Could not invoke method: "+e.getMessage());
		} catch (NoSuchMethodException e) {
			log.error("Could not invoke unknown method: "+e.getMessage());
			throw new RuntimeException(e);
		} catch (Exception e) {
			log.error("Failed to invoke method: "+e.getMessage());
		}
		
		return result;
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public boolean domainControllerBootHook(Phase p) {
		boolean hooked = false;
		log.debug("Not calling javascript function: empty domain boot hook");
		
		return hooked;
	}
	
	@Override
	public boolean isDomainController() {
		boolean is = false;
		Boolean bis = callFunction("isDomainController", boolean.class, noArgs());
		
		is = (bis == null) ? false : bis.booleanValue();
		log.debug(String.format("Calling javascript function isDomainController =>> %s", is));
		
		return is;
	}

	@Override
	public boolean isProcessRunning() {
		boolean is = false;
		Boolean bis = callFunction("isProcessRunning", boolean.class, noArgs());
		
		is = (bis == null) ? false : bis.booleanValue();
		log.debug(String.format("Calling javascript function isProcessRunning =>> %s", is));
		
		return is;
	}

	@Override
	public boolean commitSuicideMan(boolean tryFirstDyingGracefully) {
		boolean success = false;
		
		if (tryFirstDyingGracefully) {
			boolean wished = wishToDie();
			
			if (! wished) {
				waitUntilDead();
				success = true;
			}
		}
		
		return success;
	}
	
	private boolean wishToDie() {
		boolean success = false;
		Boolean bsuccess = callFunction("commitSuicideMan", Boolean.class, noArgs());
		
		success = (bsuccess == null) ? false : bsuccess.booleanValue();
		log.debug(String.format("Calling javascript function commitSuicideMan =>> %s", success));
		
		return success;
	}
	
	private void waitUntilDead() {
		Alive alive = null;
		FutureTask<Boolean> t = null;
		boolean running = this.isProcessRunning();
		
		if (running) {
			try {
				alive = new Alive();
				t = new FutureTask<Boolean>(alive);
				this.executor.submit(t).get();
				log.debug("Client is ruled dead now");
			} catch (Exception e) {
				e.printStackTrace();
				log.error(String.format("Error waiting process to die: %s", e.getMessage()));
			}
		}
	}
	
	private static final Object[] noArgs() {
		return (new Object[0]);
	}
	
	private class Alive implements Callable<Boolean> {
		private final int SECONDS_15 = 15*1000;
		private final int PER_MINUTE = 4;
		private boolean dead = false;
		private int counter = 0;
		private int timeout = 3*PER_MINUTE;
		
		@Override
		public Boolean call() {
			while ((this.dead == false) && (this.counter < timeout)) {
				try {
					boolean running = WildflyClient.this.isProcessRunning();
					this.dead = (WildflyClient.this == null) ? false : !running;
					Thread.sleep(SECONDS_15);
					this.counter++;
				} catch (InterruptedException e) {}
			}
			
			return (new Boolean(true));
		}
	}
}
