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

import static org.sisto.dpcm.DPCM.ConfigService;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jgroups.logging.Log;
import org.jgroups.logging.LogFactory;

/**
 * SimpleProcess used for prototyping
 * 
 * @deprecated because DPCM uses ComplexProcess.class
 * 
 **/
public class SimpleProcess extends JVMProcess {
	
	private static Log log = LogFactory.getLog(SimpleProcess.class.getName());
	private static final String DEFAULT_PROCESS = "org.sisto.dpcm.StandaloneProcess.class";
	
	public SimpleProcess() {
		super(ProcessType.SIMPLE);
		this.running = new AtomicBoolean(false);
		log.info("New simple process");
	}
	
	private static String whichProc() {
		final String proc = DEFAULT_PROCESS;
		StringBuilder sb = new StringBuilder();
		
		sb.append(proc);
		
		return (sb.toString());
	}
	
	private static String whichJava() {
		final String path = ConfigService.getSystem("java.home", "java");
		final String separator = ConfigService.getSystem("file.separator", File.separator);
		final String bin = "bin";
		final String java = "java";
		StringBuilder sb = new StringBuilder();
		
		sb.append(path);
		sb.append(separator);
		sb.append(bin);
		sb.append(separator);
		sb.append(java);
		
		return (sb.toString());
	}
	
	public void createProcess() {
		final String CLASS_PATH = "-cp";
		final String LIB = ("lib/StandaloneProcess").replaceAll("/", File.separator);
		final String CFG = ("configuration").replaceAll("/", File.separator);
		final String processPath = ConfigService.getConfiguration("dpcm.process.lib", LIB);
		File f = new File(whichProc());
		ProcessBuilder pb = new ProcessBuilder(whichJava(), CLASS_PATH, processPath, CLASS_PATH, CFG, whichProc());
		Map<String, String> env = pb.environment();
		
		env.put("managedby", "DPCM");
		pb.redirectErrorStream(true);
		log.info(String.format("Creating the simple process from %s'%s'", ((f.isFile()) ? "existing file " : ""), f.getPath()));
		try {
			this.proc = pb.start();
			this.runProcess("Simple process runner");
		} catch (IOException e) {
			log.debug("Could not create simple process: "+e.getMessage(), e);
			this.proc = null;
		} catch (Exception e) {
			log.debug("Could not create simple process: "+e.getMessage(), e);
			this.proc = null;
		}
		
	}
	
	public void destroyProcess() {
		log.info("Destroying the simple process");
		if (this.proc != null) {
			this.proc.destroy();
		}
		this.running.set(false);
	}
}
