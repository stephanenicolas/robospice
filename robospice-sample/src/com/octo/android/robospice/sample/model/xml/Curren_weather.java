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

    @Element(required = false)
    @DatabaseField
    private String humidity;
    @Element(required = false)
    @DatabaseField
    private String pressure;
    @Element(required = false)
    @DatabaseField
    private String temp;
    @Element(required = false)
    @DatabaseField
    private String temp_unit;
    @Element(required = false)
    @DatabaseField
    private String weather_code;
    @Element(required = false)
    @DatabaseField
    private String weather_text;

    @Element(required = false)
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( temp == null ? 0 : temp.hashCode() );
        result = prime * result + ( temp_unit == null ? 0 : temp_unit.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        Curren_weather other = (Curren_weather) obj;
        if ( temp == null ) {
            if ( other.temp != null ) {
                return false;
            }
        } else if ( !temp.equals( other.temp ) ) {
            return false;
        }
        if ( temp_unit == null ) {
            if ( other.temp_unit != null ) {
                return false;
            }
        } else if ( !temp_unit.equals( other.temp_unit ) ) {
            return false;
        }
        return true;
    }

}
