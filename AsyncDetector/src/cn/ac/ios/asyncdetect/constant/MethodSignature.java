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
