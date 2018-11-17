package com.example.memoryleaktest;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
	
	private ListReferenceAsyncTask mListAsyncTask = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mListAsyncTask = new ListReferenceAsyncTask(findViewById(R.id.view1));
		mListAsyncTask.execute("");
		mListAsyncTask.cancel(true);
		
	}

}
