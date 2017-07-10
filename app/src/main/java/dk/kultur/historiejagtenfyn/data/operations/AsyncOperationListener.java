package dk.kultur.historiejagtenfyn.data.operations;

public interface AsyncOperationListener {
	public void onOperationStarted(AbsAsyncOperation<?> operation);
	public void onOperationFinished(AbsAsyncOperation<?> operation);
}
