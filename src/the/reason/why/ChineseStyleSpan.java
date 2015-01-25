package the.reason.why;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Parcel;
import android.text.TextPaint;
import android.text.style.StyleSpan;

public class ChineseStyleSpan
    extends StyleSpan
{
    public ChineseStyleSpan(int src)
    {
        super(src);

    }

    public ChineseStyleSpan(Parcel src)
    {
        super(src);
    }

    @Override
    public void updateDrawState(TextPaint ds)
    {
        newApply(ds, this.getStyle());
    }

    @Override
    public void updateMeasureState(TextPaint paint)
    {
        newApply(paint, this.getStyle());
    }

    private static void newApply(Paint paint, int style)
    {
        int oldStyle;

        Typeface old = paint.getTypeface();
        if (old == null)
            oldStyle = 0;
        else
            oldStyle = old.getStyle();

        int want = oldStyle | style;
        Typeface tf;
        if (old == null)
            tf = Typeface.defaultFromStyle(want);
        else
            tf = Typeface.create(old, want);
        // int fake = want & ~tf.getStyle();

        if ((want & Typeface.BOLD) != 0) paint.setFakeBoldText(true);
        if ((want & Typeface.ITALIC) != 0) paint.setTextSkewX(-0.25f);
        // The only two lines to be changed, the normal StyleSpan will set you
        // paint to use FakeBold when you want Bold Style but the Typeface
        // return say it don't support it.
        // However, Chinese words in Android are not bold EVEN THOUGH the
        // typeface return it can bold, so the Chinese with StyleSpan(Bold
        // Style) do not bold at all.
        // This Custom Class therefore set the paint FakeBold no matter typeface
        // return it can support bold or not.
        // Italic words would be the same

        paint.setTypeface(tf);
    }
}