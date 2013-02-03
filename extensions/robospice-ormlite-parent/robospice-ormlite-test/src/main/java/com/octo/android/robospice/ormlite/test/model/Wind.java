package com.octo.android.robospice.ormlite.test.model;

import com.j256.ormlite.field.DatabaseField;

public class Wind {

    @DatabaseField(generatedId = true)
    private int id;
    private String dir;
    private String dir_degree;
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

    public void setDir_degree(String dir_degree) {
        this.dir_degree = dir_degree;
    }

    public String getDir_degree() {
        return dir_degree;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (dir == null ? 0 : dir.hashCode());
        result = prime * result
            + (dir_degree == null ? 0 : dir_degree.hashCode());
        result = prime * result + id;
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
        if (dir_degree == null) {
            if (other.dir_degree != null) {
                return false;
            }
        } else if (!dir_degree.equals(other.dir_degree)) {
            return false;
        }
        if (id != other.id) {
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
