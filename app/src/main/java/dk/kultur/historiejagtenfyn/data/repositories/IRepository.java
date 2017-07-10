package dk.kultur.historiejagtenfyn.data.repositories;

import java.util.List;

/**
 * Created by Lina on 2014.06.27.
 */
public interface IRepository<T> {

    public void insert(List<T> items) throws Exception;
    public List<T> selectAll();
    public void createOrUpdate(List<T> items) throws Exception;
    public Integer cleanAll();
}
