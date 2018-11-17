package cn.ac.ios.ad.constant;

public class MethodSignature {
	
	public final static int LIFE_CYCLE_NUMBER=8;
	public final static String INIT="void <init>()";
	public final static String ON_CREATE="void onCreate(android.os.Bundle)";
	public final static String ON_START="void onStart()";
	public final static String ON_RESUME="void onResume()";
	public final static String ON_PAUSE="void onPause()";
	public final static String ON_STOP="void onStop()";
	public final static String ON_DESTROY="void onDestroy()";
	public final static String ON_RESTART="void onRestart()";
	public final static String signatureArray[]=new String[]{MethodSignature.INIT,MethodSignature.ON_CREATE,MethodSignature.ON_START,
		MethodSignature.ON_RESUME,MethodSignature.ON_PAUSE,MethodSignature.ON_STOP,MethodSignature.ON_DESTROY,MethodSignature.ON_RESTART};
	
	public final static String EXECUTE_SIGNATURE="<android.os.AsyncTask: android.os.AsyncTask execute(java.lang.Object[])>";
	public final static String CANCEL_SIGNATURE="<android.os.AsyncTask: boolean cancel(boolean)>";
}
