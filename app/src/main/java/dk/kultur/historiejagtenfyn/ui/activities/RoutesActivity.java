package dk.kultur.historiejagtenfyn.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import dk.kultur.historiejagtenfyn.R;
import dk.kultur.historiejagtenfyn.data.models.DataModelListener;
import dk.kultur.historiejagtenfyn.data.models.RoutesDataModel;
import dk.kultur.historiejagtenfyn.data.parse.models.RouteModelHis;
import dk.kultur.historiejagtenfyn.ui.Views.TabbView;
import java.util.ArrayList;
import java.util.List;

public class RoutesActivity extends AbsActivity implements TabbView.OnTabClickedListener, DataModelListener {
    public static final int MY_ACTIVITY_ID = 1;

    private RoutesDataModel mModel;
    private RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mModel = new RoutesDataModel(this);
        setContentView(R.layout.activity_routes);
        TabbView tabbView = (TabbView) findViewById(R.id.tabbar);
        tabbView.setActiveTabId(MY_ACTIVITY_ID);
        tabbView.setOnTabClickListener(this);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final TextView viewById = (TextView) findViewById(R.id.header_text);
        viewById.setText("Header");
    }

    @Override
    protected void onStart() {
        mModel.registerDataModelListener(this);
        mModel.load();
        super.onStart();
    }

    @Override
    public void onDestroy() {
        mModel.unregisterDataModelListener(this);
        mModel.unload();
        recyclerView = null;
        super.onDestroy();
    }


    @Override
    public void onLoadStarted() {

    }

    @Override
    public void onLoadFinished() {
        ArrayList<RouteModelHis> language = mModel.getRoutes();
        Log.d("List of Routes", language.toString());
        recyclerView.setAdapter(new RoutesAdapter(language));
    }

    @Override
    public void onTabClicked(int id, boolean animate) {
        if (id != MY_ACTIVITY_ID) {
            Intent i = null;
            switch (id) {
                case MapActivity.MY_ACTIVITY_ID:
                    i = new Intent(RoutesActivity.this, MapActivity.class);
                    break;
                case ARViewActivity.MY_ACTIVITY_ID:
                    i = new Intent(RoutesActivity.this, ARViewActivity.class);
                    break;
                case ScanActivity.MY_ACTIVITY_ID:
                    i = new Intent(RoutesActivity.this, ScanActivity.class);
                    break;
            }
            if (i != null) {
                startActivityWithPageCurlAnimation(i);
            }
        }
    }

    class RoutesAdapter extends RecyclerView.Adapter<TextViewHolder> {
        private List<RouteModelHis> labels;

        public RoutesAdapter(ArrayList<RouteModelHis>  data) {
            labels = data;
        }

        @Override
        public TextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
            return new TextViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final TextViewHolder holder, final int position) {
            final RouteModelHis label = labels.get(position);
            holder.textView.setText(label.getString("name"));
            holder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(holder.textView.getContext(), label.getString("name"), Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return labels.size();
        }
    }

    class TextViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public TextViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.header_text);
        }
    }


}
