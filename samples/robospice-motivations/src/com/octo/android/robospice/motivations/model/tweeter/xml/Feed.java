package com.octo.android.robospice.motivations.model.tweeter.xml;

import java.util.ArrayList;
import java.util.Collection;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
@Root(strict = false)
public class Feed {

    @DatabaseField(id = true)
    @Element
    private String id;
    @DatabaseField
    @Element
    private String title;

    @ForeignCollectionField
    @ElementList(name = "entry", inline = true)
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
