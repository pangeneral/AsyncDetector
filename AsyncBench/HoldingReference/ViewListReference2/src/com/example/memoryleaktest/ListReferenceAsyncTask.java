package com.example.memoryleaktest;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.view.View;

public class ListReferenceAsyncTask extends AsyncTask<String, String, String> {

	private List<View> mList = new ArrayList<View>();

	public ListReferenceAsyncTask(View view) {
		mList.add(view);
	}

	@Override
	protected String doInBackground(String... params) {

		StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append(mList);

		return stringBuffer.toString();
	}
}
