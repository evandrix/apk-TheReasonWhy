package the.reason.why.activity;

import java.io.IOException;
import java.sql.SQLException;

import wei.he.wo.xin.R;
import the.reason.why.view.TocAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class TocListActivity
    extends BaseListActivity
{
    private static final String TAG_NAME = TocListActivity.class
                    .getSimpleName();
    private TextView txtTitle;
    private Button btnExit;

    public float mRatio = 1.0f;
    public int fontSize = 18;

    public SharedPreferences prefs;
    public TocAdapter tocAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.d(TAG_NAME, "Toc ListView activated successfully!");

        // Set the test adapter
        try {
            tocAdapter = new TocAdapter(this);
            setListAdapter(tocAdapter);
        } catch (SQLException e) {
            Log.e(TAG_NAME, "Error: SQLException", e);
        } catch (IOException e) {
            Log.e(TAG_NAME, "Error: IOException", e);
        }

        // TextViews
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        btnExit = (Button) findViewById(R.id.btnExit);
        if (txtTitle == null || btnExit == null) {
            Log.d(TAG_NAME, "txtTitle / btnExit in main View is null");
            return;
        }

        // set values
        prefs = getSharedPreferences(getApplicationContext().getPackageName(),
            MODE_PRIVATE);
        String dbLang = prefs.getString("DB_LANG", null);
        txtTitle.setText("Table of Contents");
        if (dbLang.equalsIgnoreCase("zh")) {
            txtTitle.setText("目录");
        }
        txtTitle.setTextSize(mRatio + fontSize);
        if (dbLang.equalsIgnoreCase("zh")) {
            btnExit.setText("退出");
        }

        btnExit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG_NAME, "Exit button was pressed!");
                prefs.edit().clear().commit();

                Intent intent = new Intent("android.action.KILL");
                intent.setType("text/plain");
                sendBroadcast(intent);
            }
        });
    }

    @Override
    public void onDestroy()
    {
        Log.d(TAG_NAME, "onDestroy()");
        super.onDestroy();
        tocAdapter.closeAllDb();
        System.gc();
    }

    /**
     * Set the current orientation to landscape. This will prevent the OS from
     * changing the app's orientation.
     */
    public void lockOrientationLandscape()
    {
        lockOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    /**
     * Set the current orientation to portrait. This will prevent the OS from
     * changing the app's orientation.
     */
    public void lockOrientationPortrait()
    {
        lockOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    /**
     * Locks the orientation to a specific type. Possible values are:
     * <ul>
     * <li>{@link ActivityInfo#SCREEN_ORIENTATION_BEHIND}</li>
     * <li>{@link ActivityInfo#SCREEN_ORIENTATION_LANDSCAPE}</li>
     * <li>{@link ActivityInfo#SCREEN_ORIENTATION_NOSENSOR}</li>
     * <li>{@link ActivityInfo#SCREEN_ORIENTATION_PORTRAIT}</li>
     * <li>{@link ActivityInfo#SCREEN_ORIENTATION_SENSOR}</li>
     * <li>{@link ActivityInfo#SCREEN_ORIENTATION_UNSPECIFIED}</li>
     * <li>{@link ActivityInfo#SCREEN_ORIENTATION_USER}</li>
     * </ul>
     * 
     * @param orientation
     */
    public void lockOrientation(int orientation)
    {
        setRequestedOrientation(orientation);
    }
}
