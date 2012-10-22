package com.octo.android.asynctest.model.tweeter.xml;

import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(strict = false)
public class Feed {

    @Element
    private String id;
    @Element
    private String title;

    @ElementList(name = "entry", inline = true)
    private List< Entry > listEntry;

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

    public List< Entry > getListEntry() {
        return listEntry;
    }

    public void setListEntry( List< Entry > listEntry ) {
        this.listEntry = listEntry;
    }
}
