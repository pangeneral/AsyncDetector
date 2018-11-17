package com.example.memoryleaktest;

import android.os.AsyncTask;
import android.view.View;

public class ArrayReferenceAsyncTask extends AsyncTask<String, String, String> {

	private View[] mList = null;

	public ArrayReferenceAsyncTask(View[] list) {
		mList = list;
	}

	@Override
	protected String doInBackground(String... params) {

		StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append(mList);

		return stringBuffer.toString();
	}
}
