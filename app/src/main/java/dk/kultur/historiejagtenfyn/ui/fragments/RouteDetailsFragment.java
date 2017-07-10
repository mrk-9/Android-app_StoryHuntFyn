package dk.kultur.historiejagtenfyn.ui.fragments;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import com.flurry.android.FlurryAgent;
import dk.kultur.historiejagtenfyn.R;
import dk.kultur.historiejagtenfyn.data.sql.HisContract.RouteContentEntry;
import dk.kultur.historiejagtenfyn.data.sql.HisContract.RouteEntry;
import dk.kultur.historiejagtenfyn.data.sql.HisContract.RouteJoinedRouteContentEntry;
import dk.kultur.historiejagtenfyn.ui.util.FileUtils;
import dk.kultur.historiejagtenfyn.ui.util.UIUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * Created by JustinasK on 2/25/2015.
 */
public class RouteDetailsFragment extends Fragment implements LoaderCallbacks<Cursor>, OnClickListener {
    private static final String LOG_TAG = RouteDetailsFragment.class.getSimpleName();

    private static final String KEY_OBJECT_ID = "key_object_id";
    private static final String KEY_ROUTE_NAME = "key_route_name";
    private static final int ROUTE_DETAIL_LOADER = 0;

    private static final String[] FORECAST_COLUMNS = {
            RouteJoinedRouteContentEntry.COLUMN_AVATAR,
            RouteJoinedRouteContentEntry.COLUMN_INFO,
            RouteContentEntry.TABLE_NAME + "." + RouteContentEntry.COLUMN_NAME,
    };

    public static final int COL_AVATAR_ID = 0;
    public static final int COL_INFO = 1;
    public static final int COL_NAME = 2;

    private static final String sSelect = RouteEntry.TABLE_NAME + "." + RouteEntry.COLUMN_OBJECT_ID + " = ?";
    private TextView headerText;
    private TextView txViewBack;
    private TextView txViewSeeMore;
    private WebView routeWebDetails;
    private ImageView avatarView;
    private View viewContainer;

    public static String getLogTag() {
        return LOG_TAG;
    }

    public static RouteDetailsFragment getInstance(String objectId, String routeName) {
        RouteDetailsFragment fragment = new RouteDetailsFragment();
        Bundle args = new Bundle();
        args.putString(KEY_OBJECT_ID, objectId);
        args.putString(KEY_ROUTE_NAME, routeName);
        fragment.setArguments(args);
        return fragment;
    }

    public RouteDetailsFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(KEY_OBJECT_ID)) {
            getLoaderManager().initLoader(ROUTE_DETAIL_LOADER, null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_routes_details, container, false);
        headerText = (TextView) rootView.findViewById(R.id.headerText);
        txViewBack = (TextView) rootView.findViewById(R.id.txViewBack);
        txViewSeeMore = (TextView) rootView.findViewById(R.id.txViewSeeMore);
        routeWebDetails = (WebView) rootView.findViewById(R.id.routeWebDetails);
        avatarView = (ImageView) rootView.findViewById(R.id.routeAvatar);
        viewContainer = rootView.findViewById(R.id.container);

        txViewBack.setOnClickListener(this);
        txViewSeeMore.setOnClickListener(this);

        final Typeface typeFaceMarkerFelt = UIUtils.getTypeFaceMarkerFelt(getActivity());
        headerText.setTypeface(typeFaceMarkerFelt);
        txViewBack.setTypeface(typeFaceMarkerFelt);
        txViewSeeMore.setTypeface(typeFaceMarkerFelt);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        headerText = null;
        txViewBack = null;
        txViewSeeMore = null;
        routeWebDetails = null;
        avatarView = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = RouteEntry.TABLE_NAME + "." + RouteEntry.COLUMN_OBJECT_ID + " LIMIT 1";
        return new CursorLoader(
                getActivity(),
                RouteJoinedRouteContentEntry.CONTENT_URI,
                FORECAST_COLUMNS,
                sSelect,
                new String[]{getArguments().getString(KEY_OBJECT_ID)},
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            data.getString(COL_NAME);
            String avatarId = data.getString(COL_AVATAR_ID);

            headerText.setText(data.getString(COL_NAME));
            WebSettings settings = routeWebDetails.getSettings();
            settings.setDefaultTextEncodingName("utf-8");
            String htmlString = data.getString(COL_INFO);
            String base64 = Base64.encodeToString(htmlString.getBytes(), Base64.DEFAULT);
            routeWebDetails.setBackgroundColor(0);
            routeWebDetails.loadData(base64, "text/html; charset=utf-8", "base64");
            final int height = routeWebDetails.getHeight();
            Log.d(getLogTag(), "Height" + height);

            try {
                final InputStream fileStream = FileUtils.getFileStream(getActivity(), avatarId, FileUtils.AVATAR_1);
                final Options options = UIUtils.getOptions(fileStream);
                fileStream.close();

                //final int i = UIUtils.calculateInSampleSize(options, 0, (int) (viewContainer.getHeight() * 1));

                options.inJustDecodeBounds = false;
                //options.inSampleSize = i;

                Bitmap bitmap = BitmapFactory.decodeStream(FileUtils.getFileStream(getActivity(), avatarId, FileUtils.AVATAR_1), null, options);
                avatarView.setImageBitmap(bitmap);
            } catch (IOException e) {
                Log.e(LOG_TAG, "No avatar found", e);
            }
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txViewBack:
                ((GoBackFromDetailsListener) getActivity()).onGoBackFromDetails();
                break;
            case R.id.txViewSeeMore:
                ((MapRouteSelectedListener) getActivity()).onMapRouteSelected(getArguments().getString(KEY_OBJECT_ID));
                break;
        }
    }

    public interface MapRouteSelectedListener {
        void onMapRouteSelected(String routeId);
    }

    public interface GoBackFromDetailsListener {
        void onGoBackFromDetails();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Map<String, String> props = new HashMap<>();
        if (getArguments() != null) {
            String routeName = getArguments().getString(KEY_ROUTE_NAME);
            props.put(getString(R.string.flurry_property_route_name), routeName);
        }
        FlurryAgent.logEvent(getString(R.string.flurry_route_fragment), props, true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        FlurryAgent.endTimedEvent(getString(R.string.flurry_route_fragment));
    }
}
