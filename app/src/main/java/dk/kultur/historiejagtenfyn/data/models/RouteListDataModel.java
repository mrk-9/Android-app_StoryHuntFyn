package dk.kultur.historiejagtenfyn.data.models;

import android.content.Context;

import dk.kultur.historiejagtenfyn.data.parse.models.RouteModelHis;
import java.util.ArrayList;
import java.util.List;

import dk.kultur.historiejagtenfyn.data.operations.AbsAsyncOperation;
import dk.kultur.historiejagtenfyn.data.operations.GetRouteListOperation;

/**
 * Created by Lina on 2014.07.02.
 */
public class RouteListDataModel extends AbsSingleOperationDataModel {

    private final Context context;
    protected List<RouteModelHis> mList = new ArrayList<>(0);




    public RouteListDataModel(Context context) {
        this.context = context;
    }

    @Override
    protected AbsAsyncOperation<?> createAsyncOperation() {
        return new GetRouteListOperation(context);
    }

    @Override
    protected void handleLoadedData(AbsAsyncOperation<?> operation) {
        if (operation instanceof GetRouteListOperation) {
            GetRouteListOperation op = (GetRouteListOperation) operation;
            mList.addAll(op.getResult());
        }
    }

    /**
     * Returns list items
     * @return items list
     */
    public List<RouteModelHis> getItems() {
        return mList;
    }
}
