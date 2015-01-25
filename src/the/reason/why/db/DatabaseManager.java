package the.reason.why.db;

import java.lang.reflect.ParameterizedType;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;

@SuppressWarnings({ "unchecked" })
public class DatabaseManager<H extends OrmLiteSqliteOpenHelper>
{
    private H helper;

    public Class<H> getGenericRuntimeClass()
    {
        ParameterizedType parameterizedType = (ParameterizedType) getClass()
                        .getGenericSuperclass();
        return (Class<H>) parameterizedType.getActualTypeArguments()[0];
    }

    public H getHelper(Context context)
    {
        helper = (H) OpenHelperManager.getHelper(context,
            DatabaseHelper.class);
        return helper;
    }

    public void releaseHelper()
    {
        if (helper != null) {
            OpenHelperManager.releaseHelper();
            helper = null;
        }
    }
}
