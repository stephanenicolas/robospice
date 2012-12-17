package com.octo.android.robospice.motivations.model.tweeter.json;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.google.api.client.util.Key;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Tweet {
    @Key
    private String text;

    public String getText() {
        return text;
    }

    public void setText( String text ) {
        this.text = text;
    }
}