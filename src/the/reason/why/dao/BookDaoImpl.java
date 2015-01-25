package the.reason.why.dao;

import java.sql.SQLException;
import java.util.List;

import the.reason.why.db.DatabaseHelper;
import the.reason.why.model.BookItem;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

/**
 * implements CRUD, among other utility functions
 */
public class BookDaoImpl
{
    private Dao<BookItem, String> bookDao;

    public BookDaoImpl(DatabaseHelper db) throws SQLException
    {
        bookDao = DaoManager
                        .createDao(db.getConnectionSource(), BookItem.class);
    }

    public int create(BookItem article)
        throws SQLException
    {
        return bookDao.create(article);
    }

    public int update(BookItem article)
        throws SQLException
    {
        return bookDao.update(article);
    }

    public int delete(BookItem article)
        throws SQLException
    {
        return bookDao.delete(article);
    }

    public BookItem getContent(int chapter, int verse)
        throws SQLException
    {
        // get our query builder from the DAO
        QueryBuilder<BookItem, String> queryBuilder = bookDao.queryBuilder();
        // the 'password' field must be equal to "qwerty"
        queryBuilder.where().eq("chapter", chapter).and().eq("verse", verse);
        // prepare our sql statement
        PreparedQuery<BookItem> preparedQuery = queryBuilder.prepare();
        // query for all accounts that have that password
        return bookDao.queryForFirst(preparedQuery);
    }

    public int length(int chapter)
        throws SQLException
    {
        return getAll(chapter).size();
    }

    public List<BookItem> getAll(int chapter)
        throws SQLException
    {
        QueryBuilder<BookItem, String> queryBuilder = bookDao.queryBuilder();
        queryBuilder.where().eq("chapter", chapter);
        PreparedQuery<BookItem> preparedQuery = queryBuilder.prepare();
        return bookDao.query(preparedQuery);
    }
}
