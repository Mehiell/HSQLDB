/* Copyright (c) 2001-2011, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the HSQL Development Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL HSQL DEVELOPMENT GROUP, HSQLDB.ORG,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package org.hsqldb.lib;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import org.apache.commons.vfs.FileObject;
import org.hsqldb.gae.GAEFileManager;
import org.hsqldb.lib.java.JavaSystem;

/**
 * A collection of file management methods.<p>
 * Also provides the default FileAccess implementation
 *
 * @author Campbell Boucher-Burnet (boucherb@users dot sourceforge.net)
 * @author Fred Toussi (fredt@users dot sourceforge.net)
 * @author Ocke Janssen oj@openoffice.org
 * @version 1.9.0
 * @since 1.7.2
 */
public class FileUtil implements FileAccess {

    private static FileUtil      fileUtil      = new FileUtil();
    private static FileAccessRes fileAccessRes = new FileAccessRes();

    /** Creates a new instance of FileUtil */
    FileUtil() {}

    public static FileUtil getFileUtil() {
        return fileUtil;
    }

    public static FileAccess getFileAccess(boolean isResource) {
        return isResource ? (FileAccess) fileAccessRes
                          : (FileAccess) fileUtil;
    }

    public boolean isStreamElement(java.lang.String elementName) {
    	try {
    		return (GAEFileManager.getFile(elementName)).exists();
    	}	catch(Exception ee)
    	{
    		return false;
    	}
    }

    public InputStream openInputStreamElement(java.lang.String streamName)
    throws java.io.IOException {

        try {
            return GAEFileManager.getFile(streamName).getContent().getInputStream();
        } catch (Throwable e) {
            throw JavaSystem.toIOException(e);
        }
    }

    public void createParentDirs(String filename) {
    	try {
    		makeParentDirectories(GAEFileManager.getFile(filename));
    	}	catch(Exception ee)
    	{
    		return;
    	}
        
    }

    public void removeElement(String filename) {

        if (isStreamElement(filename)) {
            delete(filename);
        }
    }

    public void renameElement(java.lang.String oldName,
                              java.lang.String newName) {
        renameWithOverwrite(oldName, newName);
    }

    public java.io.OutputStream openOutputStreamElement(
            java.lang.String streamName) throws java.io.IOException {
        return GAEFileManager.getFile(streamName).getContent().getOutputStream();
    }

    // end of FileAccess implementation
    // a new File("...")'s path is not canonicalized, only resolved
    // and normalized (e.g. redundant separator chars removed),
    // so as of JDK 1.4.2, this is a valid test for case insensitivity,
    // at least when it is assumed that we are dealing with a configuration
    // that only needs to consider the host platform's native file system,
    // even if, unlike for File.getCanonicalPath(), (new File("a")).exists() or
    // (new File("A")).exits(), regardless of the hosting system's
    // file path case sensitivity policy.
    public final boolean fsIsIgnoreCase = false;
        //(GAEFileManager.getFile("A")).equals(GAEFileManager.getFile("a"));

    // posix separator normalized to File.separator?
    // CHECKME: is this true for every file system under Java?
    public final boolean fsNormalizesPosixSeparator = true;
        //(GAEFileManager.getFile("/").getName().getPath().endsWith("/"));

    // for JDK 1.1 createTempFile
    final Random random = new Random(System.currentTimeMillis());

    /**
     * Delete the named file
     */
    public boolean delete(String filename) {
    	try {
    		return (GAEFileManager.getFile(filename)).delete();
    	} catch(Exception e) {
    		return false;
    	}
    	 
    }

    /**
     * Requests, in a JDK 1.1 compliant way, that the file or directory denoted
     * by the given abstract pathname be deleted when the virtual machine
     * terminates. <p>
     *
     * Deletion will be attempted only for JDK 1.2 and greater runtime
     * environments and only upon normal termination of the virtual
     * machine, as defined by the Java Language Specification. <p>
     *
     * Once deletion has been sucessfully requested, it is not possible to
     * cancel the request. This method should therefore be used with care. <p>
     *
     * @param f the abstract pathname of the file be deleted when the virtual
     *       machine terminates
     */
    public void deleteOnExit(FileObject f) {
    	try {
    		f.delete();
    	} catch(Exception e) {
    		//return false;
    	}
    }

    /**
     * Return true or false based on whether the named file exists.
     */
    public boolean exists(String filename) {
    	try {
    		return (GAEFileManager.getFile(filename)).exists();
    	} catch(Exception e) {
    		return false;
    	}    	
    }

    public boolean exists(String fileName, boolean resource, Class cla) {

        if (fileName == null || fileName.length() == 0) {
            return false;
        }

        return resource ? null != cla.getResource(fileName)
                        : FileUtil.getFileUtil().exists(fileName);
    }

