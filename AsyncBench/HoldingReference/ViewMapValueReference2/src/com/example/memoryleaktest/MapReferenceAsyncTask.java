package com.example.memoryleaktest;

import java.util.HashMap;
import java.util.Map;

import android.os.AsyncTask;
import android.view.View;

public class MapReferenceAsyncTask extends AsyncTask<String, String, String> {

	private Map<String, View> mMap = new HashMap<String, View>();

	public MapReferenceAsyncTask(View view) {
		mMap.put("value", view);
	}

	@Override
	protected String doInBackground(String... params) {

		StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append(mMap);

		return stringBuffer.toString();
	}
}
