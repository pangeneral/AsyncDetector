package com.example.memoryleaktest;

import java.util.HashSet;
import java.util.Set;

import android.os.AsyncTask;
import android.view.View;

public class SetReferenceAsyncTask extends
		AsyncTask<String, String, String> {

	private Set<View> mSet = new HashSet<View>();

	public SetReferenceAsyncTask(View view) {
		mSet.add(view);
	}

	@Override
	protected String doInBackground(String... params) {

		StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append(mSet);

		return stringBuffer.toString();
	}
}
