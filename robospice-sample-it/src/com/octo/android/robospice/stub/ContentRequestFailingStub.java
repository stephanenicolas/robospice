package com.octo.android.robospice.stub;

public final class ContentRequestFailingStub< T > extends ContentRequestStub< T > {

    private long sleepTimeBeforeAnswering = 0;

    public ContentRequestFailingStub( Class< T > clazz ) {
        super( clazz );
    }

    public ContentRequestFailingStub( Class< T > clazz, long sleepTimeBeforeAnswering ) {
        super( clazz );
        this.sleepTimeBeforeAnswering = sleepTimeBeforeAnswering;
    }

    @Override
    public T loadDataFromNetwork() throws Exception {
        if ( sleepTimeBeforeAnswering != 0 ) {
            Thread.sleep( sleepTimeBeforeAnswering );
        }
        isLoadDataFromNetworkCalled = true;
        throw new Exception();
    }

}