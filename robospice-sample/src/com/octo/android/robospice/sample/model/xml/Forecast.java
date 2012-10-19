package com.octo.android.robospice.sample.model.xml;

import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import com.j256.ormlite.field.DatabaseField;

public class Forecast {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "weather_id")
    private Weather weather;

    @Element
    private String date;
    @ElementList(inline = true)
    private List< Day > listDay;
    @Element
    private String day_max_temp;
    @ElementList(inline = true)
    private List< Night > listNight;
    @Element
    private String night_min_temp;
    @Element
    private String temp_unit;

    public String getDate() {
        return this.date;
    }

    public void setDate( String date ) {
        this.date = date;
    }

    public List< Day > getDay() {
        return this.listDay;
    }

    public void setDay( List< Day > day ) {
        this.listDay = day;
    }

    public String getDay_max_temp() {
        return this.day_max_temp;
    }

    public void setDay_max_temp( String day_max_temp ) {
        this.day_max_temp = day_max_temp;
    }

    public List< Night > getNight() {
        return this.listNight;
    }

    public void setNight( List< Night > night ) {
        this.listNight = night;
    }

    public String getNight_min_temp() {
        return this.night_min_temp;
    }

    public void setNight_min_temp( String night_min_temp ) {
        this.night_min_temp = night_min_temp;
    }

    public String getTemp_unit() {
        return this.temp_unit;
    }

    public void setTemp_unit( String temp_unit ) {
        this.temp_unit = temp_unit;
    }

    @Override
    public String toString() {
        return "Forecast [date=" + date + ", day=" + listDay + ", day_max_temp=" + day_max_temp + ", night=" + listNight + ", night_min_temp=" + night_min_temp
                + ", temp_unit=" + temp_unit + "]";
    }

}
