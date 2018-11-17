package com.example.nullreferencetest;

import java.lang.ref.WeakReference;

import android.os.AsyncTask;
import android.widget.TextView;

public class Tasker extends AsyncTask<String,String,String>{

	private WeakReference<TextView> referenceView;
	
	public Tasker(TextView view){
		this.referenceView = new WeakReference<TextView>(view);
	}
	
	@Override
	protected String doInBackground(String... params) {
		// TODO Auto-generated method stub
		return null;
	}

}
