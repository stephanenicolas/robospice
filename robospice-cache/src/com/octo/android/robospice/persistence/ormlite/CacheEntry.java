package com.octo.android.robospice.persistence.ormlite;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class CacheEntry {
	
	@DatabaseField(id=true)
	private String cacheKey;
	@DatabaseField
	private long timestamp;
	
	public CacheEntry() {

	}
	
	public CacheEntry(String cacheKey, long timestamp) {
		this.cacheKey = cacheKey;
		this.timestamp = timestamp;
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
}
