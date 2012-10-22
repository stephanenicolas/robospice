package com.octo.android.asynctest.model.tweeter.json;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Tweet {
    private String text;

    public String getText() {
        return text;
    }

    public void setText( String text ) {
        this.text = text;
    }
}