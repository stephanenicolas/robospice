package com.octo.android.robospice.sample.model.xml;

import org.simpleframework.xml.Element;

import com.j256.ormlite.field.DatabaseField;

public class Day {

    @DatabaseField(generatedId = true)
    private int id;

    @Element
    private String weather_code;
    @Element
    private String weather_text;

    @Element
    private Wind wind;

    public String getWeather_code() {
        return this.weather_code;
    }

    public void setWeather_code( String weather_code ) {
        this.weather_code = weather_code;
    }

    public String getWeather_text() {
        return this.weather_text;
    }

    public void setWeather_text( String weather_text ) {
        this.weather_text = weather_text;
    }

    public Wind getWind() {
        return this.wind;
    }

    public void setWind( Wind wind ) {
        this.wind = wind;
    }

    @Override
    public String toString() {
        return "Day [weather_code=" + weather_code + ", weather_text=" + weather_text + ", wind=" + wind + "]";
    }

}
