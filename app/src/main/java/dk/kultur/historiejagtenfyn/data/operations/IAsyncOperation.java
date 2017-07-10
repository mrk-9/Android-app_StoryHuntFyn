package dk.kultur.historiejagtenfyn.data.operations;

public interface IAsyncOperation {

	public void cancelOperation();
	public void executeOperation();
	public void addListener(AsyncOperationListener listener);
	public void removeListener(AsyncOperationListener listener);

}
