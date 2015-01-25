package the.reason.why.view;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import the.reason.why.MainApplication;
import wei.he.wo.xin.R;
import the.reason.why.activity.BookListActivity;
import the.reason.why.context.BookDbContext;
import the.reason.why.model.BookItem;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

@SuppressWarnings("unused")
public class BookAdapter
    extends BaseAdapter
{
    private static final String TAG_NAME = BookAdapter.class.getSimpleName();
    private LayoutInflater mInflater;
    private Context context;
    public BookDbContext bookDbContext;
    private Bundle bundle;
    private int chapter;
    public static Set<TextView> listViewItems;

    public void closeAllDb()
    {
        if (bookDbContext != null) {
            bookDbContext.close();
        }
    }

    /**
     * Constructor
     * 
     * @param context
     * @throws SQLException
     */
    public BookAdapter(Context context, Bundle bundle) throws SQLException
    {
        listViewItems = new HashSet<TextView>();
        this.context = context;
        this.bundle = bundle;
        this.chapter = bundle.getInt("CHAPTER");
        mInflater = LayoutInflater.from(context);
        bookDbContext = new BookDbContext(context);
        Log.d(TAG_NAME, "BookAdapter constructed successfully!");
    }

    @Override
    public int getCount()
    {
        try {
            return bookDbContext.dao.length(chapter);
        } catch (SQLException e) {
            Log.e(TAG_NAME, "Error: unable to get length of chapter", e);
        }
        return 0;
    }

    @Override
    public BookItem getItem(int position)
    {
        try {
            Log.d(TAG_NAME,
                String.format("BookItem#%d retrieved successfully", position));
            // convert from 0-based (here) -> 1-based (SQL) indexing
            return bookDbContext.dao.getContent(chapter, position + 1);
        } catch (SQLException e) {
            Log.e(TAG_NAME, String.format(
                "Error: unable to retrieve BookItem@%d", position), e);
        }
        return null;
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    public static Set<TextView> getAllChildren()
    {
        return listViewItems;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View newView = convertView;
        // if (newView == null) {
        newView = mInflater.inflate(R.layout.list_item_content, parent, false);
        // }

        TextView txtBookItem = (TextView) newView.findViewById(R.id.listItem);
        txtBookItem.setPadding(0, 0, 0, 0);

        // populate TextView with data from DB
        try {
            // convert from 0-based (here) -> 1-based (SQL) indexing
            BookItem item = bookDbContext.dao.getContent(chapter, position + 1);
            if (item != null) {
                Log.d(TAG_NAME,
                    String.format("#%d: %s", position + 1, item.getContent()));
                txtBookItem.setText(Html.fromHtml(item.getContent()));
            }
        } catch (SQLException e) {
            Log.e(TAG_NAME, "Error: unable to retrieve chapter content", e);
        }

        assert context instanceof BookListActivity;
        MainApplication app = (MainApplication) context.getApplicationContext();
        txtBookItem.setTextSize(app.getDesiredTextSize());
        listViewItems.add(txtBookItem);

        return newView;
    }

    public int getDistance(MotionEvent event)
    {
        int dx = (int) (event.getX(0) - event.getX(1));
        int dy = (int) (event.getY(0) - event.getY(1));
        return (int) Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public boolean areAllItemsEnabled()
    {
        return false;
    }

    @Override
    public boolean isEnabled(int position)
    {
        return false;
    }
}
