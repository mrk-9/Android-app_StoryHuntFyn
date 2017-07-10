package dk.kultur.historiejagtenfyn.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.Nullable;

import dk.kultur.historiejagtenfyn.R;
import dk.kultur.historiejagtenfyn.ui.Views.TabbView;

/**
 * Created by Lina on 2015.02.06
 */
public abstract class AbsFragmentWithTabbar extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(getLayoutId(), container, false);
        TabbView tabbView = (TabbView) layout.findViewById(R.id.tabbar);
        tabbView.setActiveTabId(getFragmentId());
        tabbView.setOnTabClickListener((TabbView.OnTabClickedListener) getActivity());
        return layout;

    }
//
//    @Override
//    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        AbsActivityFullScreen a = (AbsActivityFullScreen) getActivity();
//        if (a != null) {
//            a.clearCurl();
//        }
//    }

    protected abstract int getFragmentId();

    protected abstract int getLayoutId();
}
