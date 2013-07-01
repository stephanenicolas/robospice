package com.octo.android.robospice.request.simple;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;

import roboguice.util.temp.Ln;

import com.octo.android.robospice.request.SpiceRequest;

public class SimpleTextRequest extends SpiceRequest<String> {

    private final String url;

    public SimpleTextRequest(final String url) {
        super(String.class);
        this.url = url;
    }

    // can't use activity here or any non serializable field
    // will be invoked in remote service
    @Override
    public String loadDataFromNetwork() throws Exception {
        try {
            Ln.d("Call web service " + url);
            return IOUtils.toString(new InputStreamReader(new URL(url).openStream(), CharEncoding.UTF_8));
        } catch (final MalformedURLException e) {
            Ln.e(e, "Unable to create URL");
            throw e;
        } catch (final IOException e) {
            Ln.e(e, "Unable to download content");
            throw e;
        }
    }

    // can't use activity here or any non serializable field
    // will be invoked in remote service
    protected final String getUrl() {
        return this.url;
    }

}
