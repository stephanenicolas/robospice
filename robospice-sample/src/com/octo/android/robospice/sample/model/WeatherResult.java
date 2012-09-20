package com.octo.android.robospice.sample.model;

public class WeatherResult {
	private Weather weather;

	public Weather getWeather() {
		return this.weather;
	}

	public void setWeather(Weather weather) {
		this.weather = weather;
	}

	@Override
	public String toString() {
		return "WeatherResult [weather=" + weather + "]";
	}

}
