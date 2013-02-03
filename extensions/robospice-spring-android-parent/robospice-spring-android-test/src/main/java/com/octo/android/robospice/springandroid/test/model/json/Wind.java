package com.octo.android.robospice.springandroid.test.model.json;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Wind {
    private String dir;
    private String speed;
    private String wind_unit;

    public String getDir() {
        return this.dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getSpeed() {
        return this.speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getWind_unit() {
        return this.wind_unit;
    }

    public void setWind_unit(String wind_unit) {
        this.wind_unit = wind_unit;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (dir == null ? 0 : dir.hashCode());
        result = prime * result + (speed == null ? 0 : speed.hashCode());
        result = prime * result
            + (wind_unit == null ? 0 : wind_unit.hashCode());
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
        Wind other = (Wind) obj;
        if (dir == null) {
            if (other.dir != null) {
                return false;
            }
        } else if (!dir.equals(other.dir)) {
            return false;
        }
        if (speed == null) {
            if (other.speed != null) {
                return false;
            }
        } else if (!speed.equals(other.speed)) {
            return false;
        }
        if (wind_unit == null) {
            if (other.wind_unit != null) {
                return false;
            }
        } else if (!wind_unit.equals(other.wind_unit)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Wind [dir=" + dir + ", speed=" + speed + ", wind_unit="
            + wind_unit + "]";
    }

}
