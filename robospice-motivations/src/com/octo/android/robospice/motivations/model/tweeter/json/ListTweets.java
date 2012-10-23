package com.octo.android.robospice.motivations.model.tweeter.json;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ListTweets {

    private List< Tweet > results;

    public List< Tweet > getResults() {
        return results;
    }

    public void setResults( List< Tweet > results ) {
        this.results = results;
    }
}