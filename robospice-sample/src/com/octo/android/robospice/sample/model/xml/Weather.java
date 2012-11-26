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
    @ElementList(inline = true, required = false)
    private Collection< Curren_weather > listWeather;
    @ForeignCollectionField(eager = false)
    @ElementList(inline = true, required = false)
    private Collection< Forecast > listForecast;

    public Weather() {
    }

    public void setId( int id ) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Collection< Curren_weather > getListWeather() {
        return this.listWeather;
    }

    public void setListWeather( Collection< Curren_weather > curren_weather ) {
        this.listWeather = curren_weather;
    }

    public Collection< Forecast > getListForecast() {
        return this.listForecast;
    }

    public void setListForecast( Collection< Forecast > forecast ) {
        this.listForecast = forecast;
    }

    @Override
    public String toString() {
        return "Weather [curren_weather=" + listWeather + ", forecast=" + listForecast + "]";
    }

}
