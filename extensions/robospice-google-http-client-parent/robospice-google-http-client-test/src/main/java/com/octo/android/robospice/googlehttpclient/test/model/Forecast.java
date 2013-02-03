package com.octo.android.robospice.googlehttpclient.test.model;

import java.util.List;

import com.google.api.client.util.Key;

public class Forecast {
    @Key
    private String date;
    @Key
    private List<Day> day;
    @Key
    private String day_max_temp;
    @Key
    private List<Day> night;
    @Key
    private String night_min_temp;
    @Key
    private String temp_unit;

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<Day> getDay() {
        return this.day;
    }

    public void setDay(List<Day> day) {
        this.day = day;
    }

    public String getDay_max_temp() {
        return this.day_max_temp;
    }

    public void setDay_max_temp(String day_max_temp) {
        this.day_max_temp = day_max_temp;
    }

    public List<Day> getNight() {
        return this.night;
    }

    public void setNight(List<Day> night) {
        this.night = night;
    }

    public String getNight_min_temp() {
        return this.night_min_temp;
    }

    public void setNight_min_temp(String night_min_temp) {
        this.night_min_temp = night_min_temp;
    }

    public String getTemp_unit() {
        return this.temp_unit;
    }

    public void setTemp_unit(String temp_unit) {
        this.temp_unit = temp_unit;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (date == null ? 0 : date.hashCode());
        result = prime * result + (day == null ? 0 : day.hashCode());
        result = prime * result
            + (day_max_temp == null ? 0 : day_max_temp.hashCode());
        result = prime * result + (night == null ? 0 : night.hashCode());
        result = prime * result
            + (night_min_temp == null ? 0 : night_min_temp.hashCode());
        result = prime * result
            + (temp_unit == null ? 0 : temp_unit.hashCode());
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
        Forecast other = (Forecast) obj;
        if (date == null) {
            if (other.date != null) {
                return false;
            }
        } else if (!date.equals(other.date)) {
            return false;
        }
        if (day == null) {
            if (other.day != null) {
                return false;
            }
        } else if (!day.equals(other.day)) {
            return false;
        }
        if (day_max_temp == null) {
            if (other.day_max_temp != null) {
                return false;
            }
        } else if (!day_max_temp.equals(other.day_max_temp)) {
            return false;
        }
        if (night == null) {
            if (other.night != null) {
                return false;
            }
        } else if (!night.equals(other.night)) {
            return false;
        }
        if (night_min_temp == null) {
            if (other.night_min_temp != null) {
                return false;
            }
        } else if (!night_min_temp.equals(other.night_min_temp)) {
            return false;
        }
        if (temp_unit == null) {
            if (other.temp_unit != null) {
                return false;
            }
        } else if (!temp_unit.equals(other.temp_unit)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Forecast [date=" + date + ", day=" + day + ", day_max_temp="
            + day_max_temp + ", night=" + night + ", night_min_temp="
            + night_min_temp + ", temp_unit=" + temp_unit + "]";
    }

}
