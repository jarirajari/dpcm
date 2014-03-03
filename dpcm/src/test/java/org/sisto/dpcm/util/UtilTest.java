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
package org.sisto.dpcm.util;

import java.io.File;
import static org.junit.Assert.*;
import org.junit.Test;

public class UtilTest {
	
	@Test
	public void testParseScriptCommandWindows() {
		File temp = null;
		String name = null;
		String cmd = null;
		String os = System.getProperty("os.name");
		boolean isWin = os.contains("windows");
		
		try {
			temp = new File("windowsScript.bat");
			name = temp.getAbsolutePath();
			cmd = Util.parseScriptCommand(temp);
		} catch (Exception e) {
			fail();
		} finally {
			temp.delete();
		}
		
		if (! isWin) {
			name = String.format("sh %s", name);
		}
		
		assertEquals("Wrong windows command",  name, cmd);
	}
}
