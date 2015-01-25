package the.reason.why.db;

import java.sql.SQLException;

import the.reason.why.model.BookItem;
import the.reason.why.model.TocItem;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseHelper
    extends OrmLiteSqliteOpenHelper
{
    private static final String TAG_NAME = DatabaseHelper.class
                    .getSimpleName();
    private static final String DB_LANG_KEY = "DB_LANG";
    private static final int DATABASE_VERSION = 11;
    private transient Context mContext;
    private DatabaseInitializer initializer;

    public static SharedPreferences getSharedPreferences(Context ctxt)
    {
        return ctxt.getSharedPreferences(ctxt.getPackageName(),
            Context.MODE_PRIVATE);
    }

    public static String getDbLang(Context ctxt)
    {
        SharedPreferences prefs = getSharedPreferences(ctxt);
        return prefs.getString(DB_LANG_KEY, null);
    }

    /**
     * Initializes the database helper
     * 
     * @param ctx the context to run in.
     * @throws SQLException
     */
    public DatabaseHelper(Context context)
    {
        super(context, context.getResources().getString(
            context.getResources().getIdentifier(getDbLang(context), "string",
                context.getPackageName())), null, DATABASE_VERSION);
        Log.d(TAG_NAME, "::init() pkg_name - " + context.getPackageName());
        mContext = context;
        initializer = new DatabaseInitializer(mContext);
        initializer.createDatabase(this);
        initializer.closeDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource)
    {
        Log.d(TAG_NAME, String.format("onCreate(%s)", db.toString()));
        // try {
        // TableUtils.createTableIfNotExists(connectionSource,
        // TocItem.class);
        // TableUtils.createTableIfNotExists(connectionSource,
        // BookItem.class);
        // TableUtils.clearTable(connectionSource, TocItem.class);
        // TableUtils.clearTable(connectionSource, BookItem.class);
        // TocDbContext tocDbContext = new TocDbContext(mContext);
        // List<TocItem> tocList = tocDbContext.toc.getAll();
        // int chapter_no = 1;
        // for (TocItem item : tocList) {
        // getTocDao().createOrUpdate(item);
        // BookDbContext bookDbContext = new BookDbContext(mContext);
        // List<BookItem> bookList = bookDbContext.book.getAll(chapter_no);
        // if (bookList == null) continue;
        // for (BookItem chapter : bookList) {
        // if (chapter == null) break;
        // getBookDao().createOrUpdate(chapter);
        // }
        // chapter_no++;
        // }
        // fillDatabaseWithDummyData();
        // } catch (SQLException e) {
        // throw new RuntimeException("Can't create database");
        // }
    }

    @Override
    public void onUpgrade(
        SQLiteDatabase db,
        ConnectionSource connectionSource,
        int oldVersion,
        int newVersion)
    {
        Log.d(
            TAG_NAME,
            String.format("onUpgrade(db=%s [%d=>%d], connStr=%s)",
                db.toString(), oldVersion, newVersion,
                connectionSource.toString()));
        // try {
        // Log.d(TAG_NAME, "onUpgrade(): Refreshing table 'toc'...");
        // TableUtils.dropTable(connectionSource, TocItem.class, true);
        // Log.d(TAG_NAME, "onUpgrade(): Refreshing table 'book'...");
        // TableUtils.dropTable(connectionSource, BookItem.class, true);
        // onCreate(db, connectionSource);
        // } catch (SQLException e) {
        // throw new RuntimeException("Can't drop tables in database");
        // }
    }

    /**
     * Drops all tables and recreates them. Used by unit tests only!!!
     * 
     * @throws SQLException
     */
    public void recreateDatabase()
        throws SQLException
    {
        final SQLiteDatabase database = getWritableDatabase();
        final ConnectionSource connection = getConnectionSource();
        TableUtils.dropTable(connection, TocItem.class, true);
        TableUtils.dropTable(connection, BookItem.class, true);
        onCreate(database, connection);
    }
}
