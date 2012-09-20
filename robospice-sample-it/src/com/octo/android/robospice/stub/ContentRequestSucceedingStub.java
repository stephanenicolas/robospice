package com.octo.android.robospice.stub;

public final class ContentRequestSucceedingStub< T > extends ContentRequestStub< T > {
    private T returnedData;
    private long waitBeforeExecution = 0;

    public ContentRequestSucceedingStub( Class< T > clazz, T returnedData ) {
    	this( clazz, returnedData, 0 );
    }
    
    public ContentRequestSucceedingStub( Class< T > clazz, T returnedData, long waitBeforeExecution ) {
    	super( clazz );
    	this.returnedData = returnedData;
    	this.waitBeforeExecution = waitBeforeExecution;
    }

    @Override
    public T loadDataFromNetwork() throws Exception {
    	
    	if( waitBeforeExecution != 0 ) {
    		try {
    		Thread.sleep( waitBeforeExecution );
    		} catch( Exception ex ) {
    			ex.printStackTrace();
    		}
    	}
        isLoadDataFromNetworkCalled = true;
        return returnedData;
    }
}