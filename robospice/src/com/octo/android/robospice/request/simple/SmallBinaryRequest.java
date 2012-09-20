package com.octo.android.robospice.request.simple;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;

import com.google.common.io.ByteStreams;
import com.octo.android.robospice.request.ContentRequest;

/**
 * Downloads small images in size. All data is passed to the listener using
 * memory. This class is meant to help download small images (like thumbnails).
 * If you wish to download bigger documents (or if you don't know the size of
 * your documents), you would be better using {@link BigBinaryRequest}.
 * 
 * @author sni
 * 
 */
public class SmallBinaryRequest extends ContentRequest<InputStream> {

	protected String url;

	public SmallBinaryRequest(String url) {
		super(InputStream.class);
		this.url = url;
	}

	@Override
	public final InputStream loadDataFromNetwork() throws Exception {
		try {
			InputStream is = new URL(url).openStream();
			byte[] bytes = ByteStreams.toByteArray(is);
			return new ByteArrayInputStream(bytes);
		} catch (MalformedURLException e) {
			Log.e(getClass().getName(), "Unable to create image URL", e);
			return null;
		} catch (IOException e) {
			Log.e(getClass().getName(), "Unable to download image", e);
			return null;
		}
	}

	protected final String getUrl() {
		return this.url;
	}

}
