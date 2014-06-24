package com.octo.android.robospice.request.okhttp.simple;

import java.io.IOException;
import java.net.MalformedURLException;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

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
            Request request = new Request.Builder().url(url).build();
            Response response = getOkHttpClient().newCall(request).execute();
            return response.body().string();
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
