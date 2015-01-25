package the.reason.why;

import android.app.Application;

public class MainApplication
    extends Application
{
    public float mBaseRatio, mRatio, desiredTextSize;
    public int mBaseDist;

    @Override
    public void onCreate()
    {
        super.onCreate();
        mRatio = mBaseRatio = 1.0f;
        desiredTextSize = 19.0f;
        mBaseDist = 0;
    }

    public float getmRatio()
    {
        return mRatio;
    }

    public void setmRatio(float mRatio)
    {
        this.mRatio = mRatio;
    }

    public float getDesiredTextSize()
    {
        return desiredTextSize;
    }

    public void setDesiredTextSize(float desiredTextSize)
    {
        this.desiredTextSize = desiredTextSize;
    }

    public float getmBaseRatio()
    {
        return mBaseRatio;
    }

    public void setmBaseRatio(float mBaseRatio)
    {
        this.mBaseRatio = mBaseRatio;
    }

    public int getmBaseDist()
    {
        return mBaseDist;
    }

    public void setmBaseDist(int mBaseDist)
    {
        this.mBaseDist = mBaseDist;
    }
}
