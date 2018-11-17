package com.example.memoryleaktest;

import java.util.HashMap;
import java.util.Map;

import android.os.AsyncTask;
import android.view.View;

public class MapReferenceAsyncTask extends AsyncTask<String, String, String> {

	private Map<View, String> mMap = new HashMap<View,String>();

	public MapReferenceAsyncTask(View view) {
		mMap.put(view, "value");
	}

	@Override
	protected String doInBackground(String... params) {

		StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append(mMap);

		return stringBuffer.toString();
	}
}
