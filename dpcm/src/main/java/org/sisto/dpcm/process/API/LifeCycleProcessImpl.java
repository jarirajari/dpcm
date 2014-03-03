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

import org.sisto.dpcm.process.API.LifeCycleProcess;

public class LifeCycleProcessImpl implements LifeCycleProcess {

	private String name = "default-LifeCycleProcessImpl";
	private boolean bootedAsDC = false;
	
	public LifeCycleProcessImpl() {}
	
	@Override
	public String getName() { 
		return (this.name); 
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public boolean isProcessRunning() {
		return true;
	}
	
	@Override
	public boolean isDomainController() {
		return (this.bootedAsDC);
	}
	
	@Override
	public boolean domainControllerBootHook(Phase p) {
		boolean success = true;
		
		this.bootedAsDC = true;
		
		return success;
	}

	@Override
	public boolean commitSuicideMan(boolean tryDyingFirst) {
		return false;
	}
}
