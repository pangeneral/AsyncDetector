/* AsyncDetecotr - an Android async component misuse detection tool
 * Copyright (C) 2018 Linjie Pan
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package cn.ac.ios.asyncdetect.util;

import java.io.File;

/**
 * 
 * @author Linjie Pan
 * @version 1.0
 */
public class FileUtil {
	
	/**
	 * Delete empty directory 
	 * @param dir is the directory that we need to delete
	 */
	public static void doDeleteEmptyDir(String dir) {
		boolean success = (new File(dir)).delete();
		if (success) {
			Log.i("Successfully deleted empty directory: " + dir);
		} else {
			Log.i("Failed to delete empty directory: " + dir);
		}
	}

	/**
	 * Recursively delete all files in a directory and files in the sub directory of current directory
	 * @param dir 
	 * @return  Returns "true" if all deletions were successful. If a
	 *         deletion fails, the method stops attempting to delete and returns
	 *         "false".
	 */
	private static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				deleteDir(new File(dir, children[i]));
			}
		}
		if(dir.getName().contains(".txt")){
			return true;
		}
		return dir.delete();
	}

	public static void deleteFile(String filePath) {
		File file = new File(filePath);
		if (file.exists()) {
			if (file.isFile()) {
				file.delete();
			} else {
				deleteDir(file);
			}
		}
	}
}
