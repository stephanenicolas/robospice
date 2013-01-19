package com.octo.android.robospice.springandroid.test.model.json;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Weather {

    private List<CurrenWeather> currenWeather;
    private List<Forecast> forecast;

    public Weather() {
    }

    public List<CurrenWeather> getCurren_weather() {
        return this.currenWeather;
    }

    public void setCurren_weather(List<CurrenWeather> currenWeather) {
        this.currenWeather = currenWeather;
    }

    public List<Forecast> getForecast() {
        return this.forecast;
    }

    public void setForecast(List<Forecast> forecast) {
        this.forecast = forecast;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
            + (currenWeather == null ? 0 : currenWeather.hashCode());
        result = prime * result + (forecast == null ? 0 : forecast.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Weather other = (Weather) obj;
        if (currenWeather == null) {
            if (other.currenWeather != null) {
                return false;
            }
        } else if (!currenWeather.equals(other.currenWeather)) {
            return false;
        }
        if (forecast == null) {
            if (other.forecast != null) {
                return false;
            }
        } else if (!forecast.equals(other.forecast)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Weather [currenWeather=" + currenWeather + ", forecast="
            + forecast + "]";
    }

}
