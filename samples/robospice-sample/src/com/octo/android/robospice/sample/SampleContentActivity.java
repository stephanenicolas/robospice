package com.octo.android.robospice.sample;

import java.io.File;
import java.io.InputStream;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import roboguice.util.temp.Ln;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.listener.RequestProgress;
import com.octo.android.robospice.request.listener.RequestProgressListener;
import com.octo.android.robospice.request.simple.BigBinaryRequest;
import com.octo.android.robospice.request.simple.SimpleTextRequest;
import com.octo.android.robospice.sample.model.json.WeatherResult;
import com.octo.android.robospice.sample.request.WeatherRequestJson;
import com.octo.android.robospice.sample.request.WeatherRequestXml;

@ContentView(R.layout.main)
public class SampleContentActivity extends BaseSampleContentActivity {

	// ============================================================================================
	// ATTRIBUTES
	// ============================================================================================

	@InjectView(R.id.textview_hello_cnil)
	private TextView mLoremTextView;
	@InjectView(R.id.textview_hello_json)
	private TextView mCurrentWeatherJsonTextView;
	@InjectView(R.id.textview_hello_xml)
	private TextView mCurrentWeatherXmlTextView;
	@InjectView(R.id.textview_hello_image)
	private TextView mImageTextView;

	SimpleTextRequest loremRequest;
	BigBinaryRequest imageRequest;
	WeatherRequestJson weatherRequest;
	WeatherRequestXml weatherRequestXml;

	// ============================================================================================
	// ACITVITY LIFE CYCLE
	// ============================================================================================

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Initializes the logging
		// Log a message (only on dev platform)
		Ln.i("onCreate");

		loremRequest = new SimpleTextRequest("http://www.loremipsum.de/downloads/original.txt");
		weatherRequest = new WeatherRequestJson("75000");
		weatherRequestXml = new WeatherRequestXml("75000");
		File cacheFile = new File(getApplication().getCacheDir(), "earth.jpg");
		imageRequest = new BigBinaryRequest("http://earthobservatory.nasa.gov/blogs/elegantfigures/files/2011/10/globe_west_2048.jpg", cacheFile);
	}

	@Override
	protected void onStart() {
		super.onStart();

		getJsonContentManager().execute(loremRequest, "lorem.txt", DurationInMillis.ONE_DAY, new LoremRequestListener());

		getJsonContentManager().execute(weatherRequest, "75000.json", DurationInMillis.ONE_DAY, new WeatherRequestJsonListener());

		getJsonContentManager().execute(weatherRequestXml, "75000.xml", DurationInMillis.ONE_DAY, new WeatherRequestXmlListener());

		getJsonContentManager().execute(imageRequest, "logo", DurationInMillis.ONE_DAY, new ImageRequestListener());

	}

	// ============================================================================================
	// INNER CLASSES
	// ============================================================================================

	public final class LoremRequestListener implements RequestListener<String> {

		@Override
		public void onRequestFailure(SpiceException spiceException) {
			Toast.makeText(SampleContentActivity.this, "failure", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onRequestSuccess(final String result) {
			Toast.makeText(SampleContentActivity.this, "success", Toast.LENGTH_SHORT).show();
			String originalText = mLoremTextView.getText().toString();
			mLoremTextView.setText(originalText + result);
		}
	}

	public final class WeatherRequestJsonListener implements RequestListener<WeatherResult> {

		@Override
		public void onRequestFailure(SpiceException spiceException) {
			Toast.makeText(SampleContentActivity.this, "failure json", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onRequestSuccess(final WeatherResult result) {
			Toast.makeText(SampleContentActivity.this, "success json", Toast.LENGTH_SHORT).show();
			String originalText = mCurrentWeatherJsonTextView.getText().toString();
			mCurrentWeatherJsonTextView.setText(originalText + result.toString());
		}

	}

	public final class WeatherRequestXmlListener implements RequestListener<com.octo.android.robospice.sample.model.xml.Weather> {

		@Override
		public void onRequestFailure(SpiceException spiceException) {
			Toast.makeText(SampleContentActivity.this, "failure xml", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onRequestSuccess(final com.octo.android.robospice.sample.model.xml.Weather result) {
			Toast.makeText(SampleContentActivity.this, "success xml", Toast.LENGTH_SHORT).show();
			String originalText = mCurrentWeatherXmlTextView.getText().toString();
			mCurrentWeatherXmlTextView.setText(originalText + result.toString());
		}
	}

	public final class ImageRequestListener implements RequestListener<InputStream>, RequestProgressListener {

		@Override
		public void onRequestFailure(SpiceException spiceException) {
			Toast.makeText(SampleContentActivity.this, "failure", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onRequestSuccess(final InputStream result) {
			Bitmap bitmap = BitmapFactory.decodeStream(result);
			BitmapDrawable drawable = new BitmapDrawable(bitmap);
			Toast.makeText(SampleContentActivity.this, "success", Toast.LENGTH_SHORT).show();
			mImageTextView.setBackgroundDrawable(drawable);
			mImageTextView.setText("");
		}

		@Override
		public void onRequestProgressUpdate(RequestProgress progress) {
			Ln.d("Binary progress : %s = %d", progress.getStatus(), Math.round(100 * progress.getProgress()));
		}
	}

}
