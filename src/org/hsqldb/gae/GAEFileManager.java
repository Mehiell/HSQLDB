/**
 * 
 */
package org.hsqldb.gae;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;

import com.newatlanta.commons.vfs.provider.gae.GaeVFS;

/**
 * @author petershen
 * File manager to access the GAEVFS file system
 */
public class GAEFileManager {

	private static Boolean  isInitialized  = false;
	private static FileSystemManager fsManager = null;
	
	private static void init() {
		if(!isInitialized) {
			synchronized(isInitialized) {
				try
				{
					GaeVFS.setRootPath("/");
					fsManager = GaeVFS.getManager();


				}
				catch(Exception e)
				{
					fsManager = null;
				}
				
				isInitialized = true; 
			}
		}
	}
		
	public static FileObject getFile(String filename) throws FileSystemException {
		init();
		FileObject file = fsManager.resolveFile("gae://" + filename);
		return file;
	}
	

}
