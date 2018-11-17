package com.example.memoryleaktest;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {
	
	private ListReferenceAsyncTask mListAsyncTask = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		List<View> list = new ArrayList<View>();
		list.add(findViewById(R.id.view1));
		mListAsyncTask = new ListReferenceAsyncTask(list);
		mListAsyncTask.execute("");
		mListAsyncTask.cancel(true);
		
	}

}
