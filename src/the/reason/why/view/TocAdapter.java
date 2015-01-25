package the.reason.why.view;

import java.io.IOException;
import java.sql.SQLException;

import wei.he.wo.xin.R;
import the.reason.why.activity.BookListActivity;
import the.reason.why.context.TocDbContext;
import the.reason.why.model.TocItem;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TocAdapter
    extends BaseAdapter
{
    private static final String TAG_NAME = TocAdapter.class.getSimpleName();
    private LayoutInflater mInflater;
    private Context context;
    public TocDbContext tocDbContext;

    public float mRatio = 1.0f;
    public int fontSize = 18;

    public void closeAllDb()
    {
        if (tocDbContext != null) {
            tocDbContext.close();
        }
    }

    /**
     * Constructor
     * 
     * @param context
     * @throws SQLException
     * @throws IOException
     */
    public TocAdapter(Context context) throws SQLException, IOException
    {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        tocDbContext = new TocDbContext(context);
        Log.d(TAG_NAME, "TocAdapter constructed successfully!");
    }

    @Override
    public int getCount()
    {
        try {
            return tocDbContext.dao.length();
        } catch (SQLException e) {
            Log.e(TAG_NAME, "Error: unable to get count of ToC items", e);
        }
        return 0;
    }

    @Override
    public TocItem getItem(int position)
    {
        try {
            Log.d(TAG_NAME,
                String.format("ToC#%d retrieved successfully", position));

            // convert from 0-based (here) -> 1-based (SQL) indexing
            return tocDbContext.dao.getById(position + 1);
        } catch (SQLException e) {
            Log.e(TAG_NAME,
                String.format("Error: unable to retrieve item#%d", position, e));
        }
        return null;
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        // setup view
        View newView = convertView;
        // if (newView == null) {
        newView = mInflater.inflate(R.layout.list_item_toc, parent, false);
        // newView.setBackgroundResource(R.layout.border);
        // }
        TextView txtTocItem = (TextView) newView.findViewById(R.id.listItem);

        // query DB for item
        TocItem thisItem = getItem(position);

        // populate TextView with data from DB
        Log.d(TAG_NAME,
            "getView(): " + thisItem.getId() + " - " + thisItem.getTitle());

        txtTocItem.setText(
            Html.fromHtml(String.format("%d %s", thisItem.getId(),
                thisItem.getTitle())), TextView.BufferType.SPANNABLE);
        txtTocItem.getPaint().setFakeBoldText(true);
        Log.d(TAG_NAME, "getView(): mRatio = " + mRatio + " / fontSize = "
                        + fontSize);
        txtTocItem.setTextSize(mRatio + fontSize);

        // attach event handler
        final int pos = position;
        txtTocItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG_NAME, "TOCItem#" + (pos + 1) + " was clicked!");
                Intent myIntent = new Intent(context, BookListActivity.class);
                // pass parameters onto next Activity
                // 1. first create the bundle and initialize it
                Bundle bundle = new Bundle();
                // 2. then add the parameters to bundle as required
                bundle.putString("TITLE", getItem(pos).getTitle());
                bundle.putInt("CHAPTER", pos + 1);
                // 3. add this bundle to the intent
                myIntent.putExtras(bundle);
                // now finally start next Activity
                v.getContext().startActivity(myIntent);
            }
        });

        return newView;
    }
}
