package com.octo.android.robospice.sample.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Wind {
	private String dir;
	private String speed;
	private String wind_unit;

	public String getDir() {
		return this.dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

	public String getSpeed() {
		return this.speed;
	}

	public void setSpeed(String speed) {
		this.speed = speed;
	}

	public String getWind_unit() {
		return this.wind_unit;
	}

	public void setWind_unit(String wind_unit) {
		this.wind_unit = wind_unit;
	}

	@Override
	public String toString() {
		return "Wind [dir=" + dir + ", speed=" + speed + ", wind_unit=" + wind_unit + "]";
	}

}
