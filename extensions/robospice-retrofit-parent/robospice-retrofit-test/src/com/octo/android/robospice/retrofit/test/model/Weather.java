package com.octo.android.robospice.retrofit.test.model;

import java.util.List;

public class Weather {

    private List< Curren_weather > curren_weather;
    private List< Forecast > forecast;

    public Weather() {
    }

    public List< Curren_weather > getCurren_weather() {
        return this.curren_weather;
    }

    public void setCurren_weather( List< Curren_weather > curren_weather ) {
        this.curren_weather = curren_weather;
    }

    public List< Forecast > getForecast() {
        return this.forecast;
    }

    public void setForecast( List< Forecast > forecast ) {
        this.forecast = forecast;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( curren_weather == null ? 0 : curren_weather.hashCode() );
        result = prime * result + ( forecast == null ? 0 : forecast.hashCode() );
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
        Weather other = (Weather) obj;
        if ( curren_weather == null ) {
            if ( other.curren_weather != null ) {
                return false;
            }
        } else if ( !curren_weather.equals( other.curren_weather ) ) {
            return false;
        }
        if ( forecast == null ) {
            if ( other.forecast != null ) {
                return false;
            }
        } else if ( !forecast.equals( other.forecast ) ) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Weather [curren_weather=" + curren_weather + ", forecast=" + forecast + "]";
    }

}
