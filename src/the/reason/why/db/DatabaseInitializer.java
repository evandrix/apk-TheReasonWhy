package the.reason.why.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DatabaseInitializer
{
    private static final String TAG_NAME = DatabaseInitializer.class
                    .getSimpleName();
    private static final String DB_LANG_KEY = "DB_LANG";
    public SQLiteDatabase mDatabase;
    public Context mContext;
    public String DB_LANG, DB_NAME, DB_PATH;
    public SharedPreferences prefs;

    public DatabaseInitializer(Context context)
    {
        Log.d(TAG_NAME, "::init()");
        mContext = context;
        String pkgName = context.getPackageName();

        prefs = context.getSharedPreferences(pkgName, Context.MODE_PRIVATE);
        DB_LANG = prefs.getString(DB_LANG_KEY, null);
        Log.d(TAG_NAME, "DB_LANG = " + DB_LANG);

        int dbId = context.getResources().getIdentifier(DB_LANG, "string",
            pkgName);
        DB_NAME = context.getResources().getString(dbId);
        Log.d(TAG_NAME, "DB_NAME = " + DB_NAME);

        DB_PATH = context.getDatabasePath(DB_NAME).getPath();
        Log.d(TAG_NAME, "DB_PATH = " + DB_PATH);
    }

    public void createDatabase(DatabaseHelper mDbHelper)
    {
        Log.d(TAG_NAME, String.format("createDatabase(%s)", mDbHelper));
        try {
            if (!isDatabasePresent()) {
                // get database; will override it at next steep
                // but folders containing the database are created
                mDatabase = mDbHelper.getWritableDatabase();
                mDatabase.close();
                // copy database
                copyDatabase();
            }
            mDatabase = mDbHelper.getWritableDatabase();
        } catch (SQLException eSQL) {
            Log.e(TAG_NAME, "Error: Cannot open database", eSQL);
        } catch (IOException IOe) {
            Log.e(TAG_NAME, "Error: Cannot copy initial database", IOe);
        }
    }

    /**
     * queries existence of database by attempting to open it
     */
    private boolean isDatabasePresent()
    {
        return new File(DB_PATH).exists();
    }

    private void copyDatabase()
        throws IOException
    {
        Log.d(TAG_NAME,
            String.format("copyDatabase(): %s -> %s", DB_NAME, DB_PATH));

        // Open local db as input stream
        InputStream myInput = mContext.getAssets().open(DB_NAME,
            AssetManager.ACCESS_STREAMING);

        // Path to newly created empty db
        OutputStream myOutput = new FileOutputStream(DB_PATH);

        // Transfer bytes from inputfile -> outputfile
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        // Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    public synchronized void closeDatabase()
    {
        if (mDatabase != null) {
            mDatabase.close();
            mDatabase = null;
        }
    }
}