    /**
     * Rename the file with oldname to newname. If a file with newname already
     * exists, it is deleted before the renaming operation proceeds.
     *
     * If a file with oldname does not exist, no file will exist after the
     * operation.
     */
    private boolean renameWithOverwrite(String oldname, String newname) {
    	
    	boolean renamed = false;
    	try {
    		FileObject    file    = GAEFileManager.getFile(oldname);
    		FileObject    newfile    = GAEFileManager.getFile(newname);
    		renamed =  file.canRenameTo(newfile);
    		
    		 if (renamed) {
    	            return true;
    	        }
    		 if (delete(newname)) {
    	            return file.canRenameTo(newfile);
    	        }
    	} catch(Exception e) {
    		return false;
    	}    	
    	return false;
    }

    /**
     * Retrieves the absolute path, given some path specification.
     *
     * @param path the path for which to retrieve the absolute path
     * @return the absolute path
     */
    public String absolutePath(String path) {
    	try {
    		return (GAEFileManager.getFile(path)).getURL().toString();
    	} catch(Exception e) {
    		return path;
    	}    	
    }

    /**
     * Retrieves the canonical file for the given file, in a
     * JDK 1.1 complaint way.
     *
     * @param f the File for which to retrieve the absolute File
     * @return the canonical File
     */
    public FileObject canonicalFile(FileObject f) throws IOException {
        return f;
    }

    /**
     * Retrieves the canonical file for the given path, in a
     * JDK 1.1 complaint way.
     *
     * @param path the path for which to retrieve the canonical File
     * @return the canonical File
     */
    public FileObject canonicalFile(String path) throws IOException {
    	return GAEFileManager.getFile(path);
    }

    /**
     * Retrieves the canonical path for the given File, in a
     * JDK 1.1 complaint way.
     *
     * @param f the File for which to retrieve the canonical path
     * @return the canonical path
     */
    public String canonicalPath(FileObject f) throws IOException {
        return f.getName().getPath();
    }

    /**
     * Retrieves the canonical path for the given path, in a
     * JDK 1.1 complaint way.
     *
     * @param path the path for which to retrieve the canonical path
     * @return the canonical path
     */
    public String canonicalPath(String path) throws IOException {
        return canonicalPath(GAEFileManager.getFile(path));
    }

    /**
     * Retrieves the canonical path for the given path, or the absolute
     * path if attemting to retrieve the canonical path fails.
     *
     * @param path the path for which to retrieve the canonical or
     *      absolute path
     * @return the canonical or absolute path
     */
    public String canonicalOrAbsolutePath(String path) {

        try {
            return canonicalPath(path);
        } catch (Exception e) {
            return absolutePath(path);
        }
    }

    public void makeParentDirectories(FileObject f) {
    	 try {
    		 FileObject parent = f.getParent();

    	        if (parent != null && !parent.exists()) {
    	        	parent.createFolder();
    	        }
    	 } catch (Exception e) {
            return;
         }
    	
        
    }

    public static String makeDirectories(String path) {
    	try {
    		
    		FileObject f =	GAEFileManager.getFile(path);
   		 	
    		if (f != null && !f.exists()) {
   	        	f.createFolder();
   	        }
    		
    		return f.getName().getPath();
   	 } catch (Exception e) {
           return null;
        }
    	
        
    }

    public FileAccess.FileSync getFileSync(java.io.OutputStream os)
    throws java.io.IOException {
        return new FileSync((FileOutputStream) os);
    }

    public static class FileSync implements FileAccess.FileSync {

        FileDescriptor outDescriptor;

        FileSync(FileOutputStream os) throws IOException {
            outDescriptor = os.getFD();
        }

        public void sync() throws IOException {
            outDescriptor.sync();
        }
    }

    public static class FileAccessRes implements FileAccess {

        public boolean isStreamElement(String elementName) {
            return getClass().getResource(elementName) != null;
        }

        public InputStream openInputStreamElement(final String fileName)
        throws IOException {

            InputStream fis = null;

            try {
                fis = getClass().getResourceAsStream(fileName);

                if (fis == null) {
                    ClassLoader cl =
                        Thread.currentThread().getContextClassLoader();

                    if (cl != null) {
                        fis = cl.getResourceAsStream(fileName);
                    }
                }
            } catch (Throwable t) {

                //
            } finally {
                if (fis == null) {
                    throw new FileNotFoundException(fileName);
                }
            }

            return fis;
        }

        public void createParentDirs(java.lang.String filename) {}

        public void removeElement(java.lang.String filename) {}

        public void renameElement(java.lang.String oldName,
                                  java.lang.String newName) {}

        public java.io.OutputStream openOutputStreamElement(String streamName)
        throws IOException {
            throw new IOException();
        }

        public FileAccess.FileSync getFileSync(OutputStream os)
        throws IOException {
            throw new IOException();
        }
    }
}
