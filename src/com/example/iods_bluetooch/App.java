package com.example.iods_bluetooch;

import android.app.Application;

public class App extends Application{
	
	public static App app;
	
	@Override
	public void onCreate() {
		app=this;
		super.onCreate();
		
	}

}
