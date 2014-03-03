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

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Config {
	
	private static final String EMPTY = "";
	private static final String ENV = "$";
	private static final String ENV_REGEX = "\\$[A-Z_]+[A-Z_0-9]+";
	public static final Pattern isEnvironmentVariable = Pattern.compile(ENV_REGEX);
	private static OrderedProperties props = new OrderedProperties();
	private static Config conf = null;
	
	private static final String CONFIG_PROPERTIES = "configuration/configuration.properties".replaceAll("/", File.separator);
	private static final String DEFAULT_PROPERTIES = "configuration.properties";
	private static final String JGROUPS3_CONFIG = "configuration/jgroups3-dpcm.xml".replaceAll("/", File.separator);
	private static final String JGROUPS3_DEFAULT = "jgroups3-dpcm.xml";
	
	private Config() {
		loadCompiletimeProps();
	}
	
	public void loadProperties() {
		resolveRuntimeProps();
		evaluateEnvironment();
	}
	
	private void evaluateEnvironment() {
		Enumeration<Object> all = props.keys();
		ArrayList<EvaluatedProperty> eprops = new ArrayList<EvaluatedProperty>(); 
		
		while (all.hasMoreElements()) {
			String key = (String) all.nextElement();
			String val = (String) props.get(key);
			eprops.add(new EvaluatedProperty(key, this.evaluateProperty(val, null)));
		}
		appendEvaluated(eprops);
	}
	
	private void appendEvaluated(ArrayList<EvaluatedProperty> eprops) {
		Iterator<EvaluatedProperty> i = eprops.iterator();
		
		while (i.hasNext()) {
			EvaluatedProperty ep = i.next();
			String key = ep.getKey();
			String val = ep.getVal();
			props.put(key, val);
		}
	}
	
	private class EvaluatedProperty {
		String key = null;
		String val = null;
		
		public EvaluatedProperty(String key, String val) {
			this.key = key;
			this.val = val;
		}
		
		public String getKey() {
			return (this.key);
		}
		
		public String getVal() {
			return (this.val);
		}
	}
	
	public String resolveJgroups3Config() {
		String conffile = JGROUPS3_CONFIG;
		String confpath = JGROUPS3_DEFAULT;
		File cf = new File(conffile);
		boolean exists = (cf.isFile() && cf.exists());
		String path = null;
		
		if (exists) {
			path = cf.getAbsolutePath();
		} else {
			loadFromFileOrClasspath(conffile, confpath);
		}
		
		return path;
	}
	
	private void resolveRuntimeProps() {
		String file = CONFIG_PROPERTIES;
		String path = DEFAULT_PROPERTIES;
		
		loadFromFileOrClasspath(file, path);
	}
	
	private void loadFromFileOrClasspath(String file, String path) {
		FileInputStream fis = null;
		InputStream is = null;
		
		try {
			File f = new File(file);
			boolean exists = (f.isFile() && f.exists());
			String fn = f.getAbsolutePath();
			
			if (exists) {
				fis = new FileInputStream(fn);
				props.load(fis);
			} else {
				is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
				props.load(is);
			}
		} catch (Exception e) {
			
		} finally {
			close(is);
			close(fis);
		}
	}
	
	private void close(Closeable c) {
		if (c != null) {
			try {
				c.close();
			} catch (Exception e) {}
		}
	}
	
	private void loadCompiletimeProps() {
		// How often user defined configuration file is checked: e.g. 10(s) value(unit in [h]ours, [m]inutes, or [s]econds)
		props.put("dpcm.configuration.change.check.interval", "10(s)");
	}
	
	public static Config getConfig() {
		if (conf == null) {
			conf = new Config();
		}
		
		return conf;
	}
	
	public String getConfiguration(String key, String def) {
		return (getProperty(key, def));
	}
	
	public String[] getConfigurations(String groupName) {
		Enumeration<Object> all = props.keys();
		ArrayList<String> keys = new ArrayList<String>(0);
		String[] asArray = new String[0];
		
		while (all.hasMoreElements()) {
			String prop = (String) all.nextElement();
			boolean belongsToGroup = prop.startsWith(groupName);
			if (belongsToGroup) {
				keys.add(prop);
			} else {
				continue;
			}
		}
		asArray = keys.toArray(new String[keys.size()]);
		
		return asArray;
	}
	
	public String getProperty(String key, String def) {
		String tmp = (key == null) ? EMPTY : ((String) props.get(key));
		String p;
		
		if (tmp == EMPTY || tmp == null) {
			p = def;
		} else {
			p = evaluateProperty(tmp, def);
		}
		
		return p;
	}
	
	private String evaluateProperty(String var, String def) {
		Matcher m = isEnvironmentVariable.matcher(var);
		boolean evaluate = m.find();
		String evaluated = var;
		
		while (evaluate) {
			String env = m.group();
			String nv = (env.length() >= 1) ? env.substring(1) : env;
			String n = this.getEnvironment(nv, def);
			
			evaluated = evaluate(var, env, n);
			evaluate = m.find();
		}
		
		return evaluated;
	}
	
	protected String evaluate(String text, String oldvar, String newvar) {
		if (oldvar == null || newvar == null) {
			return EMPTY;
		}
		return (text.replace(oldvar, newvar));
	}
	
	public String getEnvironment(String var, String def) {
		String ENV = (var == null) ? null : var.trim().toUpperCase();
		String tmp = (ENV == null) ? null : System.getenv(ENV);
		String v;
		
		if (tmp == null) {
			v = def;
		} else {
			v = tmp;
		}
		
		return v;
	}
	
	public String getSystem(String sys, String def) {
		String tmp = (sys == null) ? null : System.getProperty(sys);
		String s;
		
		if (tmp == null) {
			s = def;
		} else {
			s = tmp;
		}
		
		return s;
	}
}
