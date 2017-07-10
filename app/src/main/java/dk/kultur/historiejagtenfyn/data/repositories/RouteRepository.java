package dk.kultur.historiejagtenfyn.data.repositories;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedDelete;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import dk.kultur.historiejagtenfyn.data.parse.models.LanguageModelHis;
import dk.kultur.historiejagtenfyn.data.parse.models.RouteModelHis;
import java.sql.SQLException;

import dk.kultur.historiejagtenfyn.data.entities.Route;
import dk.kultur.historiejagtenfyn.data.entities.RouteContent;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Juskrt on 2015.01.07.
 */
public class RouteRepository extends AbsRepository<Route> {


    private static final String LOG_TAG = RouteRepository.class.getSimpleName();
    public RouteRepository(Context cnt) {
        super(cnt);
    }

    @Override
    protected Dao<Route, Integer> getEntityDao() throws SQLException {
        return mDbOpener.getRouteDao();
    }

    public void deleteRoute(String objectId) throws SQLException {
        Dao<Route, Integer> routeDao = getEntityDao();
        DeleteBuilder<Route, Integer> builder = routeDao.deleteBuilder();
        builder.where().eq(Route.COLUMN_OBJECTID, objectId);
        PreparedDelete<Route> preparedDelete = builder.prepare();
        Log.i("Route delete ", preparedDelete.getStatement());
        int deleted = routeDao.delete(preparedDelete);

        Dao<RouteContent, Integer> routeContentDao = mDbOpener.getRouteContentDao();
        DeleteBuilder<RouteContent, Integer> contentBuilder = routeContentDao.deleteBuilder();
        contentBuilder.where().eq(RouteContent.COLUMN_ROUTE_ID, objectId);
        PreparedDelete<RouteContent> contentPreparedDelete = contentBuilder.prepare();
        Log.i("Route delete ", contentPreparedDelete.getStatement());
        deleted = routeContentDao.delete(contentPreparedDelete);

    }

    public List<RouteModelHis> getRoutes(){
        ParseQuery<RouteModelHis> query = ParseQuery.getQuery(RouteModelHis.class);
        query.orderByAscending(RouteModelHis.KEY_NAME);
        final String TOP = "activeRoutes";
        List<RouteModelHis> routes = null;

        try {
            routes = query.find();
            //saving data to offline mode
            ParseObject.unpinAll(TOP);
            ParseObject.pinAll(TOP, routes);

        } catch (ParseException e) {
            Log.e(getLogTag(), "Uncaught exception", e);
        }
        return routes;
    }


    public static String getLogTag() {
        return LOG_TAG;
    }

}
