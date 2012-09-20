package com.octo.android.robospice.sample.model;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Weather {
	private List<Curren_weather> curren_weather;
	private List<Forecast> forecast;

	public List<Curren_weather> getCurren_weather() {
		return this.curren_weather;
	}

	public void setCurren_weather(List<Curren_weather> curren_weather) {
		this.curren_weather = curren_weather;
	}

	public List<Forecast> getForecast() {
		return this.forecast;
	}

	public void setForecast(List<Forecast> forecast) {
		this.forecast = forecast;
	}

	@Override
	public String toString() {
		return "Weather [curren_weather=" + curren_weather + ", forecast=" + forecast + "]";
	}

}
