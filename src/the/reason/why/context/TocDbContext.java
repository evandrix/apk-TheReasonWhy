package the.reason.why.context;

import java.sql.SQLException;

import the.reason.why.dao.TocDaoImpl;
import the.reason.why.db.DatabaseHelper;
import the.reason.why.db.DatabaseManager;
import android.content.Context;
import android.util.Log;

public class TocDbContext
{
    private static final String TAG_NAME = TocDbContext.class.getSimpleName();
    public DatabaseHelper db;
    public TocDaoImpl dao;
    public DatabaseManager<DatabaseHelper> manager;

    public TocDbContext(Context context) throws SQLException
    {
        Log.d(TAG_NAME, String.format("before::init() manager=%s;db=%s;dao=%s",
            manager, db, dao));
        manager = new DatabaseManager<DatabaseHelper>();
        db = manager.getHelper(context);
        dao = new TocDaoImpl(db);
        Log.d(TAG_NAME, String.format("after::init() manager=%s;db=%s;dao=%s",
            manager, db, dao));
    }

    public void close()
    {
        manager.releaseHelper();
    }
}
