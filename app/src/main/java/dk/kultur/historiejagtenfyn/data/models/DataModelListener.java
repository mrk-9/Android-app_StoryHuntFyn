package dk.kultur.historiejagtenfyn.data.models;

public interface DataModelListener {
	
	/**
	 * data model started load its data on background task
	 * Implement when you need to show indication of loading data
	 */
	void onLoadStarted();
	
	/**
	 * data model finished load its data on background task
	 * Implement when you need to show indication of loading data
	 */
	void onLoadFinished();
	

}
