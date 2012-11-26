package com.octo.android.robospice.sample.model.ormlite;

import java.util.ArrayList;
import java.util.Collection;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Feed {

    @DatabaseField(id = true)
    private String id;
    @DatabaseField
    private String title;

    @ForeignCollectionField
    private Collection< Entry > listEntry = new ArrayList< Entry >();

    public String getId() {
        return id;
    }

    public void setId( String id ) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle( String title ) {
        this.title = title;
    }

    public Collection< Entry > getListEntry() {
        return listEntry;
    }

    public void setListEntry( Collection< Entry > listEntry ) {
        this.listEntry = listEntry;
    }
}
