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

public class BasicType {
	public final static String STRING = "java.lang.String";
	
	public final static String BASIC_INT = "int";
	public final static String BASIC_FLOAT = "float";
	public final static String BASIC_DOUBLE = "double";
	public final static String BASIC_BYTE = "byte";
	public final static String BASIC_CHAR = "char";
	public final static String BASIC_LONG = "long";
	public final static String BASIC_SHORT = "short";
	public final static String BASIC_BOOL = "boolean";
    
	public final static String INTEGER = "java.lang.Integer";
	public final static String FLOAT = "java.lang.Float";
	public final static String DOUBLE = "java.lang.Double";
	public final static String BYTE = "java.lang.Byte";
	public final static String CHAR = "java.lang.Character";
	public final static String LONG = "java.lang.Long";
	public final static String SHORT = "java.lang.Short";
	public final static String BOOL = "java.lang.Boolean";
	
	public final static String WEAK_REFERENCE="java.lang.ref.WeakReference";
	public final static String VIEW="android.view.View";
	public final static String MAP="java.util.Map";
	public final static String COLLECTION="java.util.Collection";
	
	public final static String ACTIVITY="android.app.Activity";
	
	public final static String OBJECT="java.lang.Object";
	
	public final static String ON_CLICK_LISTENER="android.view.View$OnClickListener";
	public final static String LISTENER="android\\.view\\.View\\$On.*Listener";
	
}
