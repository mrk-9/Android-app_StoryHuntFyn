package dk.kultur.historiejagtenfyn.data.models;

import android.content.Context;
import com.parse.ParseObject;
import dk.kultur.historiejagtenfyn.data.operations.AbsAsyncOperation;
import dk.kultur.historiejagtenfyn.data.operations.GetInfoDetailsOperation;
import dk.kultur.historiejagtenfyn.data.operations.GetRouteListOperation;
import dk.kultur.historiejagtenfyn.data.parse.models.RouteModelHis;
import java.util.ArrayList;

/**
 * Created by Juskrt on 2015.01.07.
 */
public class RoutesDataModel extends AbsSingleOperationDataModel {

    private final Context context;
    private ArrayList<RouteModelHis> route;

    /**
     *
     * @param context
     */
    public RoutesDataModel(Context context) {
        this.context = context;
    }

    /**
     * Returns route object with info in it
     * Use fields infoTitle and infoText for displaying
     * @return
     */
    public ArrayList<RouteModelHis> getRoutes() {
        return route;
    }

    @Override
    protected AbsAsyncOperation<?> createAsyncOperation() {
        return new GetRouteListOperation(context);
    }

    @Override
    protected void handleLoadedData(AbsAsyncOperation<?> operation) {
        route = (ArrayList<RouteModelHis>) operation.getResult();
    }
}
