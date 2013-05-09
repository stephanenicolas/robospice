package com.octo.android.robospice.persistence.ormlite;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class CacheEntry {

    @DatabaseField(id = true)
    private String cacheKey;
    @DatabaseField
    private String resultClassName;
    @DatabaseField
    private long timestamp;

    @DatabaseField
    private String resultIdString;
    @DatabaseField
    private Character resultIdChar;
    @DatabaseField
    private Byte resultIdByte;
    @DatabaseField
    private Short resultIdShort;
    @DatabaseField
    private Integer resultIdInteger;
    @DatabaseField
    private Long resultIdLong;
    @DatabaseField
    private Float resultIdFloat;
    @DatabaseField
    private Double resultIdDouble;

    public CacheEntry() {

    }

    public CacheEntry(String cacheKey, long timestamp, Class<?> resultClass, Object resultId) {
        this.cacheKey = cacheKey;
        this.timestamp = timestamp;
        this.resultClassName = resultClass.getName();
        fillResultId(resultId);
    }

    private void fillResultId(Object id) {

        if (id instanceof String) {
            resultIdString = (String) id;
        } else if (id instanceof Character) {
            resultIdChar = (Character) id;
        } else if (id instanceof Byte) {
            resultIdByte = (Byte) id;
        } else if (id instanceof Short) {
            resultIdShort = (Short) id;
        } else if (id instanceof Integer) {
            resultIdInteger = (Integer) id;
        } else if (id instanceof Long) {
            resultIdLong = (Long) id;
        } else if (id instanceof Float) {
            resultIdFloat = (Float) id;
        } else if (id instanceof Double) {
            resultIdDouble = (Double) id;
        }
    }

    public String getCacheKey() {
        return cacheKey;
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setResultClassName(String resultClassName) {
        this.resultClassName = resultClassName;
    }

    public String getResultClassName() {
        return resultClassName;
    }

    public Object getResultId() {

        if (resultIdByte != null) {
            return resultIdByte;
        } else if (resultIdChar != null) {
            return resultIdChar;
        } else if (resultIdDouble != null) {
            return resultIdDouble;
        } else if (resultIdFloat != null) {
            return resultIdFloat;
        } else if (resultIdInteger != null) {
            return resultIdInteger;
        } else if (resultIdLong != null) {
            return resultIdLong;
        } else if (resultIdShort != null) {
            return resultIdShort;
        } else if (resultIdString != null) {
            return resultIdString;
        }

        throw new RuntimeException("Cache entry has no result id.");

    }
}
