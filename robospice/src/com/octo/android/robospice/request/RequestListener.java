package com.octo.android.robospice.request;

import com.octo.android.robospice.exception.ContentManagerException;

/**
 * Interface used to deal with request result. Two cases : request failed or succeed.
 * 
 * Implement this interface to retrieve request result or to manage error
 * 
 * @author jva
 * 
 * @param <RESULT>
 */
public interface RequestListener< RESULT > {

    void onRequestFailure( ContentManagerException contentManagerException );

    void onRequestSuccess( RESULT result );
}
