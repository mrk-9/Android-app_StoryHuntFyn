package dk.kultur.historiejagtenfyn.data.models;

public interface IDataModel<T extends DataModelListener> {

	/**
	 * registers DataModelListener
	 * @param listener
	 */
	public void registerDataModelListener(T listener);
	
	/**
	 * unregisters DataModelListener
	 */
	public void unregisterDataModelListener(T listener);
	
	/**
	 * loads data to the model
	 * @return true if model is already loaded
	 */
	public boolean load();

	/**
	 * reloads data of the model. Old data is destroyed and new set is loaded.
	 */
	public void reload();
	
	/**
	 * releases models resources.
	 */
	public void unload();
	
	/**
	 * indicates if model is currently loading its data
	 * @return true if model is loading data, otherwise false
	 */
	public boolean isLoading();
	
	/**
	 * indicates if model is already loaded its data
	 * 
	 * @return true if model is loaded its data, otherwise false
	 */
	public boolean isLoaded();
}
