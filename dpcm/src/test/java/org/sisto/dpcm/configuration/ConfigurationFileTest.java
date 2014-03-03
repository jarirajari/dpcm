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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sisto.dpcm.configuration.ConfigurationFile.FileType;

public class ConfigurationFileTest {
	
	private ConfigurationFile cf = null;
	private static File tempFile = null;
	
	@Before
	public void addFile() throws IOException {
		tempFile = new File("configuration-file.test");
		tempFile.createNewFile();
		this.cf = new ConfigurationFile(tempFile.getName());
	}
	
	@After
	public void removeFile() {
		this.cf = null;
		tempFile.delete();
	}
	
	@Test
	public void testWriteFile() {
		boolean wrote = cf.writeFile(true, FileType.DEF);
		
		assertEquals(true, wrote);
	}
	
	@Test
	public void testReadFile() {
		boolean read = cf.readFile(true, FileType.DEF);
		
		assertEquals(true, read);
	}
}
