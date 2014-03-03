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
package org.sisto.dpcm.configuration;

import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.jgroups.logging.Log;
import org.jgroups.logging.LogFactory;
import org.sisto.dpcm.DPCM;
import org.sisto.dpcm.configuration.ConfigurationFile.FileType;

public class FileConfiguration implements Externalizable {
	
	private static Log log = LogFactory.getLog(FileConfiguration.class.getName());
	private Map<String, ConfigurationFile> configurationFiles = Collections.synchronizedMap(new TreeMap<String, ConfigurationFile>());
	
	public FileConfiguration() {
		this.configure();
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		this.loadFiles();
		out.writeObject(this.configurationFiles);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		this.configurationFiles = (Map<String, ConfigurationFile>) in.readObject();	
		this.saveFiles();
	}
	
	private void configure() {
		String[] files = DPCM.ConfigService.getConfigurations("dpcm.process.file");
		int fileCount = files.length;
		String[] fileNames = new String[fileCount];
		File dir = new File("");
		
		log.debug(String.format("Searching configuration files from %s", dir.getAbsolutePath()));
		for (int i=0; i<fileCount; i++) {
			String fileName = DPCM.ConfigService.getConfiguration(files[i], null);
			if (fileName != null) {
				fileNames[i] = fileName;
			} else {
				fileNames[i] = null;
			}
		}
		this.addFiles(fileNames);
	}
	
	public boolean haveFilesChanged() {
		boolean changed = false;
		
		synchronized (this.configurationFiles) {
			Set<String> files = configurationFiles.keySet();
			Iterator<String> conf = files.iterator();
			while (conf.hasNext()) {
				String key = conf.next();
				ConfigurationFile cf = this.configurationFiles.get(key);
				changed = (cf.readFile(false, FileType.DEF) || changed) ? true : false;
				log.debug(String.format("File '%s' has %s...", key, ((changed) ? "changed" : "not changed")));
			}
		}
		log.info(String.format("File configuration changed -> %s", changed));
		
		return changed;
	}
	
	public int saveFiles(FileType type) {
		return (saveFilesWith(type));
	}
	
	public int saveFiles() {
		return (saveFilesWith(FileType.BKP));
	}
	
	private int saveFilesWith(FileType type) {
		boolean changed = false;
		int saving = 0;
		
		synchronized (this.configurationFiles) {
			Set<String> files = configurationFiles.keySet();
			Iterator<String> conf = files.iterator();
			while (conf.hasNext()) {
				String key = conf.next();
				log.debug(String.format("Saving file '%s'", key));
				ConfigurationFile cf = this.configurationFiles.get(key);
				changed = (cf.writeFile(true, type)) ? true : false;
				saving++;
			}
		}
		log.info(String.format("Saved files changed -> %s", changed));
		
		return saving;
	}
	
	public int loadFiles() {
		boolean changed = false;
		int loading = 0;
		
		synchronized (this.configurationFiles) {
			Set<String> files = configurationFiles.keySet();
			Iterator<String> conf = files.iterator();
			while (conf.hasNext()) {
				String key = conf.next();
				log.debug(String.format("Loading file '%s'", key));
				ConfigurationFile cf = this.configurationFiles.get(key);
				changed = (cf.readFile(true, FileType.DEF)) ? true : false;
				loading++;
			}
		}
		log.info(String.format("Loaded files changed -> %s'", changed));
		
		return loading;
	}
	
	public void addFiles(String[] filenames) {
		for (String filename : filenames) {
			ConfigurationFile cf = new ConfigurationFile(filename);
			
			log.debug(String.format("Adding file '%s' to configuration", filename));
			if (! (configurationFiles.containsKey(filename))) {
				configurationFiles.put(filename, cf);
			}
		}
	}
	
	public void removesFiles(String[] filenames) {
		for (String filename : filenames) {
			log.debug(String.format("Removing file '%s' from configuration", filename));
			if (configurationFiles.containsKey(filename)) {
				configurationFiles.remove(filename);
			}
		}
	}
	
	public void toProcessConfiguration() {
		synchronized (this.configurationFiles) {
			Set<String> files = configurationFiles.keySet();
			Iterator<String> conf = files.iterator();
			while (conf.hasNext()) {
				String key = conf.next();
				ConfigurationFile cf = this.configurationFiles.get(key);
				boolean copied = ConfigurationFile.copyFile(cf.getFile(), FileType.BKP, FileType.DEF);
				log.debug(String.format("Converting file '%s' to process configuration file -> ", (copied ? "success" : "failed")));
			}
		}
	}
}
