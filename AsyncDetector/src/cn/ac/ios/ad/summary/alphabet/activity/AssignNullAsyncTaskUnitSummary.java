package cn.ac.ios.ad.summary.alphabet.activity;

import java.util.List;

import soot.Unit;
import soot.jimple.Expr;

public class AssignNullAsyncTaskUnitSummary extends ActivityOperationUnitSummary {
	
	public AssignNullAsyncTaskUnitSummary(Unit currentUnit) {
		super(currentUnit);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getSummary() {
		// TODO Auto-generated method stub
		return SummaryAlphabet.ASSIGN_NULL_ASYNC;
	}
}
