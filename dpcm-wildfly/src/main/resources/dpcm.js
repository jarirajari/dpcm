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
importPackage(org.jboss.as.cli.scriptsupport)      
importPackage(java.lang)
/*
 * Note! You can also run CLI-scripts as a batch by typing in console:
 *       'run-batch --file <filename.cli>'
 */
cli = null

function connect() {
	ct = false
	cli = CLI.newInstance()
	
	try {
		cli.connect()
		ct = true
	} catch (e) {
		println("scripting error, could not connect")
		print(e)
		ct = false
	}
	
	return ct
}

function disconnect() {
	dt = false
	
	try {
		cli.disconnect()
		dt = true
	} catch (e) {
		println("scripting error, could not disconnect")
		dt = false
	}
	
	return dt
}

function isDomainController() {
	ret = false
	connected = connect()
	
	if (connected) {
		result = cli.cmd(":read-attribute(name=process-type)")
		response = result.getResponse()
		dc = response.get("result").asString()
		if (dc == "Domain Controller") {
			ret = true
		} else {
			ret = false
		}
	} else {
		ret = false
	}
	disconnect()
	
	return ret
}

function isProcessRunning() {
	ret = false
	connected = connect()
	
	if (connected) {
		result = cli.cmd(":read-attribute(name=name)")
		ret = result.isSuccess()
	} else {
		ret = false
	}
	disconnect()
	
	return ret
}

function commitSuicideMan() {
	ret = false
	connected = connect()
	
	if (connected) {
		result = cli.cmd(":read-attribute(name=local-host-name)")
		response = result.getResponse()
		localhost = response.get("result").asString()
		result = cli.cmd("/host="+localhost+":shutdown(restart=false)")
		shuttingdown = result.isSuccess()
		if (shuttingdown) {
			ret = true
		} else {
			ret = false
		}
	} else {
		ret = false
	}
	disconnect()
	
	return ret
}

