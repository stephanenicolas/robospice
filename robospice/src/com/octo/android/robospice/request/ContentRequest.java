package com.octo.android.robospice.request;

import java.util.concurrent.Future;

public abstract class ContentRequest< RESULT > {

    private Class< RESULT > resultType;
    private boolean isCanceled = false;
    private Future< ? > future;

    public ContentRequest( Class< RESULT > clazz ) {
        this.resultType = clazz;
    }

    public abstract RESULT loadDataFromNetwork() throws Exception;

    public Class< RESULT > getResultType() {
        return resultType;
    }

    public void cancel() {
        this.isCanceled = true;
        if ( future != null ) {
            future.cancel( true );
        }
    }

    public boolean isCancelled() {
        return this.isCanceled;
    }

    protected void setFuture( Future< ? > future ) {
        this.future = future;
    }

}