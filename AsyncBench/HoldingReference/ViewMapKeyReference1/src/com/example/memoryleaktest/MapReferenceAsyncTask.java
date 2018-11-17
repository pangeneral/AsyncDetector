package com.example.memoryleaktest;

import java.util.Map;

import android.os.AsyncTask;
import android.view.View;

public class MapReferenceAsyncTask extends AsyncTask<String, String, String> {

	private Map<View, String> mMap = null;

	public MapReferenceAsyncTask(Map<View, String> map) {
		mMap = map;
	}

	@Override
	protected String doInBackground(String... params) {

		StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append(mMap);

		return stringBuffer.toString();
	}
}
