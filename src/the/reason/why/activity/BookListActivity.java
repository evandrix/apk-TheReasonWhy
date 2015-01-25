package the.reason.why.activity;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import the.reason.why.MainApplication;
import wei.he.wo.xin.R;
import the.reason.why.SimpleGestureFilter;
import the.reason.why.view.BookAdapter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

@SuppressWarnings("unused")
public class BookListActivity
    extends BaseListActivity
{
    private static final String TAG_NAME = BookListActivity.class
                    .getSimpleName();
    private TextView txtTitle;
    private Button btnBack;
    private boolean bUseFullscreen;

    /* Gestures */
    private SimpleGestureFilter detector;
    private GestureDetector gestureDetector;

    public static final List<Integer> events = new ArrayList<Integer>();

    /* TextView zooming functionality parameters */
    public final static float STEP = 200;
    public final static int fontSize = 18;

    public BookAdapter bookAdapter;
    public ZoomListener zoomListener;

    public MainApplication app;

    public class ZoomListener
        implements OnTouchListener
    {
        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            Log.v(TAG_NAME, v.toString() + " / " + event.toString());
            MainApplication app = (MainApplication) getApplication();
            boolean bSetTextSize = false;
            int action = event.getAction();
            int pureaction = action & MotionEvent.ACTION_MASK;
            int pointerIndex = ((event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            int pointerId = event.getPointerId(pointerIndex);
            int pointerCount = event.getPointerCount();
            Log.v(
                TAG_NAME,
                String.format(
                    "action: %d, pureaction: %d, pointerIdx: %d, pointerId: %d, pointerCnt: %d",
                    action, pureaction, pointerIndex, pointerId, pointerCount));
            events.add(pointerCount);
            if (pureaction == MotionEvent.ACTION_UP) {
                // Log.d(getLocalClassName(), "# events = " +
                // events.size());
                boolean bMultiTouch = false;
                for (int evtCnt : events) {
                    if (evtCnt > 1) {
                        bMultiTouch = true;
                        break;
                    }
                }
                events.clear();
                if (!bMultiTouch) {
                    // Log.d(getLocalClassName(), "Single tap");
                    // app.setmRatio(1.0f);
                    // app.setDesiredTextSize(app.getmRatio() +
                    // fontSize);
                    // bSetTextSize = false/* true */;
                }
            }
            if (pointerCount == 2) {
                if (pureaction == MotionEvent.ACTION_POINTER_DOWN) {
                    app.setmBaseDist(getDistance(event));
                    app.setmBaseRatio(app.getmRatio());
                } else {
                    float delta = (getDistance(event) - app.getmBaseDist())
                                    / STEP;
                    float multi = (float) Math.pow(2, delta);
                    app.setmRatio(Math.min(1024.0f,
                        Math.max(0.1f, app.getmBaseRatio() * multi)));
                    app.setDesiredTextSize(app.getmRatio() + fontSize);
                    bSetTextSize = true;
                }
            }

            assert v instanceof ListView;
            if (bSetTextSize) {
                setTextSize();
            }
            return false;
        }

        public void setTextSize()
        {
            app = (MainApplication) getApplication();
            for (TextView tv : BookAdapter.getAllChildren()) {
                Log.v(TAG_NAME, "\t" + tv.toString());
                tv.setTextSize(app.getDesiredTextSize());
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        bUseFullscreen = false;
        setContentView(R.layout.main);
        Log.d(TAG_NAME, "Book ListView activated successfully!");

        detector = new SimpleGestureFilter(this);
        gestureDetector = new GestureDetector(detector);

        // Extract the bundle from intent
        Bundle bundle = getIntent().getExtras();

        // UI components
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        btnBack = (Button) findViewById(R.id.btnExit);
        if (txtTitle == null || btnBack == null) {
            Log.d(TAG_NAME, "txtTitle / btnBack in main View is null");
            return;
        }

        app = (MainApplication) getApplication();

        // Set values
        txtTitle.setText(
            Html.fromHtml(String.format("<font color=#FFC330>%s</font>",
                bundle.getString("TITLE"))), TextView.BufferType.SPANNABLE);
        txtTitle.setTextSize(app.getmRatio() + fontSize);
        SharedPreferences prefs = getSharedPreferences(getApplicationContext()
                        .getPackageName(), MODE_PRIVATE);
        String dbLang = prefs.getString("DB_LANG", null);
        btnBack.setText("Table of Contents");
        if (dbLang.equalsIgnoreCase("zh")) {
            btnBack.setText("目录");
        }
        btnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG_NAME, "Back button was pressed!");
                finish();
            }
        });

        // Set the test adapter
        try {
            bookAdapter = new BookAdapter(this, bundle);
            setListAdapter(bookAdapter);
            zoomListener = new ZoomListener();
            getListView().setOnTouchListener(zoomListener);
        } catch (SQLException e) {
            Log.e(TAG_NAME, "Error: Unable to set Adapter", e);
        }
    }

    @Override
    public void onDestroy()
    {
        Log.d(TAG_NAME, "onDestroy()");
        super.onDestroy();
        bookAdapter.closeAllDb();
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

    /** Gesture events */
    public void onSwipe(int direction)
    {
        String str = "";
        switch (direction) {
        case SimpleGestureFilter.SWIPE_RIGHT:
            str = "Swipe Right";
            break;
        case SimpleGestureFilter.SWIPE_LEFT:
            str = "Swipe Left";
            break;
        case SimpleGestureFilter.SWIPE_DOWN:
            str = "Swipe Down";
            break;
        case SimpleGestureFilter.SWIPE_UP:
            str = "Swipe Up";
            break;
        }
        // Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    private void updateFullscreenStatus(boolean bUseFullscreen)
    {
        LinearLayout btnBackParent = (LinearLayout) btnBack.getParent();

        if (bUseFullscreen) {
            btnBackParent.setVisibility(View.GONE);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            btnBackParent.setVisibility(View.VISIBLE);
            getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }

        btnBackParent.requestLayout();
        findViewById(android.R.id.content).getRootView().requestLayout();
    }

    public void onDoubleTap()
    {
        // Toast.makeText(this, "Double Tap", Toast.LENGTH_SHORT).show();
        bUseFullscreen = !bUseFullscreen;
        updateFullscreenStatus(bUseFullscreen);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event)
    {
        this.detector.onTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    public int getDistance(MotionEvent event)
    {
        int dx = (int) (event.getX(0) - event.getX(1));
        int dy = (int) (event.getY(0) - event.getY(1));
        return (int) Math.sqrt(dx * dx + dy * dy);
    }
}
