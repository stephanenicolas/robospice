package com.octo.android.robospice.sample.model.xml;

import org.simpleframework.xml.Element;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "curren_weather")
public class Curren_weather {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "weather_id")
    private Weather weather;

    @Element
    private String humidity;
    @Element
    private String pressure;
    @Element
    private String temp;
    @Element
    private String temp_unit;
    @Element
    private String weather_code;
    @Element
    private String weather_text;

    @Element
    private Wind wind;

    public String getHumidity() {
        return this.humidity;
    }

    public void setHumidity( String humidity ) {
        this.humidity = humidity;
    }

    public String getPressure() {
        return this.pressure;
    }

    public void setPressure( String pressure ) {
        this.pressure = pressure;
    }

    public String getTemp() {
        return this.temp;
    }

    public void setTemp( String temp ) {
        this.temp = temp;
    }

    public String getTemp_unit() {
        return this.temp_unit;
    }

    public void setTemp_unit( String temp_unit ) {
        this.temp_unit = temp_unit;
    }

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
        return "Curren_weather [humidity=" + humidity + ", pressure=" + pressure + ", temp=" + temp + ", temp_unit=" + temp_unit + ", weather_code="
                + weather_code + ", weather_text=" + weather_text + ", wind=" + wind + "]";
    }

}
