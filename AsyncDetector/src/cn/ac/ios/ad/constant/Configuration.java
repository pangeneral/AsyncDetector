package cn.ac.ios.ad.constant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Configuration {
	
	public static final boolean isServer = false;
	
	public static final String XLS_OUTPUT_FOLDER = "./output/";
	
	public static final String XML_RESULT_FILE_NAME = "_DetectorResult.xls";
	public static final String EXCEPTION_FILE_NAME = "_Exception.txt";
	public static final String LONG_TIME_FILE_NAME = "_LONG_TIME_Exception.txt";
	
	
	public static String ApkBasePath;//apk path
	public static String JimpleBasePath;
	public final static String ErrorFolder="error";
	public static String AndroidPath= isServer?"/HostServer/xxx/android-platforms":"D:\\download\\soot-path\\android-platforms-master\\";
	public static String JavaHome=System.getProperty("java.home") + File.separator + "lib" + File.separator + "rt.jar";
	public static String CurrentApkName;

	
	public static String getPath(String[] lineArray){
		if( lineArray.length == 2)
			return lineArray[1];
		String result=lineArray[1]+File.separator;
		for(int i=2;i < lineArray.length-1; i++)
			result+=lineArray[i]+File.separator;
		result+=lineArray[lineArray.length-1];
		return result;
	}
	
	public static void initParameter() throws IOException{
		String configurationFilePath = "configure"+File.separator+"path_configuration.txt";
		BufferedReader br = new BufferedReader(new FileReader(new File(configurationFilePath)));
		String strLine=null;
		while( (strLine=br.readLine()) != null ){
			if(strLine.startsWith("//"))
				continue;
			if( strLine.split("\\s+").length < 2)
				continue;
			String lineArray[] = strLine.split("\\s+");
			switch(lineArray[0]){
				case "apkBasePath":
//					Constant.ApkBasePath = basePath+File.separator+Constant.getPath(lineArray);
					Configuration.ApkBasePath = Configuration.getPath(lineArray);
					break;
				case "jimpleBasePath":
//					Constant.JimpleBasePath = basePath+File.separator+Constant.getPath(lineArray);
					Configuration.JimpleBasePath = Configuration.getPath(lineArray);
					break;
				case "javaHome":
//					Constant.JavaHome = basePath+File.separator+Constant.getPath(lineArray);
					Configuration.JavaHome = Configuration.getPath(lineArray);
					break;
				case "androidPath":
//					Constant.AndroidPath = basePath+File.separator+Constant.getPath(lineArray);
					Configuration.AndroidPath = Configuration.getPath(lineArray);
					break;
				default:
					break;
			}
		}
		br.close();
	}
	
	//初始化所有的路径
	public static void initPath(){
		File jimpleFolder = new File(Configuration.JimpleBasePath);
    	if( !jimpleFolder.exists() )
    		jimpleFolder.mkdir();
    	String errorPath = Configuration.JimpleBasePath+File.separator+Configuration.CurrentApkName.substring(0, Configuration.CurrentApkName.indexOf("."))
				+File.separator+Configuration.ErrorFolder;
    	File errorFolder = new File(errorPath);
    	if( !errorFolder.exists())
    		return;
    	for(String s:errorFolder.list()){
    		String filePath = errorFolder+File.separator+s;
    		File currentFile = new File(filePath);
    		currentFile.delete();
    	}
	}
}
