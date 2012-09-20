package com.octo.android.robospice.request.simple;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;

import com.google.common.io.CharStreams;
import com.octo.android.robospice.request.ContentRequest;

public class SimpleTextRequest extends ContentRequest<String> {

	private String url;

	public SimpleTextRequest(String url) {
		super(String.class);
		this.url = url;
	}

	// can't use activity here or any non serializable field
	// will be invoked in remote service
	@Override
	public final String loadDataFromNetwork() throws Exception {
		try {
			Log.d(getClass().getName(), "Call web service " + url);
			return CharStreams.toString(new InputStreamReader(new URL(url).openStream(), "UTF-8"));
		} catch (MalformedURLException e) {
			Log.e(getClass().getName(), "Unable to create image URL", e);
			return null;
		} catch (IOException e) {
			Log.e(getClass().getName(), "Unable to download image", e);
			return null;
		}
	}

	// can't use activity here or any non serializable field
	// will be invoked in remote service
	protected final String getUrl() {
		return this.url;
	}

}
