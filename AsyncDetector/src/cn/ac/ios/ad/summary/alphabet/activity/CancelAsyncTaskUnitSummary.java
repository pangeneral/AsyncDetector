package cn.ac.ios.ad.summary.alphabet.activity;

import soot.Unit;

/**
 * Execute cancel() method to cancel a AsyncTask
 * @author panlj
 *
 */
public class CancelAsyncTaskUnitSummary extends ActivityOperationUnitSummary{

	public CancelAsyncTaskUnitSummary(Unit currentUnit) {
		super(currentUnit);
	}
	
	public CancelAsyncTaskUnitSummary(Unit currentUnit,int argIndex){
		super(currentUnit,argIndex);
	}

	@Override
	public String getSummary() {
		// TODO Auto-generated method stub
		return SummaryAlphabet.CANCEL_ASYNC;
	}

}
