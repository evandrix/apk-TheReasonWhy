package the.reason.why.activity;

import wei.he.wo.xin.R;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class LangSelActivity
    extends BaseActivity
{
    private static final String TAG_NAME = LangSelActivity.class
                    .getSimpleName();
    private static final String DB_LANG_KEY = "DB_LANG";
    public static final String[] items = { "English", "中文" };
    public static final String[] data = { "en", "zh" };
    public SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        setContentView(R.layout.blank);
        Log.d(TAG_NAME, "Language selector activity started successfully!");
        Builder builder = new Builder(this);
        builder.setTitle("Select language");
//        builder.setIcon(R.drawable.icon);
        builder.setItems(items, new OnClickListener() {
            public void onClick(DialogInterface dialog, int item)
            {
                prefs = getSharedPreferences(getApplicationContext()
                                .getPackageName(), MODE_PRIVATE);

                Log.d(TAG_NAME,
                    "before: DB_LANG - " + prefs.getString(DB_LANG_KEY, null));
                prefs.edit().remove(DB_LANG_KEY)
                                .putString(DB_LANG_KEY, data[item]).commit();
                Log.d(TAG_NAME,
                    "after: DB_LANG - " + prefs.getString(DB_LANG_KEY, null));

                Log.d(TAG_NAME, "Starting MainActivity...");
                startActivity(new Intent(getApplicationContext(),
                                MainActivity.class));
            }
        });
        AlertDialog alert = builder.create();
        alert.setCancelable(false);
        alert.show();
        Log.d(TAG_NAME, "ListView AlertDialog activated successfully!");
    }
}
