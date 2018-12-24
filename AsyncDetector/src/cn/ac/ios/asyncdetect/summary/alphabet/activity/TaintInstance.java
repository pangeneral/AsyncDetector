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

package cn.ac.ios.asyncdetect.summary.alphabet.activity;

import soot.SootField;
import soot.Unit;

/**
 * If AsyncTask holds reference to Activity, at least one of its field holds reference to Activity.
 * 'taintedField' is the tainted field. 
 * 'argIndex' is the index of argument which is assigned to 'tainted field' in the init method of AsyncTask.
 * 'source' is the source of the argument.
 * @author Linjie Pan
 * @version 1.0
 */
public class TaintInstance{
	SootField mTaintedField;
	int mArgIndex;
	Unit mSource;
	
	public TaintInstance(SootField taintedField,int argIndex,Unit source){
		this.mTaintedField = taintedField;
		this.mArgIndex = argIndex;
		this.mSource = source;
	}
	
	public int getArgIndex() {
		return mArgIndex;
	}
	public void setArgIndex(int argIndex) {
		this.mArgIndex = argIndex;
	}

	public SootField getTaintedField() {
		return mTaintedField;
	}

	public void setTaintedField(SootField taintedField) {
		this.mTaintedField = taintedField;
	}

	public Unit getSource() {
		return mSource;
	}

	public void setSource(Unit source) {
		this.mSource = source;
	}
	
}
