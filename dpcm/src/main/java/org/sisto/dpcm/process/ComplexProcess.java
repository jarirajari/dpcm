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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jlibs.core.io.FileUtil;
import jlibs.core.lang.JavaProcessBuilder;

import org.jgroups.logging.Log;
import org.jgroups.logging.LogFactory;
import org.sisto.dpcm.StandaloneProcess;
import org.sisto.dpcm.util.Util;

import static org.sisto.dpcm.DPCM.ConfigService;

public class ComplexProcess extends JVMProcess {
	
	private static Log log = LogFactory.getLog(ComplexProcess.class.getName());
	private static final String DEFAULT_PROCESS = "org.sisto.dpcm.StandaloneProcess.class";
	private List<String> argumentsJVM = new ArrayList<String>();
	private PrintStream ps = null;
	
	public ComplexProcess() {
		super(ProcessType.COMPLEX);
		try {
			ps = new PrintStream(new BufferedOutputStream(new FileOutputStream("process.log")), true);
		} catch (FileNotFoundException e) {
			log.error("Could not open log file for process");
		}
		log.info("Created new complex process");
	}
	
	private void unconfigure() {
		this.argumentsJVM.clear();
	}
	
	private void configure() {
		final String NO_CONFIG = "";
		String prefix = ConfigService.getConfiguration("dpcm.process.jvm.arg", "dpcm.process.jvm.arg");
		String[] keys = ConfigService.getConfigurations(prefix);
		int keyCount = keys.length;
		
		log.debug(String.format("Checking JVM process arguments with prefix %s", prefix));
		for (int i=0; i<keyCount; i++) {
			String param = ConfigService.getConfiguration(keys[i], NO_CONFIG);
			
			if (param != null) {
				log.debug(String.format("Adding a new attribute for the complex process (J=%s)", param));
				this.argumentsJVM.add(param);
			} else {
				log.debug(String.format("Could not find a parameter '%s'", param));
				continue;
			}
		}
	}
	
	private static boolean isJar() {
		final String main = ConfigService.getConfiguration("dpcm.process.main.location", DEFAULT_PROCESS);
		boolean isJar = main.contains(".jar");
		
		return isJar;
	}
	
	private static boolean isMain() {
		final String main = ConfigService.getConfiguration("dpcm.process.main.location", DEFAULT_PROCESS);
		boolean isMain = main.contains(".class");
		
		return isMain;
	}
	
	public boolean isDefault() {
		final String main = ConfigService.getConfiguration("dpcm.process.main.location", DEFAULT_PROCESS);
		boolean isDefault = DEFAULT_PROCESS.equals(main);
		
		return isDefault;
	}
	
	public boolean isScript() {
		final String script = ConfigService.getConfiguration("dpcm.process.script", null);
		boolean isScript = (script == null) ? false : true;
		
		return isScript;
	}
	
