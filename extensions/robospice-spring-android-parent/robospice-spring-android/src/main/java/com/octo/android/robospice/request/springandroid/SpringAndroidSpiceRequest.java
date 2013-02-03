package com.octo.android.robospice.request.springandroid;

import org.springframework.web.client.RestTemplate;

import roboguice.util.temp.Ln;

import com.octo.android.robospice.request.SpiceRequest;

public abstract class SpringAndroidSpiceRequest<RESULT> extends
    SpiceRequest<RESULT> {

    private RestTemplate restTemplate;

    public SpringAndroidSpiceRequest(Class<RESULT> clazz) {
        super(clazz);
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    /**
     * This method doesn't really work within the Spring Android module : once the request is 
     * loading data from network, there is no way to interrupt it. This is weakness of the spring android framework,
     * and seems to come from even deeper. The IO operations on which it relies don't support the interrupt flag
     * properly.
     * Nevertheless, there are still some opportunities to cancel the request, basically during cache operations.
     */
    public void cancel() {
        super.cancel();
        Ln.w(SpringAndroidSpiceRequest.class.getName(),
            "Cancel can't be invoked directly on "
                + SpringAndroidSpiceRequest.class.getName()
                + " requests. You must call SpiceManager.cancelAllRequests().");
    }
}
