package dk.kultur.historiejagtenfyn.data.operations;

import android.content.Context;

import dk.kultur.historiejagtenfyn.data.parse.models.RouteModelHis;
import java.util.List;

import dk.kultur.historiejagtenfyn.data.entities.Route;
import dk.kultur.historiejagtenfyn.data.repositories.RouteRepository;

/**
 * Created by Lina on 2014.07.02.
 */
public class GetRouteListOperation extends AbsAsyncOperation<List<RouteModelHis>> {

    private RouteRepository routeRepository;

    public GetRouteListOperation(Context context) {
        routeRepository = new RouteRepository(context);
    }

    @Override
    protected List<RouteModelHis> doInBackground(String... params) {
        return routeRepository.getRoutes();
    }
}
