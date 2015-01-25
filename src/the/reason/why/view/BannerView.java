package the.reason.why.view;

import wei.he.wo.xin.R;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class BannerView
    extends View
{
    private Drawable logo;

    private void draw(Context context)
    {
        logo = context.getResources().getDrawable(R.drawable.logo);
        setBackgroundDrawable(logo);
    }

    public BannerView(Context context)
    {
        super(context);
        draw(context);
    }

    public BannerView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        draw(context);
    }

    public BannerView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        draw(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = width * logo.getIntrinsicHeight()
                        / logo.getIntrinsicWidth();
        setMeasuredDimension(width, height);
    }
}
