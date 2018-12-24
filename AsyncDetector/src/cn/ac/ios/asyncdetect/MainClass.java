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
package cn.ac.ios.asyncdetect;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import soot.Pack;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.Transform;
import soot.options.Options;
import soot.util.Chain;
import cn.ac.ios.asyncdetect.constant.Configuration;
import cn.ac.ios.asyncdetect.record.ExceptionRecord;
import cn.ac.ios.asyncdetect.record.RecordController;
import cn.ac.ios.asyncdetect.util.Log;

/**
 * Entry point of AsyncDetector
 * @author Linjie Pan
 * @version 1.0
 */
public class MainClass {
	/**
	 * AsyncTaskDetector instance.
	 */
	private AsyncTaskDetector mDetector = null;

	public static void main(String[] args) throws Exception {
		
		Configuration.init(args);
		
		MainClass studyInstance = new MainClass();
		
		studyInstance.analyzeApk();
	}

	public void analyzeApk()
			throws IOException {

		final Object lock = new Object();
		
		final TimeOutDetectionThread thread = new TimeOutDetectionThread(lock);
		thread.start();
		
		long startTime = System.currentTimeMillis();
		List<String> argsList = Configuration.getSootArgs();
		String[] soot_args = new String[argsList.size()];
		argsList.toArray(soot_args);
		
		String excludeArray[] = new String[]{"jdk.*","sun.*","javax.*","android.*","org.*"};
		Options.v().set_exclude(Arrays.asList(excludeArray));
		
		Pack p1 = PackManager.v().getPack("jtp");
		this.mDetector = new AsyncTaskDetector();
		AsyncTaskTransformer transformer = new AsyncTaskTransformer();
		String currentApkName = Configuration.getCurrentApkName();
		String phaseName = "jtp."
				+ currentApkName.substring(0, currentApkName.indexOf("."));
		Transform t1 = new Transform(phaseName,transformer);
		p1.add(t1);
		
		soot.Main.main(soot_args);
		
		long detectorStartTime = System.currentTimeMillis();
		
		try {
			Log.i("----------detector.detectAsyncTask() starts ");
			this.mDetector.detectAsyncTask();
			Log.i(" --- detector.detectAsyncTask has been finished");
			synchronized (lock) {
				RecordController.getInstance().getTimeOutput().sootTime = detectorStartTime - startTime;
				RecordController.getInstance().getTimeOutput().detecorTime =  System.currentTimeMillis() - detectorStartTime;
				RecordController.getInstance().output();
			}
		} catch (Exception e) {
			ExceptionRecord.saveException(e);
		}finally{
			thread.killSelf();
		}
		
	}
	
	@SuppressWarnings("unused")
	private void printClassMessage(){
		Chain<SootClass> classes = Scene.v().getClasses();
		Scene.v().getLibraryClasses();
		Chain<SootClass> applicationClasses = Scene.v().getApplicationClasses();
		Chain<SootClass> libraryClasses = Scene.v().getLibraryClasses();
		Log.i("Class Number: "+classes.size());
		Log.i("Application class Number: "+applicationClasses.size());
		Log.i("Library class Number: "+libraryClasses.size());
		for(SootClass currentClass:classes){
			if( !applicationClasses.contains(currentClass) && !libraryClasses.contains(currentClass) )
				Log.i(currentClass.getName());
		}
		Log.i("**********************************************");
		for(SootClass currentClass:applicationClasses)
			Log.i(currentClass.getName());
		Log.i("**********************************************");
		for(SootClass currentClass:libraryClasses){
			Log.i("********************************$$$$$$$$$$$$$");
			Log.i(currentClass.getName());
		}
		Log.i("**********************************************");
	}
}
