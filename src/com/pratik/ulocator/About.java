package com.pratik.ulocator;

import com.actionbarsherlock.app.SherlockActivity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

public class About extends SherlockActivity {

	private WebView webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);

		// Using the webview to load the help page
		webView = (WebView) findViewById(R.id.webview);

		webView.setBackgroundColor(0x00000000);
		// pointing to the html page on the file system
		webView.loadUrl("file:///android_asset/about.html");

	}

}
