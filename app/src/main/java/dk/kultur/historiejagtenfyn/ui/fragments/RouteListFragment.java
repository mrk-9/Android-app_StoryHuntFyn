package dk.kultur.historiejagtenfyn.ui.fragments;


import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import dk.kultur.historiejagtenfyn.R;
import dk.kultur.historiejagtenfyn.data.sql.Columns.ParseColumns;
import dk.kultur.historiejagtenfyn.data.sql.Columns.RouteColumns.RouteContentColumns;
import dk.kultur.historiejagtenfyn.data.sql.HisContract.IconEntry;
import dk.kultur.historiejagtenfyn.data.sql.HisContract.RouteContentEntry;
import dk.kultur.historiejagtenfyn.data.sql.HisContract.RouteEntry;
import dk.kultur.historiejagtenfyn.data.sql.HisContract.RouteJoinedIconJoinedContentEntry;
import dk.kultur.historiejagtenfyn.ui.Views.CursorRecyclerViewAdapter;
import dk.kultur.historiejagtenfyn.ui.Views.SimpleDividerItemDecoration;
import dk.kultur.historiejagtenfyn.ui.util.FileUtils;
import dk.kultur.historiejagtenfyn.ui.util.UIUtils;
import java.io.IOException;
import java.io.InputStream;

/**
 * Display routes
 */
public class RouteListFragment extends AbsFragmentWithTabbar implements LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = RouteListFragment.class.getSimpleName();
    public static final int FRAGMENT_ID = 1;
    private static final int ROUTE_LIST_LOADER = 10;
    private static final String SELECTED_KEY = "selected_key";

    private RoutesAdapter routeAdapter;

    private static final String[] ROUTE_COLUMNS = {
            RouteEntry.TABLE_NAME + "." + RouteEntry._ID,
            RouteEntry.TABLE_NAME + "." + RouteEntry.COLUMN_OBJECT_ID,
            IconEntry.TABLE_NAME + "." + ParseColumns.COLUMN_OBJECT_ID,
            RouteContentEntry.TABLE_NAME + "." + RouteContentEntry.COLUMN_NAME,
    };

    public static final int COL_ID = 0;
    public static final int COL_ROUTE_ID = 1;
    public static final int COL_ICON_ID = 2;
    public static final int COL_ROUTE_CONTENT_NAME = 3;

    private RecyclerView recyclerView;
    private int position;

    public RouteListFragment() {
    }

    @Override
    protected int getFragmentId() {
        return FRAGMENT_ID;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_routes;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(ROUTE_LIST_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity().getApplicationContext()));
        routeAdapter = new RoutesAdapter(getActivity(), null);
        recyclerView.setAdapter(routeAdapter);
        TextView headerText = (TextView) view.findViewById(R.id.headerText);
        final Typeface typeFaceMarkerFelt = UIUtils.getTypeFaceMarkerFelt(getActivity());
        headerText.setTypeface(typeFaceMarkerFelt);

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            position = savedInstanceState.getInt(SELECTED_KEY);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (position != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, position);
        }
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        recyclerView = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = null;

        switch (id) {
            case ROUTE_LIST_LOADER:
                Uri routeJoinedIcons = RouteJoinedIconJoinedContentEntry.CONTENT_URI;
                loader = new CursorLoader(
                        getActivity(),
                        routeJoinedIcons,
                        ROUTE_COLUMNS,
                        null,
                        null,
                        RouteEntry.TABLE_NAME + "." + RouteContentColumns.COLUMN_NAME + " COLLATE LOCALIZED");
                break;
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        routeAdapter.swapCursor(data);
        if (position != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            recyclerView.smoothScrollToPosition(position);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        routeAdapter.swapCursor(null);
    }

    private class RoutesAdapter extends CursorRecyclerViewAdapter<RouteViewHolder> {

        RoutesAdapter(Context context, Cursor cursor) {
            super(context, cursor);
        }

        @Override
        public void onBindViewHolder(final RouteViewHolder viewHolder, Cursor cursor) {
            final String routeName = cursor.getString(COL_ROUTE_CONTENT_NAME);
            viewHolder.textView.setText(routeName);
            viewHolder.objectRouteId = cursor.getString(COL_ROUTE_ID);
            viewHolder.objectIconId = cursor.getString(COL_ICON_ID);
            try {
                final InputStream fileStream = FileUtils.getFileStream(getActivity(), viewHolder.objectIconId, FileUtils.ICON_RETINA);
                Bitmap bitmap = BitmapFactory.decodeStream(fileStream);
                final Bitmap scaledBitmap = UIUtils.createScaledBitmap(bitmap, getActivity());
                viewHolder.imageView.setImageBitmap(scaledBitmap);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Uncaught exception", e);
            }
            viewHolder.mainView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String routeId = viewHolder.objectRouteId;
                    ((RouteSelectedListener) getActivity()).onRouteSelected(routeId, routeName);
                }
            });
        }

        @Override
        public RouteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.route_item, parent, false);
            return new RouteViewHolder(view);
        }

    }

    private class RouteViewHolder extends RecyclerView.ViewHolder {
        public View mainView;
        public TextView textView;
        public ImageView imageView;
        public String objectRouteId;
        public String objectIconId;

        public RouteViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.header_text);
            textView.setTypeface(UIUtils.getTypeFaceMarkerFelt(getActivity()));
            imageView = (ImageView) itemView.findViewById(R.id.route_icon);
            mainView = itemView;
        }
    }

    public String getLogTag() {
        return LOG_TAG;
    }

    public interface RouteSelectedListener {
        void onRouteSelected(String routeId, String routeName);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        FlurryAgent.logEvent(getString(R.string.flurry_route_list_fragment), true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        FlurryAgent.endTimedEvent(getString(R.string.flurry_route_list_fragment));
    }
}