	private boolean resolveMain(JavaProcessBuilder jvm) {
		final String lib = "lib";
		final String path = ConfigService.getConfiguration("dpcm.process.lib", lib);
		final String main = ConfigService.getConfiguration("dpcm.process.main.location", DEFAULT_PROCESS);
		File pathDir = new File(path);
		File mainFile = new File(main);
		File relative = new File(path.concat(main));
		boolean isDefault = isDefault();
		boolean isJar = isJar();
		boolean isMain = isMain();
		boolean isPathDir = pathDir.isDirectory();
		boolean isRelative = relative.isFile();
		boolean resolved = false;
		String finalPath = (! (isMain || isJar) && isRelative) ? relative.getAbsolutePath() : mainFile.getAbsolutePath();
		
		try {
			final String mainName = String.format("%s", main);
			if (isDefault) {
				log.error(String.format("Using default option for '%s'", mainName));
				resolved = false;
			} else if (isJar) {
				jvm.jvmArg("-jar").jvmArg(mainName); // finalPath
				jvm.mainClass(finalPath); // mainName
				log.debug(String.format("Using jar option for '%s' (%s)", mainName, finalPath));
				resolved = true;
			} else if(isMain) {
				File proc = new File(path);
				URL[] url = new URL[] { proc.toURI().toURL() };
				URLClassLoader cl = URLClassLoader.newInstance(url); // URL class loader uses either jar or dir
				log.debug(String.format("Using class option for '%s', loading from %s '%s'", mainName, ((proc.isDirectory()) ? "dir" : "file"), proc.toURI().toURL().toString()));
				@SuppressWarnings("unused")
				Class<?> clazz = Class.forName(mainName, true, cl);
				if (isPathDir) {
					jvm.classpath(pathDir);
				}
				jvm.mainClass(mainName);
				resolved = true;
			} else {
				log.debug(String.format("Please, check option configuration for %s", finalPath));
				resolved = false;
			}
			
		} catch (ClassNotFoundException cnfe) {
			log.error(String.format("Could not find class '%s' from ", main, finalPath), cnfe);
			resolved = false;
		} catch (Exception e) {
			log.error("Could not resolve entry point", e);
			resolved = false;
		}
		
		return resolved;
	}
	
	@Override
	public void createProcess() {
		boolean continueNormally = (isScript() || ! isDefault());
		boolean isNotCreated = (! this.running.get());
		
		configure();
		if (continueNormally) {
			continueCreateProcess();
			log.debug("Creating normal complex process!");
		} else {
			if (isNotCreated) {
				continueCreateDefaultProcess();
			}
			log.debug("Creating default complex process!");
		}
	}
	
	private void continueCreateDefaultProcess() {
		StandaloneProcess.runProcess();
		this.running.set(true);
	}
	
	private JavaProcessBuilder getJavaProcessBuilder() {
		final String procHome = ConfigService.getConfiguration("dpcm.process.home", ".");
		final String javaHome = ConfigService.getConfiguration("dpcm.process.os.env.java.home", FileUtil.JAVA_HOME.getPath());
		final String workingDir = ConfigService.getConfiguration("dpcm.process.home", FileUtil.USER_DIR.getPath());
		final String debugPort = ConfigService.getConfiguration("dpcm.process.debug.port", "7171");
		final File jhf = new File(javaHome);
		final File wd = new File(workingDir);
		boolean configureProcessDebug = false;
		JavaProcessBuilder jvm = new JavaProcessBuilder();
		
		jvm.systemProperty("managedby", "DPCM");
		jvm.classpath(procHome);
		jvm.javaHome(jhf);
		jvm.workingDir(wd);
		for (String arg : this.argumentsJVM) {
			jvm.jvmArg(arg);
		}
		if (configureProcessDebug) { 
			jvm.debugPort(Integer.valueOf(debugPort)).debugSuspend(true);
		}
		
		return jvm;
	}
	
	private void continueCreateProcess() {
		final String script = ConfigService.getConfiguration("dpcm.process.script", null);
		boolean scripted = (isScript()) ? true : false;
		boolean resolved = false;
		File spt = null;
		String command = null;
		JavaProcessBuilder jvm = null;
		
		if (scripted) {
			log.info("Running process as a script!");
			spt = new File(script);
			scripted = (isScript() && spt.isFile()) ? true : false;
			command = Util.parseScriptCommand(spt);
			try {
				this.proc = Runtime.getRuntime().exec(command);
				this.runProcess("Launching complex process runner");
			} catch (IOException ioe) {
				log.fatal("Could not start complex process script: "+ioe.getMessage(), ioe);
				this.proc = null;
				ioe.printStackTrace();
			} catch (Exception e) {
				log.fatal("Could not start complex process script: "+e.getMessage(), e);
				this.proc = null;
				e.printStackTrace();
			}
		} else {
			log.info("Running process as a JVM!");
			jvm = getJavaProcessBuilder();
			resolved = resolveMain(jvm);
			printJavaProcessBuilderDebug(jvm);
			try {
				if (resolved) {
					this.proc = jvm.launch(ps, ps);
					this.runProcess("Launching complex process runner");
				} else {
					log.fatal("Could not start process, because main was not resolved!");
				}
			} catch (IOException ioe) {
				log.fatal("Could not create complex process: "+ioe.getMessage(), ioe);
				this.proc = null;
				ioe.printStackTrace();
			} catch (Exception e) {
				log.fatal("Could not create complex process: "+e.getMessage(), e);
				this.proc = null;
				e.printStackTrace();
			}
		}
		log.debug(String.format("Process exists = %s (%s)", this.proc.toString(), this.processExists()));
	}
	
