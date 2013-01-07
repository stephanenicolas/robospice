package com.octo.android.robospice.ormlite.test.model;

import com.j256.ormlite.field.DatabaseField;

public class Night {

    @DatabaseField(generatedId = true)
    private int id;
    private String weather_code;
    private String weather_text;
    private Wind wind;

    public String getWeather_code() {
        return this.weather_code;
    }

    public void setWeather_code(String weather_code) {
        this.weather_code = weather_code;
    }

    public String getWeather_text() {
        return this.weather_text;
    }

    public void setWeather_text(String weather_text) {
        this.weather_text = weather_text;
    }

    public Wind getWind() {
        return this.wind;
    }

    public void setWind(Wind wind) {
        this.wind = wind;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result
            + (weather_code == null ? 0 : weather_code.hashCode());
        result = prime * result
            + (weather_text == null ? 0 : weather_text.hashCode());
        result = prime * result + (wind == null ? 0 : wind.hashCode());
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
        Night other = (Night) obj;
        if (id != other.id) {
            return false;
        }
        if (weather_code == null) {
            if (other.weather_code != null) {
                return false;
            }
        } else if (!weather_code.equals(other.weather_code)) {
            return false;
        }
        if (weather_text == null) {
            if (other.weather_text != null) {
                return false;
            }
        } else if (!weather_text.equals(other.weather_text)) {
            return false;
        }
        if (wind == null) {
            if (other.wind != null) {
                return false;
            }
        } else if (!wind.equals(other.wind)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Day [weather_code=" + weather_code + ", weather_text="
            + weather_text + ", wind=" + wind + "]";
    }

}
