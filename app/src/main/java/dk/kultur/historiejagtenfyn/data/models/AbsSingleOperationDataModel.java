package dk.kultur.historiejagtenfyn.data.models;

import android.os.AsyncTask.Status;

import dk.kultur.historiejagtenfyn.data.operations.AbsAsyncOperation;
import dk.kultur.historiejagtenfyn.data.operations.AsyncOperationListener;

public abstract class AbsSingleOperationDataModel implements IDataModel<DataModelListener>, AsyncOperationListener {

	protected DataModelListener mListener = null;
	
	protected AbsAsyncOperation<?> mOperation = null;
	
	/**
	 * 
	 */
	@Override
	public void registerDataModelListener(DataModelListener listener) {
		mListener = listener;
	}
	
	/**
	 * 
	 */
	@Override
	public void unregisterDataModelListener(DataModelListener listener) {
		if (mListener == listener) {
			mListener = null;
		}
	}
	
	/**
	 * 
	 */
	@Override
	public boolean load() {
		if (mOperation == null) {
			mOperation = createAsyncOperation();
			mOperation.addListener(this);
			mOperation.executeOperation();
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * 
	 */
	@Override
	public void reload() {
		if (mOperation != null) {
			mOperation.cancelOperation();
			mOperation = null;
		}
		load();
	}
	
	/**
	 * 
	 */
	@Override
	public void unload() {
		if (mOperation != null) {
			mOperation.cancelOperation();
			mOperation = null;
		}
	}
	
	/**
	 * 
	 */
	@Override
	public boolean isLoading() {
		return (mOperation != null && !mOperation.getStatus().equals(Status.FINISHED));
	}
	
	/**
	 * Checks if model has loaded its data
	 */
	@Override
	public boolean isLoaded() {
		return (mOperation != null && mOperation.getStatus().equals(Status.FINISHED));
	}
	
	/**
	 * 
	 */
	@Override
	public void onOperationStarted(AbsAsyncOperation<?> operation) {
		if (mListener != null) {
			mListener.onLoadStarted();
		}
	}
	
	/**
	 * 
	 */
	@Override
	public void onOperationFinished(AbsAsyncOperation<?> operation) {
		handleLoadedData(operation);
		if (mListener != null) {
			mListener.onLoadFinished();
		}
	}
	
	/**
	 * Instantiates AsyncOperation
	 * @return
	 */
	protected abstract AbsAsyncOperation<?> createAsyncOperation();
	
	/**
	 * handles AsyncOperation data
	 * @param operation
	 */
	protected abstract void handleLoadedData(AbsAsyncOperation<?> operation);	
	

}
