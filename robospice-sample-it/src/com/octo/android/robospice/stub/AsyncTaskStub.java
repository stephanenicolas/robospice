package com.octo.android.robospice.stub;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import android.os.AsyncTask;
import android.os.Looper;

public class AsyncTaskStub<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
	protected boolean isLoadDataFromNetworkCalled = false;

	private ReentrantLock lock = new ReentrantLock();
	private Condition requestFinishedCondition = lock.newCondition();

	private Result returnedData;

	private boolean isExecutedInUIThread;

	private boolean isPostExecuteCalled = false;

	public AsyncTaskStub(Result returnedData) {
		this.returnedData = returnedData;
	}

	@Override
	protected Result doInBackground(Params... params) {
		isLoadDataFromNetworkCalled = true;
		return returnedData;
	}

	@Override
	protected void onPostExecute(Result result) {
		this.isPostExecuteCalled = true;
		checkIsExectuedInUIThread();

		lock.lock();
		try {
			requestFinishedCondition.signal();
		} finally {
			lock.unlock();
		}
		super.onPostExecute(result);
	}

	public boolean isLoadDataFromNetworkCalled() {
		return isLoadDataFromNetworkCalled;
	}

	public void await(long millisecond) throws InterruptedException {
		lock.lock();
		try {
			requestFinishedCondition.await(millisecond, TimeUnit.MILLISECONDS);
		} finally {
			lock.unlock();
		}
	}

	protected void checkIsExectuedInUIThread() {
		if (Looper.myLooper() != null && Looper.myLooper() == Looper.getMainLooper()) {
			isExecutedInUIThread = true;
		}
	}

	public boolean isExecutedInUIThread() {
		return isExecutedInUIThread;
	}

	public boolean isPostExecuteCalled() {
		return isPostExecuteCalled;
	}
}