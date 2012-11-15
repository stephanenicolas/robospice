package com.octo.android.robospice.sample.model.xml;

import java.util.Collection;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

@Root
public class Weather {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private String emptyField;

    @ForeignCollectionField(eager = false)
    @ElementList(inline = true)
    private Collection< Curren_weather > listWeather;
    @ForeignCollectionField(eager = false)
    @ElementList(inline = true, required = false)
    private Collection< Forecast > listForecast;

    public Weather() {
    }

    public Collection< Curren_weather > getCurren_weather() {
        return this.listWeather;
    }

    public void setCurren_weather( Collection< Curren_weather > curren_weather ) {
        this.listWeather = curren_weather;
    }

    public Collection< Forecast > getForecast() {
        return this.listForecast;
    }

    public void setForecast( Collection< Forecast > forecast ) {
        this.listForecast = forecast;
    }

    @Override
    public String toString() {
        return "Weather [curren_weather=" + listWeather + ", forecast=" + listForecast + "]";
    }

}
