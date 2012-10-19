package com.octo.android.robospice.sample.model.xml;

import org.simpleframework.xml.Element;

import com.j256.ormlite.field.DatabaseField;

public class Wind {

    @DatabaseField(generatedId = true)
    private int id;

    @Element
    private String dir;
    @Element(required = false)
    private String dir_degree;
    @Element
    private String speed;
    @Element
    private String wind_unit;

    public String getDir() {
        return this.dir;
    }

    public void setDir( String dir ) {
        this.dir = dir;
    }

    public String getSpeed() {
        return this.speed;
    }

    public void setSpeed( String speed ) {
        this.speed = speed;
    }

    public String getWind_unit() {
        return this.wind_unit;
    }

    public void setWind_unit( String wind_unit ) {
        this.wind_unit = wind_unit;
    }

    public void setDir_degree( String dir_degree ) {
        this.dir_degree = dir_degree;
    }

    public String getDir_degree() {
        return dir_degree;
    }

    @Override
    public String toString() {
        return "Wind [dir=" + dir + ", speed=" + speed + ", wind_unit=" + wind_unit + "]";
    }

}
