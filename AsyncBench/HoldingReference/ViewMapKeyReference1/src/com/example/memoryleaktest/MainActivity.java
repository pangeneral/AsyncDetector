package com.example.memoryleaktest;

import java.util.HashMap;
import java.util.Map;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

	private MapReferenceAsyncTask mMapAsyncTask = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Map<View,String> map = new HashMap<View,String>();
		map.put(findViewById(R.id.view1), "value");
		mMapAsyncTask = new MapReferenceAsyncTask(map);
		mMapAsyncTask.execute("");
		mMapAsyncTask.cancel(true);

		
	}

}
