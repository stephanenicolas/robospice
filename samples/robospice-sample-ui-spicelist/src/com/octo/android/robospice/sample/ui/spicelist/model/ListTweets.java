package com.octo.android.robospice.sample.ui.spicelist.model;

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