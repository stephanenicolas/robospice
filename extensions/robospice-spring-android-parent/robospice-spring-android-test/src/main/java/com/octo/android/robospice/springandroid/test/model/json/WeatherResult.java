package com.octo.android.robospice.springandroid.test.model.json;

public class WeatherResult {
    private Weather weather;

    private int id = 1;

    String string = "a";

    public WeatherResult() {

    }

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
        result = prime * result + (string == null ? 0 : string.hashCode());
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
        if (string == null) {
            if (other.string != null) {
                return false;
            }
        } else if (!string.equals(other.string)) {
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
