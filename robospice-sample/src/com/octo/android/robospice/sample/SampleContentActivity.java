package com.octo.android.robospice.sample;

import java.io.InputStream;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.octo.android.robospice.exception.ContentManagerException;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.request.RequestListener;
import com.octo.android.robospice.request.simple.SimpleTextRequest;
import com.octo.android.robospice.request.simple.SmallBinaryRequest;
import com.octo.android.robospice.sample.model.WeatherResult;
import com.octo.android.robospice.sample.request.WeatherRequest;

@ContentView(R.layout.main)
public class SampleContentActivity extends BaseSampleContentActivity {

	// ============================================================================================
	// ATTRIBUTES
	// ============================================================================================

	@InjectView(R.id.textview_hello_cnil)
	private TextView mLoremTextView;
	@InjectView(R.id.textview_hello_credit_status)
	private TextView mCurrentWeatherTextView;
	@InjectView(R.id.textview_hello_image)
	private TextView mImageTextView;

	SimpleTextRequest loremRequest;
	SmallBinaryRequest imageRequest;
	WeatherRequest weatherRequest;

	// ============================================================================================
	// ACITVITY LIFE CYCLE
	// ============================================================================================

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Initializes the logging
		// Log a message (only on dev platform)
		Log.i(getClass().getName(), "onCreate");

		loremRequest = new SimpleTextRequest("http://www.loremipsum.de/downloads/original.txt");
		weatherRequest = new WeatherRequest("75000");
		imageRequest = new SmallBinaryRequest("http://earthobservatory.nasa.gov/blogs/elegantfigures/files/2011/10/globe_west_2048.jpg");
		// imageRequest = new SimpleImageRequest("http://cdn1.iconfinder.com/data/icons/softicons/PNG/Programming.png");
	}

	@Override
	protected void onStart() {
		super.onStart();
		getContentManager().execute(loremRequest, "lorem.txt", DurationInMillis.ONE_DAY, new LoremRequestListener());
		getContentManager().execute(weatherRequest, "75000.weather", DurationInMillis.ONE_DAY, new WeatherRequestListener());
		getContentManager().execute(imageRequest, "logo", DurationInMillis.ONE_DAY, new ImageRequestListener());
	}

	// ============================================================================================
	// INNER CLASSES
	// ============================================================================================

	public final class LoremRequestListener implements RequestListener<String> {

		@Override
		public void onRequestFailure(ContentManagerException contentManagerException) {
			Toast.makeText(SampleContentActivity.this, "failure", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onRequestSuccess(final String result) {
			Toast.makeText(SampleContentActivity.this, "success", Toast.LENGTH_SHORT).show();
			String originalText = mLoremTextView.getText().toString();
			mLoremTextView.setText(originalText + result);
		}
	}

	public final class WeatherRequestListener implements RequestListener<WeatherResult> {

		@Override
		public void onRequestFailure(ContentManagerException contentManagerException) {
			Toast.makeText(SampleContentActivity.this, "failure", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onRequestSuccess(final WeatherResult result) {
			Toast.makeText(SampleContentActivity.this, "success", Toast.LENGTH_SHORT).show();
			String originalText = mCurrentWeatherTextView.getText().toString();
			mCurrentWeatherTextView.setText(originalText + result.toString());
		}
	}

	public final class ImageRequestListener implements RequestListener<InputStream> {

		@Override
		public void onRequestFailure(ContentManagerException contentManagerException) {
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
	}

}
