package dk.kultur.historiejagtenfyn.data.repositories;

import android.content.Context;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import dk.kultur.historiejagtenfyn.data.entities.RouteContent;

/**
 * Created by Lina on 2014.07.01.
 */
public class RouteContentRepository extends AbsRepository<RouteContent> {
    public RouteContentRepository(Context cnt) {
        super(cnt);
    }

    @Override
    protected Dao<RouteContent, Integer> getEntityDao() throws SQLException {
        return mDbOpener.getRouteContentDao();
    }
}
