package com.octo.android.robospice.springandroid.test.model.xml;

import java.util.ArrayList;
import java.util.Collection;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root
public class Weather {

    private int id;

    private String emptyField;

    @ElementList(inline = true, required = false)
    private Collection<Curren_weather> listWeather;
    @ElementList(inline = true, required = false)
    private Collection<Forecast> listForecast;

    public Weather() {
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Collection<Curren_weather> getListWeather() {
        return this.listWeather;
    }

    public void setListWeather(Collection<Curren_weather> curren_weather) {
        this.listWeather = curren_weather;
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

            Collection<Curren_weather> collectionCurren_Weather = new ArrayList<Curren_weather>(
                listWeather);
            Collection<Curren_weather> otherCollectionCurren_Weather = new ArrayList<Curren_weather>(
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
