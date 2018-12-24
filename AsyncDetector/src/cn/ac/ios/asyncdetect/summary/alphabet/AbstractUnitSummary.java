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

package cn.ac.ios.asyncdetect.summary.alphabet;

import soot.SootMethod;
import soot.Unit;

/**
 * Summary of unit or statement must at least include a string which indicate its operation 
 * @author Linjie Pan
 * @version 1.0
 */
public abstract class AbstractUnitSummary{
	
	private SootMethod mCurrentMethod;
	private Unit mCurrentUnit;
	
	public SootMethod getCurrentMethod() {
		return mCurrentMethod;
	}

	public void setCurrentMethod(SootMethod currentMethod) {
		this.mCurrentMethod = currentMethod;
	}

	public Unit getUnit(){
		return mCurrentUnit;
	}
	
	public AbstractUnitSummary(Unit currentUnit){
		this.mCurrentUnit = currentUnit;
	}
	
	public abstract String getSummary();
}
