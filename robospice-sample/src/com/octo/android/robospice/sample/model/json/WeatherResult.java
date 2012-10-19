package com.octo.android.robospice.sample.model.json;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "weather_result")
public class WeatherResult {
    private Weather weather;

    @DatabaseField(generatedId = true)
    private int id = 1;

    @DatabaseField(index = true)
    String string = "a";

    public WeatherResult() {

    }

    public Weather getWeather() {
        return this.weather;
    }

    public void setWeather( Weather weather ) {
        this.weather = weather;
    }

    @Override
    public String toString() {
        return "WeatherResult [weather=" + weather + "]";
    }

}
