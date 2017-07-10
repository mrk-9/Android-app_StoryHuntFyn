package dk.kultur.historiejagtenfyn.data.models;

import android.content.Context;

import dk.kultur.historiejagtenfyn.data.operations.AbsAsyncOperation;
import dk.kultur.historiejagtenfyn.data.operations.GetInfoDetailsOperation;
import dk.kultur.historiejagtenfyn.data.parse.models.InfoModelHis;

/**
 * Created by Lina on 2014.07.02.
 */
public class InfoDetailsDataModel extends AbsSingleOperationDataModel {

    private final Context context;
    private InfoModelHis languageInfo;

    /**
     *
     * @param context
     */
    public InfoDetailsDataModel(Context context) {
        this.context = context;

    }

    /**
     * Returns languageInfo object with info in it
     * Use fields infoTitle and infoText for displaying
     * @return info text object
     */
    public InfoModelHis getLanguageInfo() {
        return languageInfo;
    }

    @Override
    protected AbsAsyncOperation<?> createAsyncOperation() {
        return new GetInfoDetailsOperation(context);
    }

    @Override
    protected void handleLoadedData(AbsAsyncOperation<?> operation) {
        languageInfo = (InfoModelHis) operation.getResult();
    }
}
