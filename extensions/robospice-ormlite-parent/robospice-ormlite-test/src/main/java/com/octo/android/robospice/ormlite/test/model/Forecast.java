package com.octo.android.robospice.ormlite.test.model;

import java.util.List;

import com.j256.ormlite.field.DatabaseField;

public class Forecast {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "weather_id")
    private Weather weather;

    private String date;
    private List<Day> listDay;
    private String day_max_temp;
    private List<Night> listNight;
    private String night_min_temp;
    private String temp_unit;

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<Day> getDay() {
        return this.listDay;
    }

    public void setDay(List<Day> day) {
        this.listDay = day;
    }

    public String getDay_max_temp() {
        return this.day_max_temp;
    }

    public void setDay_max_temp(String day_max_temp) {
        this.day_max_temp = day_max_temp;
    }

    public List<Night> getNight() {
        return this.listNight;
    }

    public void setNight(List<Night> night) {
        this.listNight = night;
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
        result = prime * result
            + (day_max_temp == null ? 0 : day_max_temp.hashCode());
        result = prime * result + id;
        result = prime * result + (listDay == null ? 0 : listDay.hashCode());
        result = prime * result
            + (listNight == null ? 0 : listNight.hashCode());
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
        if (day_max_temp == null) {
            if (other.day_max_temp != null) {
                return false;
            }
        } else if (!day_max_temp.equals(other.day_max_temp)) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (listDay == null) {
            if (other.listDay != null) {
                return false;
            }
        } else if (!listDay.equals(other.listDay)) {
            return false;
        }
        if (listNight == null) {
            if (other.listNight != null) {
                return false;
            }
        } else if (!listNight.equals(other.listNight)) {
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
        return "Forecast [date=" + date + ", day=" + listDay
            + ", day_max_temp=" + day_max_temp + ", night=" + listNight
            + ", night_min_temp=" + night_min_temp + ", temp_unit=" + temp_unit
            + "]";
    }

}
