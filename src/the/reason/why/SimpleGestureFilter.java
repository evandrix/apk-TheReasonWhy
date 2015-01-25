package the.reason.why;

import the.reason.why.activity.BookListActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

public class SimpleGestureFilter
    extends SimpleOnGestureListener
{
    private static final String TAG_NAME = SimpleGestureFilter.class
                    .getSimpleName();
    public final static int SWIPE_UP = 1;
    public final static int SWIPE_DOWN = 2;
    public final static int SWIPE_LEFT = 3;
    public final static int SWIPE_RIGHT = 4;

    public final static int MODE_TRANSPARENT = 0;
    public final static int MODE_SOLID = 1;
    public final static int MODE_DYNAMIC = 2;

    private final static int ACTION_FAKE = -13; // just an unlikely number
    private int swipe_Min_Distance = 100;
    private int swipe_Max_Distance = 350;
    private int swipe_Min_Velocity = 100;

    private int mode = MODE_DYNAMIC;
    private boolean running = true;
    private boolean tapIndicator = false;

    private final BookListActivity context;
    private final GestureDetector detector;

    public SimpleGestureFilter(BookListActivity context)
    {
        this.context = context;
        detector = new GestureDetector(context, this);
    }

    public void onTouchEvent(MotionEvent event)
    {
        if (!running) return;

        boolean result = detector.onTouchEvent(event);

        if (mode == MODE_SOLID)
            event.setAction(MotionEvent.ACTION_CANCEL);
        else if (mode == MODE_DYNAMIC) {

            if (event.getAction() == ACTION_FAKE)
                event.setAction(MotionEvent.ACTION_UP);
            else if (result)
                event.setAction(MotionEvent.ACTION_CANCEL);
            else if (tapIndicator) {
                event.setAction(MotionEvent.ACTION_DOWN);
                tapIndicator = false;
            }

        }
        // else just do nothing, it's Transparent
    }

    public void setMode(int m)
    {
        mode = m;
    }

    public int getMode()
    {
        return mode;
    }

    public void setEnabled(boolean status)
    {
        running = status;
    }

    public void setSwipeMaxDistance(int distance)
    {
        swipe_Max_Distance = distance;
    }

    public void setSwipeMinDistance(int distance)
    {
        swipe_Min_Distance = distance;
    }

    public void setSwipeMinVelocity(int distance)
    {
        swipe_Min_Velocity = distance;
    }

    public int getSwipeMaxDistance()
    {
        return swipe_Max_Distance;
    }

    public int getSwipeMinDistance()
    {
        return swipe_Min_Distance;
    }

    public int getSwipeMinVelocity()
    {
        return swipe_Min_Velocity;
    }

    @Override
    public boolean onFling(
        MotionEvent e1,
        MotionEvent e2,
        float velocityX,
        float velocityY)
    {

        final float xDistance = Math.abs(e1.getX() - e2.getX());
        final float yDistance = Math.abs(e1.getY() - e2.getY());

        if (xDistance > swipe_Max_Distance || yDistance > swipe_Max_Distance)
            return false;

        velocityX = Math.abs(velocityX);
        velocityY = Math.abs(velocityY);
        boolean result = false;

        if (velocityX > swipe_Min_Velocity && xDistance > swipe_Min_Distance) {
            if (e1.getX() > e2.getX()) // right to left
            {
                context.onSwipe(SWIPE_LEFT);
            } else {
                context.onSwipe(SWIPE_RIGHT);
            }

            result = true;
        } else if (velocityY > swipe_Min_Velocity
                        && yDistance > swipe_Min_Distance) {
            if (e1.getY() > e2.getY()) // bottom to up
            {
                context.onSwipe(SWIPE_UP);
            } else {
                context.onSwipe(SWIPE_DOWN);
            }

            result = true;
        }

        return result;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e)
    {
        tapIndicator = true;
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent me)
    {
        context.onDoubleTap();
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent me)
    {
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent me)
    {
        if (mode == MODE_DYNAMIC) {
            // we owe an ACTION_UP, so we fake an action which will be converted
            // to an ACTION_UP later
            me.setAction(ACTION_FAKE);
            context.dispatchTouchEvent(me);
        }
        Log.d(TAG_NAME, "Single tap confirmed");
        MainApplication app = (MainApplication) context.getApplication();
        app.setmRatio(1.0f);
        app.setDesiredTextSize(app.getmRatio() + BookListActivity.fontSize);
        context.zoomListener.setTextSize();
        return false;
    }
}
