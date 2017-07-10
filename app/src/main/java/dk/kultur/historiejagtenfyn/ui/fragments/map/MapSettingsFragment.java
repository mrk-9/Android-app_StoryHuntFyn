package dk.kultur.historiejagtenfyn.ui.fragments.map;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import dk.kultur.historiejagtenfyn.R;
import dk.kultur.historiejagtenfyn.ui.util.SoundManager;
import dk.kultur.historiejagtenfyn.ui.util.UIUtils;

/**
 * Map settings fragment
 */
public class MapSettingsFragment extends Fragment {

    private static final String TAG = MapSettingsFragment.class.getSimpleName();


    public static MapSettingsFragment newInstance() {
        MapSettingsFragment fragment = new MapSettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_map_settings, container, false);
        TextView titleView = (TextView) layout.findViewById(R.id.title);
        titleView.setTypeface(UIUtils.getTypeFaceMarkerFelt(getActivity()));
        TextView btnBack = (TextView) layout.findViewById(R.id.btnBack);
        btnBack.setTypeface(UIUtils.getTypeFaceMarkerFelt(getActivity()));
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SoundManager.getInstance(getActivity()).playSound(SoundManager.SOUND_BOOK_PAGE);
                getActivity().onBackPressed();
            }
        });
        CheckBox btnAllRoutes = (CheckBox) layout.findViewById(R.id.btnAllRoutes); //indicates if routes should be drawn on map

        btnAllRoutes.setTypeface(UIUtils.getTypeFaceMarkerFelt(getActivity()));
        String activeRouteId = MapPreferences.getCurrentRoute(getActivity());
        if (activeRouteId == null) {
            btnAllRoutes.setEnabled(false);
        }

        btnAllRoutes.setChecked(MapPreferences.isShowRoutes(getActivity()));
        btnAllRoutes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MapPreferences.saveShowRoutes(getActivity(), isChecked);

            }
        });

        CheckBox btnPlaySounds = (CheckBox) layout.findViewById(R.id.btnPlaySounds);
        btnPlaySounds.setTypeface(UIUtils.getTypeFaceMarkerFelt(getActivity()));
        btnPlaySounds.setChecked(MapPreferences.isPlaySound(getActivity()));
        btnPlaySounds.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MapPreferences.savePlaySound(getActivity(), isChecked);
            }
        });

        return layout;
    }

    public String getLogTag() {
        return TAG;
    }



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        FlurryAgent.logEvent(getString(R.string.flurry_map_settings), true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        FlurryAgent.endTimedEvent(getString(R.string.flurry_map_settings));
    }


}
