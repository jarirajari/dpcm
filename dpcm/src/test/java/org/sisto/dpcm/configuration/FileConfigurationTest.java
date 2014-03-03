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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sisto.dpcm.configuration.ConfigurationFile.FileType;
import org.sisto.dpcm.util.Config;

public class FileConfigurationTest {
	
	private FileConfiguration fc = null;
	private static Config c = Config.getConfig();
	
	@BeforeClass
	public static void initProps(){
		c.loadProperties();
	}
	
	@Before
	public void addFile() {
		String name = c.getConfiguration("configFile", null);
		this.fc = new FileConfiguration();
		this.fc.addFiles(new String[] { name });
	}
	
	@Test
	public void testLoadFiles() {	
		int loadFileCount = fc.loadFiles();
		
		assertEquals(1, loadFileCount);
	}
	
	@Test
	public void testFileChanged() {
		boolean changedFiles = fc.haveFilesChanged();
		
		fc.loadFiles();
		fc.saveFiles(FileType.DEF);
		
		assertEquals(true, changedFiles);
	}
	
	@Test
	public void testFileNotChanged() {
		boolean changedFiles = false;
		
		fc.loadFiles();
		fc.saveFiles(FileType.DEF);
		changedFiles = fc.haveFilesChanged();
		
		assertEquals(false, changedFiles);
	}
	
	@Test
	public void testSaveFiles() {
		int saveFileCount = fc.saveFiles();
		
		assertEquals(1, saveFileCount);
	}
}
