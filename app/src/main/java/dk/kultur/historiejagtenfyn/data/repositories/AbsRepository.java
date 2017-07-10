package dk.kultur.historiejagtenfyn.data.repositories;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import dk.kultur.historiejagtenfyn.HistoriejagtenfynApplication;
import dk.kultur.historiejagtenfyn.data.DataBaseOpener_;

/**
 * Created by Lina on 2014.06.27.
 */
public abstract class AbsRepository<T> implements IRepository<T> {

    protected DataBaseOpener_ mDbOpener = null;
    protected Context mContext;

    public AbsRepository(Context cnt) {
      //  mDbOpener = ((HistoriejagtenfynApplication)cnt.getApplicationContext()).getDataBase();
        mContext = cnt;
    }

    @Override
    public void insert(final List<T> items) throws Exception {
        final Dao<T, Integer> dao = getEntityDao();
        dao.callBatchTasks(new Callable<Void>() {
            @Override
            public Void call() throws SQLException {
                for (T account : items) {
                    dao.create(account);
                }
                return null;
            }
        });
    }

    public void createOrUpdate(final List<T> items) throws Exception {
        final Dao<T, Integer> dao = getEntityDao();
        dao.callBatchTasks(new Callable<Void>() {
            @Override
            public Void call() throws SQLException {
                for (T account : items) {
                    dao.createOrUpdate(account);
                }
                return null;
            }
        });
    }

    @Override
    public List<T> selectAll() {
        List<T> result = new ArrayList<T>(0);
        try {
            final Dao<T, Integer> dao = getEntityDao();
            QueryBuilder<T, Integer> builder = dao.queryBuilder();
            result = builder.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Integer cleanAll() {
        try {
            final Dao<T, Integer> dao = getEntityDao();
            DeleteBuilder<T, Integer> builder = dao.deleteBuilder();
            PreparedDelete<T> preparedDelete = builder.prepare();
            return dao.delete(preparedDelete);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }

    }


    protected abstract Dao<T, Integer> getEntityDao() throws SQLException;


}
