package com.octo.android.robospice.sample.model;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Forecast {
	private String date;
	private List<Day> day;
	private String day_max_temp;
	private List<Day> night;
	private String night_min_temp;
	private String temp_unit;

	public String getDate() {
		return this.date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public List<Day> getDay() {
		return this.day;
	}

	public void setDay(List<Day> day) {
		this.day = day;
	}

	public String getDay_max_temp() {
		return this.day_max_temp;
	}

	public void setDay_max_temp(String day_max_temp) {
		this.day_max_temp = day_max_temp;
	}

	public List<Day> getNight() {
		return this.night;
	}

	public void setNight(List<Day> night) {
		this.night = night;
	}

	public String getNight_min_temp() {
		return this.night_min_temp;
	}

	public void setNight_min_temp(String night_min_temp) {
		this.night_min_temp = night_min_temp;
	}

	public String getTemp_unit() {
		return this.temp_unit;
	}

	public void setTemp_unit(String temp_unit) {
		this.temp_unit = temp_unit;
	}

	@Override
	public String toString() {
		return "Forecast [date=" + date + ", day=" + day + ", day_max_temp=" + day_max_temp + ", night=" + night + ", night_min_temp=" + night_min_temp + ", temp_unit=" + temp_unit + "]";
	}

}
