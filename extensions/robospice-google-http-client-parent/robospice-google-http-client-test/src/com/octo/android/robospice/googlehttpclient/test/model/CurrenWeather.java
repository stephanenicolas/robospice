package com.octo.android.robospice.googlehttpclient.test.model;

import java.util.List;

import com.google.api.client.util.Key;

public class CurrenWeather {
    @Key
    private String humidity;
    @Key
    private String pressure;
    @Key
    private String temp;
    @Key
    private String temp_unit;
    @Key
    private String weather_code;
    @Key
    private String weather_text;
    @Key
    private List<Wind> wind;

    public String getHumidity() {
        return this.humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getPressure() {
        return this.pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public String getTemp() {
        return this.temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getTemp_unit() {
        return this.temp_unit;
    }

    public void setTemp_unit(String temp_unit) {
        this.temp_unit = temp_unit;
    }

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

    public List<Wind> getWind() {
        return this.wind;
    }

    public void setWind(List<Wind> wind) {
        this.wind = wind;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (humidity == null ? 0 : humidity.hashCode());
        result = prime * result + (pressure == null ? 0 : pressure.hashCode());
        result = prime * result + (temp == null ? 0 : temp.hashCode());
        result = prime * result
            + (temp_unit == null ? 0 : temp_unit.hashCode());
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
        CurrenWeather other = (CurrenWeather) obj;
        if (humidity == null) {
            if (other.humidity != null) {
                return false;
            }
        } else if (!humidity.equals(other.humidity)) {
            return false;
        }
        if (pressure == null) {
            if (other.pressure != null) {
                return false;
            }
        } else if (!pressure.equals(other.pressure)) {
            return false;
        }
        if (temp == null) {
            if (other.temp != null) {
                return false;
            }
        } else if (!temp.equals(other.temp)) {
            return false;
        }
        if (temp_unit == null) {
            if (other.temp_unit != null) {
                return false;
            }
        } else if (!temp_unit.equals(other.temp_unit)) {
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
        return "CurrenWeather [humidity=" + humidity + ", pressure="
            + pressure + ", temp=" + temp + ", temp_unit=" + temp_unit
            + ", weather_code=" + weather_code + ", weather_text="
            + weather_text + ", wind=" + wind + "]";
    }

}
