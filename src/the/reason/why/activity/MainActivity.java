package the.reason.why.activity;

import wei.he.wo.xin.R;
import the.reason.why.view.BannerView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity
    extends BaseActivity
    implements OnClickListener
{
    private static final String TAG_NAME = MainActivity.class.getSimpleName();

    private static final String DB_LANG_KEY = "DB_LANG";
    public static final String[] items = { "English", "中文" };
    public static final String[] data = { "en", "zh" };
    public SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(getApplicationContext().getPackageName(),
            MODE_PRIVATE);
        prefs.edit().remove(DB_LANG_KEY)
                        .putString(DB_LANG_KEY, getString(R.string.app_lang))
                        .commit();

        final View view = new BannerView(this);
        view.setOnClickListener(this);
        // CountDownTimer(long millisInFuture, long countDownInterval)
        new CountDownTimer(2000, 1000) {
            public void onTick(long millisUntilFinished)
            {
                Log.d(TAG_NAME, "seconds remaining: " + millisUntilFinished
                                / 1000);
            }

            public void onFinish()
            {
                onClick(view);
            }
        }.start();
        setContentView(view);
        Log.d(TAG_NAME, "Banner View activated successfully!");
    }

    @Override
    public void onClick(View v)
    {
        v.setOnClickListener(null);
        Log.d(TAG_NAME, "View: " + v.toString() + " clicked!");

        Intent intent = new Intent(this, TocListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
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
