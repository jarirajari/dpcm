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
package org.sisto.dpcm.group;

import org.junit.Test;
import org.sisto.dpcm.configuration.MonitoredConfiguration;
import static org.junit.Assert.*;

public class GroupStateTest {
	
	@Test
	public void testCloning() {
		GroupState dst = null;
		GroupState src = new GroupState(new MonitoredConfiguration());
		
		dst = GroupState.cloneGroupState(src);
		
		assertNotEquals(src, dst);
	}
}
