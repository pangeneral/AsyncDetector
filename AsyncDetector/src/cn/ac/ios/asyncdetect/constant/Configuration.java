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

package cn.ac.ios.asyncdetect.constant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.ac.ios.asyncdetect.util.Log;

public class Configuration {
	/**
	 * Flag of debug . Log will be output in console if the value is true, or
	 * will not. This flag is used in cn.ac.ios.asyncdetect.util/Log.java.
	 * */
	public static final boolean DEBUG = true;
	/**
	 * The file of configuration.
	 * */
	private static final String CONFIGURATION_FILE = "./configure/path_configuration.txt";
	/**
	 * The path of Android Platform, especially the android.jar in different
	 * version of Android .
	 * */
	private static String ANDROID_PLATFORM_PATH = null;
	/**
	 * The path to record the results, such as xxx_time.txt, xxx_error.txt.
	 * */
	public static final String OUTPUT_FOLDER = "./output/";
	/**
	 * The file to record the exception information of this program.
	 */
	public static final String EXCEPTION_FILE_NAME = "_Exception.txt";
	/**
	 * The file to record the long time exception of this program, caused by
	 * Path Explosion.
	 */
	public static final String LONG_TIME_FILE_NAME = "_LONG_TIME_Exception.txt";
	/**
	 * The dictionary path of the apk under analysis, which depends on the
	 * second argument of the program.
	 */
	private static String APK_DICTIONARY;
	/**
	 * The dictionary to storage the jimple files.
	 */
	private static String JIMPLE_OUTPUT_FOLDER;
	/**
	 * The file to record the summary statement of misuse.
	 */
	public static final String ERROR_FOLDER = "error";
	/**
	 * The java home path , especially the rt.jar called by Soot.
	 */
	public static String JAVA_HOME = getJavaHome();
	/**
	 * Current apk name under analysis.
	 */
	private static String sCurrentApkName;
	/**
	 * Current apk name under analysis without suffix.
	 */
	private static String sCurrentApkNameWithoutSUffix = null;
	/**
	 * Auxiliary variable, used when batch processing, to identify which data
	 * set.
	 */
	private static String sDataSet = "";

	/**
	 * 
	 * @return dataSetName , may be null or empty
	 */
	public static String getDataSet() {
		return sDataSet;
	}

	/**
	 * 
	 * @return name of current apk under analysis which is set by the first
	 *         argument in this program like , wechat.apk
	 */
	public static String getCurrentApkName() {
		return sCurrentApkName;
	}

	/**
	 * @return Current apk name under analysis without suffix.
	 */
	public static String getCurrentApkNameWithoutSUffix() {
		if (sCurrentApkNameWithoutSUffix == null) {
			sCurrentApkNameWithoutSUffix = sCurrentApkName.substring(0,
					sCurrentApkName.lastIndexOf("."));
		}
		return sCurrentApkNameWithoutSUffix;
	}

	private static String getJavaHome() {
		return System.getProperty("java.home") + File.separator + "lib"
				+ File.separator + "rt.jar";
	}

	private static String getPath(String[] lineArray) {
		if (lineArray.length == 2)
			return lineArray[1];
		String result = lineArray[1] + File.separator;
		for (int i = 2; i < lineArray.length - 1; i++)
			result += lineArray[i] + File.separator;
		result += lineArray[lineArray.length - 1];
		return result;
	}

	private static void initParameter() {

		try {

			BufferedReader br = new BufferedReader(new FileReader(new File(
					CONFIGURATION_FILE)));

			String strLine = null;
			while ((strLine = br.readLine()) != null) {
				if (strLine.startsWith("//"))
					continue;
				if (strLine.split("\\s+").length < 2)
					continue;
				String lineArray[] = strLine.split("\\s+");
				switch (lineArray[0]) {
				case "apkBasePath":
					Configuration.APK_DICTIONARY = Configuration
							.getPath(lineArray);
					break;
				case "jimpleBasePath":
					Configuration.JIMPLE_OUTPUT_FOLDER = Configuration
							.getPath(lineArray);
					break;
				case "javaHome":
					Configuration.JAVA_HOME = Configuration.getPath(lineArray);
					break;
				case "androidPath":
					Configuration.ANDROID_PLATFORM_PATH = Configuration
							.getPath(lineArray);
					break;
				default:
					break;
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initialize all folder path
	 * 
	 */
	private static void initPath() {
		
		File jimpleFolder = new File(Configuration.JIMPLE_OUTPUT_FOLDER);
		if (!jimpleFolder.exists())
			jimpleFolder.mkdir();
		
		JIMPLE_OUTPUT_FOLDER = JIMPLE_OUTPUT_FOLDER + File.separator + 
				sCurrentApkName.substring(0, sCurrentApkName.indexOf("."));
		
		File outputFolder = new File(JIMPLE_OUTPUT_FOLDER);
		if (!outputFolder.exists())
			outputFolder.mkdir();
		
		File errorFolder = new File(JIMPLE_OUTPUT_FOLDER + File.separator + Configuration.ERROR_FOLDER);	
		if (errorFolder.exists()) {
			for (String s : errorFolder.list()) {
				String filePath = errorFolder + File.separator + s;
				File currentFile = new File(filePath);
				currentFile.delete();
			}
		}else{
			errorFolder.mkdir();
		}
	}

	/**
	 * Initialize all the configuration items & parameters.
	 * 
	 * @param args
	 */
	public static void init(String[] args) {
		Configuration.sCurrentApkName = args[0];
		initParameter();
		initPath();
		if (args.length > 1) {
			APK_DICTIONARY = args[1];
		}
		if (args.length > 2) {
			sDataSet = args[2];
		}
		Log.i("initParameter & initPath have been finished!");
	}

	/**
	 * set params of soot with the commandline way 
	 * 
	 * @param filePath
	 *            is the path of apk under analysis
	 * @param outputFolder
	 *            is the folder that saves the generated jimple file
	 */
	public static List<String> getSootArgs() {
		List<String> argList = new ArrayList<String>();
		argList.add("-process-dir");
		argList.add(getApkPath());
		argList.add("-android-jars");
		argList.add(ANDROID_PLATFORM_PATH);
		argList.add("-no-bodies-for-excluded");
		argList.add("-app");

		argList.add("-process-multiple-dex");
		argList.add("-src-prec");
		argList.add("apk");
		argList.add("-output-format");
		argList.add("jimple");
		argList.add("-allow-phantom-refs");
		argList.add("-output-dir");
		argList.add(JIMPLE_OUTPUT_FOLDER);
		return argList;
	}
	
	/**
	 * 
	 * @return apk path, APK_DICTIONARY + sCurrentApkName.
	 */
	public static String getApkPath() {
		return APK_DICTIONARY + File.separator + sCurrentApkName;
	}
	/**
	 * 
	 * @return JIMPLE_OUTPUT_FOLDER, ./jimple/ + sCurrentApkName without suffix like '.apk'.
	 */
	public static String getJimpleFolder() {
		return JIMPLE_OUTPUT_FOLDER;
	}
}
