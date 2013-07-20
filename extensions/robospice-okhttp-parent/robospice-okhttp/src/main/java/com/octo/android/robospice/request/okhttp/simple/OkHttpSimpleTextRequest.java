package com.octo.android.robospice.request.okhttp.simple;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import roboguice.util.temp.Ln;

import com.octo.android.robospice.request.okhttp.OkHttpSpiceRequest;

public class OkHttpSimpleTextRequest extends OkHttpSpiceRequest<String> {

    private final String url;

    public OkHttpSimpleTextRequest(final String url) {
        super(String.class);
        this.url = url;
    }

    // can't use activity here or any non serializable field
    // will be invoked in remote service
    @Override
    public String loadDataFromNetwork() throws Exception {
        try {
            Ln.d("Call web service " + url);
            HttpURLConnection connection = getOkHttpClient().open(new URL(url));
            return IOUtils.toString(connection.getInputStream());
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
