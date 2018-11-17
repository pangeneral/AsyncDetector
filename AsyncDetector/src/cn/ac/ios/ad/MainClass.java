package cn.ac.ios.ad;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import soot.Pack;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.Transform;
import soot.options.Options;
import soot.util.Chain;
import cn.ac.ios.ad.constant.Configuration;
import cn.ac.ios.ad.record.ExceptionRecord;
import cn.ac.ios.ad.record.RecordController;
import cn.ac.ios.ad.util.FileUtil;
import cn.ac.ios.ad.util.Log;

public class MainClass{	
	private AsyncTaskDetector detector;
	
	private Object mLock = new Object();
			
	
	public static void main(String[] args) throws Exception{	
		Configuration.CurrentApkName = args[0];
		RecordController.getInstance().mApkName = args[0];
		RecordController.getInstance().newSheetOutput();
		Configuration.initParameter();
		Configuration.initPath();
		if(args.length > 1){
			Configuration.ApkBasePath = args[1];
		}
		System.out.println("path initialization finished");
		MainClass studyInstance = new MainClass();
		
		String filePath= Configuration.ApkBasePath+File.separator+Configuration.CurrentApkName;
		File outputFolder = new File(Configuration.JimpleBasePath+File.separator+
			Configuration.CurrentApkName.substring(0, Configuration.CurrentApkName.indexOf(".")));
    	if( !outputFolder.exists() )
    		outputFolder.mkdir();
    	File errorFolder = new File(outputFolder+File.separator+Configuration.ErrorFolder);
    	if( !errorFolder.exists() )
    		errorFolder.mkdir();
    	studyInstance.analyzeApk(filePath, outputFolder.getAbsolutePath());
	}
	
	/**
	 * 设置soot的命令行参数
	 * @param filePath 要解析的apk文件的路径
	 */
	private List<String> setSootArgs(String filePath,String outputFolder){
		List<String> argList = new ArrayList<String>();
		argList.add("-process-dir");
		argList.add(filePath);
		argList.add("-android-jars");
		argList.add(Configuration.AndroidPath);
//		argList.add("-whole-program");
		argList.add("-no-bodies-for-excluded");
		argList.add("-app");
//		argList.add("-W");
//		argList.add("-p");
//		argList.add("wjop");
//		argList.add("enabled:true");
		
		  //enable points-to analysis
//		argList.add("-p");
//		argList.add("cg");
//		argList.add("enabled:true");
		
		  //enable Spark
//		argList.add("-p");
//		argList.add("cg.spark");
//		argList.add("enabled:true");
		
		argList.add("-process-multiple-dex");
		argList.add("-src-prec");
		argList.add("apk");
		argList.add("-output-format");
		argList.add("jimple");
		argList.add("-allow-phantom-refs");
		argList.add("-output-dir");
		argList.add(outputFolder);
		return argList;
	}
	
	public void analyzeApk(final String filePath,final String outputFolder) throws IOException{
		long startTime = System.currentTimeMillis();
		List<String> argsList = this.setSootArgs(filePath,outputFolder);
		String[] soot_args = new String[argsList.size()];
		argsList.toArray(soot_args);
		
		String excludeArray[] = new String[]{"jdk.*","sun.*","javax.*","android.*","org.*"};
//		String excludeArray[] = new String[]{"jdk.*","sun.*","javax.*","org.*"};
		Options.v().set_exclude(Arrays.asList(excludeArray));
		
		Pack p1 = PackManager.v().getPack("jtp");
		this.detector = new AsyncTaskDetector();
		AsyncTaskTransformer transformer = new AsyncTaskTransformer();
		String phaseName="jtp."+Configuration.CurrentApkName.substring(0,Configuration.CurrentApkName.indexOf("."));
		Transform t1 = new Transform(phaseName,transformer);
		p1.add(t1);
		
		soot.Main.main(soot_args);
		long detectorStartTime = System.currentTimeMillis();
		
		try {
			this.detector.detectAsyncTask();
			Log.e("after detector.detectAsyncTask");
			synchronized (mLock) {
				RecordController.getInstance().getTimeOutput().sootTime = detectorStartTime - startTime;
				RecordController.getInstance().getTimeOutput().detecorTime =  System.currentTimeMillis() - detectorStartTime;
				RecordController.getInstance().output();
				killSelf();
			}
		} catch (Exception e) {
			ExceptionRecord.saveException(e, filePath);
		}finally{
//			FileUtil.deleteFile(outputFolder);
			killSelf();
		}
		
	}
	
	@SuppressWarnings("unused")
	private void printClassMessage(){
		Chain<SootClass> classes = Scene.v().getClasses();
		Scene.v().getLibraryClasses();
		Chain<SootClass> applicationClasses = Scene.v().getApplicationClasses();
		Chain<SootClass> libraryClasses = Scene.v().getLibraryClasses();
		System.out.println("Class Number: "+classes.size());
		System.out.println("Application class Number: "+applicationClasses.size());
		System.out.println("Library class Number: "+libraryClasses.size());
		for(SootClass currentClass:classes){
			if( !applicationClasses.contains(currentClass) && !libraryClasses.contains(currentClass) )
				System.out.println(currentClass.getName());
		}
		System.out.println("**********************************************");
		for(SootClass currentClass:applicationClasses)
			System.out.println(currentClass.getName());
		System.out.println("**********************************************");
		for(SootClass currentClass:libraryClasses){
			Log.i("********************************$$$$$$$$$$$$$");
			System.out.println(currentClass.getName());
		}
		System.out.println("**********************************************");
	}
	
	private void killSelf(){
		// get name representing the running Java virtual machine.  
		String name = ManagementFactory.getRuntimeMXBean().getName();  
		Log.e(name);
		// get pid  
		String pid = name.split("@")[0];  
		String os = System.getProperty("os.name"); 
        try {
            if (os != null && os.startsWith("Windows")){
            	Runtime.getRuntime().exec("Taskkill /f /IM " + pid);
            }else{
            	String[] cmd ={"sh","-c","kill -9 "+pid};
                Runtime.getRuntime().exec(cmd);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}
	
//	public void printCallGraph(){
//		CallGraph cg = this.detector.getCallGraph();
//		Iterator it = cg.iterator();
//		while( it.hasNext() ){
//			Edge e = (Edge)it.next();
//			System.out.println(e.src().getSignature()+" called "+e.tgt().getSignature()+" through stmt "+e.srcStmt());
//		}
//	}
}
