package com.octo.android.robospice.sample.retrofit.model;

import java.util.ArrayList;

import com.octo.android.robospice.request.SpiceRequest;

/**
 * This class is needed (and should not be ofuscated) as we need a type to be passed to {@link SpiceRequest}
 * constructors and List<Tweet>.class doesn't compile in java.
 * 
 * @author sni
 * 
 */
public class ListTweet extends ArrayList< Tweet > {

    private static final long serialVersionUID = 1L;

}
