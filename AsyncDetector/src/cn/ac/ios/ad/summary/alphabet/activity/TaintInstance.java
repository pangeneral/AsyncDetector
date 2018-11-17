package cn.ac.ios.ad.summary.alphabet.activity;

import soot.SootField;
import soot.Unit;
import soot.Value;

/**
 * If AsyncTask holds reference to Activity, at least one of its field holds reference to Activity.
 * 'taintedField' is the tainted field. 
 * 'argIndex' is the index of argument which is assigned to 'tainted field' in the init method of AsyncTask.
 * 'source' is the source of the argument.
 * @author panlj
 *
 */
public class TaintInstance{
	SootField taintedField;
	int argIndex;
	Unit source;
	
	public TaintInstance(SootField taintedField,int argIndex,Unit source){
		this.taintedField = taintedField;
		this.argIndex = argIndex;
		this.source = source;
	}
	
	public int getArgIndex() {
		return argIndex;
	}
	public void setArgIndex(int argIndex) {
		this.argIndex = argIndex;
	}

	public SootField getTaintedField() {
		return taintedField;
	}

	public void setTaintedField(SootField taintedField) {
		this.taintedField = taintedField;
	}

	public Unit getSource() {
		return source;
	}

	public void setSource(Unit source) {
		this.source = source;
	}
	
}
