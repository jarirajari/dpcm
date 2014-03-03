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

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.jgroups.logging.Log;
import org.jgroups.logging.LogFactory;


public class ConfigurationFile implements Serializable {
	
	public enum FileType {
		OLD(".old"), NEW(".new"), TXT(".txt"), BKP(".bkp"), DEF("");
		
		private String ext;
		
		private FileType (String s) {
			this.ext = s;
		}
		
		public String getFileType() {
			return this.ext;
		}
	}
	
	private static final long serialVersionUID = -1830156906073619958L;
	private static Log log = LogFactory.getLog(ConfigurationFile.class.getName());
	private int size = 0;
	private byte[] content = new byte[0];
	private File file = null;
	private byte[] oldDigest = new byte[0];
	
	public ConfigurationFile(String name) {
		this.file = new File(name);
	}
	
	public File getFile() {
		return this.file;
	}
	
	public boolean readFile(boolean update, FileType type) {
		String filetype = type.getFileType();
		String filein = this.file.getAbsolutePath().concat(filetype);
		FileInputStream fis = null;
		FileChannel fc = null;
		MappedByteBuffer buffer = null;
		MessageDigest md5 = null;
		byte[] newDigest = new byte[0];
		boolean changed = false;
		
		try {
			log.debug(String.format("Reading file '%s'", filein));
			md5 = MessageDigest.getInstance("MD5");
			fis = new FileInputStream(filein);
			fc = fis.getChannel();
			buffer = fc.map(MapMode.READ_ONLY, 0, fc.size());
			buffer = buffer.load();
			if (update) {
				this.size = (int) fc.size();
				this.content = new byte[this.size];
				buffer.get(this.content);
				md5.update(this.content);
			} else {
				int size = (int) fc.size();
				byte[] tmp = new byte[size];
				buffer.get(tmp);
				md5.update(tmp);
			}
			buffer.clear();
		} catch (FileNotFoundException e) {
			log.warn(String.format("Could not find file '%s'", filein), e);
		} catch (IOException e) {
			log.warn(String.format("Could not read file '%s'", filein), e);
		} catch (NoSuchAlgorithmException e) {
			log.warn(String.format("Could not find hash algorithm"), e);
		} catch (Exception e) {
			log.warn(String.format("Could not read file because %s", e.getCause().getMessage()), e);
		} finally {
			newDigest = md5.digest();
			close(fc);
			close(fis);
		}
		changed = checkDigest(update, newDigest);
		
		return changed;
	}
	
	public boolean writeFile(boolean update, FileType type) {
		String filetype = type.getFileType();
		String fileout = this.file.getAbsolutePath().concat(filetype);
		FileOutputStream fos = null;
		FileChannel fc = null;
		MessageDigest md5 = null;
		byte[] newDigest = new byte[0];
		boolean changed = false;
		
		try {
			log.debug(String.format("Writing file '%s'", fileout));
			md5 = MessageDigest.getInstance("MD5");
			md5.update(this.content);
			fos = new FileOutputStream(fileout);
			fos.write(this.content);
		} catch (FileNotFoundException e) {
			log.warn(String.format("Could not find file '%s'", fileout), e);
		} catch (IOException e) {
			log.warn(String.format("Could not write file '%s'", fileout), e);
		} catch (NoSuchAlgorithmException e) {
			log.warn(String.format("Could not find hash algorithm"), e);
		} catch (Exception e) {
			log.warn(String.format("Could not write file because %s", e.getCause().getMessage()), e);
		} finally {
			newDigest = md5.digest();
			close(fc);
			close(fos);
		}
		changed = checkDigest(update, newDigest);
		
		return changed;
	}
	
	private boolean checkDigest(boolean updateDigest, byte[] newDigest) {
		boolean changed = (Arrays.equals(newDigest, this.oldDigest)) ? false : true;
		
		if (changed) {
			if (updateDigest) {
				int newLength = newDigest.length;
				this.oldDigest = new byte[newLength];
				this.oldDigest = Arrays.copyOf(newDigest, newLength);
			}
			changed = true;
		}
		
		return changed;
	}
	
	public static boolean hasFileExtension(File f, FileType t) {
		String fileName = f.getPath();
		String fileExt = t.getFileType();
		boolean hasExtension = fileName.endsWith(fileExt);
		
		return hasExtension;
	}
	
	public static String addFileExtension(File f, FileType t) {
		String fileName = f.getPath();
		String extended = fileName.concat(t.getFileType());
		
		return extended;
	}
	
	public static String removeFileExtension(File f, FileType t) {
		String fileName = f.getPath();
		String removed = fileName;
		String fileExt = t.getFileType();
		boolean hasExtension = hasFileExtension(f, t);
		int length = (fileName.length() - fileExt.length());
		
		if (hasExtension) {
			try {
				removed = fileName.substring(0, length);
			} catch (Exception e) {
				removed = fileName;
			}
		}
		
		return removed;
	}
	
	public static boolean copyFile(File f, FileType src, FileType dst) {
		FileChannel srcch = null;
		FileChannel dstch = null;
		String oldFileName = removeFileExtension(f, src);
		String newFileName = addFileExtension(f, dst);
		File srcf = new File(oldFileName);
		File dstf = new File(newFileName);
		boolean copied = false;
		
		try {
			srcch = new FileInputStream(srcf).getChannel();
			dstch = new FileOutputStream(dstf).getChannel();
			dstch.transferFrom(srcch, 0, srcch.size());
			copied = true;
		} catch (IOException ioe) {
			copied = false;
		} finally {
			close(dstch);
			close(srcch);
		}
		
		return copied;
	}
	
	public void resetFile() {
		this.size = 0;
		this.content = new byte[0];
	}
	
	private static void close(Closeable c) {
		if (c == null) 
			return;
		try {
			c.close();
		} catch (IOException ioe) { }
	}
}
