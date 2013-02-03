package com.octo.android.robospice.googlehttpclient.test.model;

import com.google.api.client.util.Key;

public class WeatherResult {
    @Key
    private Weather weather;

    @Key
    private int id = 1;

    public Weather getWeather() {
        return this.weather;
    }

    public void setWeather(Weather weather) {
        this.weather = weather;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result + (weather == null ? 0 : weather.hashCode());
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
        WeatherResult other = (WeatherResult) obj;
        if (id != other.id) {
            return false;
        }
        if (weather == null) {
            if (other.weather != null) {
                return false;
            }
        } else if (!weather.equals(other.weather)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "WeatherResult [weather=" + weather + "]";
    }

}
