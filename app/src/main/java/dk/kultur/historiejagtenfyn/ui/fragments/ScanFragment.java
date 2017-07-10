package dk.kultur.historiejagtenfyn.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.moodstocks.android.AutoScannerSession;
import com.moodstocks.android.MoodstocksError;
import com.moodstocks.android.Result;
import com.moodstocks.android.Scanner;

import dk.kultur.historiejagtenfyn.R;
import dk.kultur.historiejagtenfyn.ui.activities.POIDetailsActivity;
import dk.kultur.historiejagtenfyn.ui.util.SoundManager;
import dk.kultur.historiejagtenfyn.ui.util.UIUtils;

public class ScanFragment extends AbsFragmentWithTabbar implements AutoScannerSession.Listener {
    public static final int FRAGMENT_ID = 4;

    private static final int TYPES = Result.Type.IMAGE | Result.Type.QRCODE | Result.Type.EAN13;

    private AutoScannerSession session = null;

    private boolean compatible = false;
    private Scanner scanner;

    private static Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getFragmentId() {
        return FRAGMENT_ID;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_scan;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //init fonts
        TextView headerText = (TextView) view.findViewById(R.id.headerText);
        final Typeface typeFaceMarkerFelt = UIUtils.getTypeFaceMarkerFelt(getActivity());
        headerText.setTypeface(typeFaceMarkerFelt);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!isRemoving() && !isDetached() && getActivity()!= null) {
                    initScanner();
                }
            }
        }, 500);


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (compatible && scanner != null) {
            try {
                if (session != null) {
                    session.stop();
                }
                session = null;
                scanner.close();
                scanner.destroy();
                scanner = null;
            } catch (MoodstocksError e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (compatible && scanner != null) {
            try {
                scanner.close();
                scanner.destroy();
                scanner = null;
            } catch (MoodstocksError e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (session != null) {
            if (!session.isListening()) {
                session.resume();
                session.start();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (session != null) {
            session.stop();
        }
    }


    @Override
    public void onCameraOpenFailed(Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onWarning(String debugMessage) {
        Log.d("Moodstocks SDK", "onWarning: " + debugMessage);
    }

    @Override
    public void onResult(Result result) {
        FlurryAgent.logEvent(getString(R.string.flurry_scan_found_popup));

        Intent intent = new Intent(getActivity(), POIDetailsActivity.class);
        intent.putExtra(POIDetailsActivity.EXTRA_POI_ID, result.getValue());
        startActivity(intent);
        getActivity().overridePendingTransition(0, R.anim.anim_shrink_to_left);
        SoundManager.getInstance(getActivity()).playSound(SoundManager.SOUND_BOOK_PAGE);

//        CustomPopupDialogFragment newFragment = CustomPopupDialogFragment.newInstance(result.getValue(), true);
//        newFragment.setOnDismissListener(mOnDialogDismissListener);
//        newFragment.setCancelable(false);
//        newFragment.show(getFragmentManager(), "dialog");
    }

    private void initScanner() {
        compatible = Scanner.isCompatible();
        if (compatible) {
            try {
                scanner = Scanner.get();
                String path = Scanner.pathFromFilesDir(getActivity(), "scanner.db");
                scanner.open(path, getString(R.string.moodstock_api_key), getString(R.string.moodstock_api_secret));
                SurfaceView preview = (SurfaceView) getView().findViewById(R.id.preview);

                session = new AutoScannerSession(getActivity(), Scanner.get(), this, preview);
                session.setResultTypes(TYPES);
                session.start();

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        View view = getView();
                        if (view != null) {
                            view.findViewById(R.id.loading_background).setVisibility(View.GONE);
                        }
                    }
                }, 500);
            } catch (MoodstocksError e) {
                e.printStackTrace();
            }
        }
    }
//
//    private OnDialogDismissListener mOnDialogDismissListener = new OnDialogDismissListener() {
//        @Override
//        public void onDialogDismissed() {
//            session.resume();
//        }
//    };

    public interface OnDialogDismissListener {
        void onDialogDismissed();
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        FlurryAgent.logEvent(getString(R.string.flurry_scan_fragment), true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        FlurryAgent.endTimedEvent(getString(R.string.flurry_scan_fragment));
    }
}
