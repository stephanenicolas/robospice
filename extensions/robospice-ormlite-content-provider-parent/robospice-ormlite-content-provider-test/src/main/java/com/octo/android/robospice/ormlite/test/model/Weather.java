package com.octo.android.robospice.ormlite.test.model;

import java.util.ArrayList;
import java.util.Collection;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

public class Weather {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private String emptyField;

    @ForeignCollectionField(eager = false)
    private Collection<CurrenWeather> listWeather;
    @ForeignCollectionField(eager = false)
    private Collection<Forecast> listForecast;

    public Weather() {
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Collection<CurrenWeather> getListWeather() {
        return this.listWeather;
    }

    public void setListWeather(Collection<CurrenWeather> currenWeather) {
        this.listWeather = currenWeather;
    }

    public Collection<Forecast> getListForecast() {
        return this.listForecast;
    }

    public void setListForecast(Collection<Forecast> forecast) {
        this.listForecast = forecast;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
            + (emptyField == null ? 0 : emptyField.hashCode());
        result = prime * result + id;
        result = prime * result
            + (listForecast == null ? 0 : listForecast.hashCode());
        result = prime * result
            + (listWeather == null ? 0 : listWeather.hashCode());
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
        if (emptyField == null) {
            if (other.emptyField != null) {
                return false;
            }
        } else if (!emptyField.equals(other.emptyField)) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (listForecast == null) {
            if (other.listForecast != null) {
                return false;
            }
        } else {
            if (other.listForecast == null) {
                return false;
            }

            Collection<Forecast> collectionForecast = new ArrayList<Forecast>(
                listForecast);
            Collection<Forecast> otherCollectionForecast = new ArrayList<Forecast>(
                other.listForecast);
            if (!collectionForecast.equals(otherCollectionForecast)) {
                return false;
            }
        }
        if (listWeather == null) {
            if (other.listWeather != null) {
                return false;
            }
        } else {
            if (other.listWeather == null) {
                return false;
            }

            Collection<CurrenWeather> collectionCurren_Weather = new ArrayList<CurrenWeather>(
                listWeather);
            Collection<CurrenWeather> otherCollectionCurren_Weather = new ArrayList<CurrenWeather>(
                other.listWeather);
            if (!collectionCurren_Weather.equals(otherCollectionCurren_Weather)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "Weather [curren_weather=" + listWeather + ", forecast="
            + listForecast + "]";
    }

}
