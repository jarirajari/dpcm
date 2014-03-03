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
package org.sisto.dpcm.process.API;

public interface LifeCycleProcess {
	
	public enum Phase {PRE_START, POST_START, PRE_STOP, POST_STOP, NONE };
	
	public void setName(String name);
	public String getName();
	
	public boolean isProcessRunning();
	public boolean isDomainController();
	public boolean domainControllerBootHook(Phase p);
	public boolean commitSuicideMan(boolean tryFirstDyingGracefully);
}