package the.reason.why.dao;

import java.sql.SQLException;
import java.util.List;

import the.reason.why.db.DatabaseHelper;
import the.reason.why.model.TocItem;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;

/**
 * implements CRUD, among other utility functions
 */
public class TocDaoImpl
{
    private Dao<TocItem, String> tocDao;

    public TocDaoImpl(DatabaseHelper db) throws SQLException
    {
        tocDao = DaoManager.createDao(db.getConnectionSource(), TocItem.class);
    }

    public int create(TocItem article)
        throws SQLException
    {
        return tocDao.create(article);
    }

    public int update(TocItem article)
        throws SQLException
    {
        return tocDao.update(article);
    }

    public int delete(TocItem article)
        throws SQLException
    {
        return tocDao.delete(article);
    }

    public TocItem getById(int id)
        throws SQLException
    {
        return tocDao.queryForId(Integer.toString(id));
    }

    public int length()
        throws SQLException
    {
        return tocDao.queryForAll().size();
    }

    public List<TocItem> getAll()
        throws SQLException
    {
        return tocDao.queryForAll();
    }
}
