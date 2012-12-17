package com.octo.android.robospice.motivations.model.tweeter.xml;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
@Root(strict = false)
public class Entry {

    @DatabaseField(id = true)
    @Element
    private String id;
    @DatabaseField
    @Element
    private String title;
    @DatabaseField
    @Element
    private String content;

    @DatabaseField(foreign = true)
    private Feed feed;

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

    public String getContent() {
        return content;
    }

    public void setContent( String content ) {
        this.content = content;
    }

    public void setFeed( Feed feed ) {
        this.feed = feed;
    }

    public Feed getFeed() {
        return feed;
    }
}