	@Override
	public void destroyProcess() {
		boolean continueNormally = (isScript() || ! isDefault());
		
		if (continueNormally) {
			continueDestroyProcess();
			unconfigure();
		}
		synchronized(this.running) {
			this.running.set(false);
			this.running.notify();
		}
		log.info("Destroyed complex process");
	}
	
	public void continueDestroyProcess() {
		final int MAX_RETRIES = 10;
		
		for (int i=MAX_RETRIES; i>0; i--) {
			try {
				Thread.sleep(1000);
				if (this.proc != null) {
					this.proc.destroy();
					this.proc.waitFor();
					int exit = this.proc.exitValue();
				}
			} catch (IllegalThreadStateException itse) {
				log.debug("Could not destroy process, retrying "+i);
			} catch (InterruptedException ie) {
				log.debug("Could not destroy process, retrying"+i);
			}
		}
	}
	
	private static void printJavaProcessBuilderDebug(JavaProcessBuilder jvm) {
		
		log.debug("JVM INFORMATION");
		
		log.debug("JVM commands:");
		String[] cmds;
		try {
			cmds = jvm.command();
		} catch (IOException ioe) {
			cmds = new String[] { "" };
		}
		for (String cmd : cmds) {
			log.debug(cmd);
		}
		
		log.debug("JVM information:");
		log.debug("Java home: "+jvm.javaHome().getPath());
		log.debug("Working dir: "+jvm.workingDir().getPath());
		log.debug("JVM Main class: "+jvm.mainClass());
		log.debug("JVM type: "+jvm.vmType());
		log.debug("Initial heap: "+jvm.initialHeap());		
		log.debug("Maximum heap: "+jvm.maxHeap());
		
		log.debug("System properties:");
		Map<String, String> sysProps = jvm.systemProperties();
		Set<String> keys = sysProps.keySet();
		Iterator<String> ki = keys.iterator();
		while (ki.hasNext()) {
			String key = ki.next();
			String val = sysProps.get(key);
			log.debug(String.format("%s: %s", key, val));
		}
		
		log.debug("JVM arguments:");
		List<String> args = jvm.jvmArgs();
		for (String arg : args) {
			log.debug(arg);
		}
		
		log.debug("JVM boot class path:");
		List<File> bootCP = jvm.bootClasspath();
		for (File arg : bootCP) {
			log.debug(arg.getPath());
		}
		
		log.debug("JVM class path:");
		List<File> jvmCP = jvm.classpath();
		for (File arg : jvmCP) {
			log.debug(arg.getPath());
		}
		
		log.debug("JVM endorsed dirs:");
		List<File> endorsedDirs = jvm.endorsedDirs();
		for (File arg : endorsedDirs) {
			log.debug(arg.getPath());
		}
		
		log.debug("JVM ext dirs:");
		List<File> extDirs = jvm.extDirs();
		for (File arg : extDirs) {
			log.debug(arg.getPath());
		}
		
		log.debug("JVM lib dirs:");
		List<File> libPath = jvm.libraryPath();
		for (File arg : libPath) {
			log.debug(arg.getPath());
		}
	}
}
