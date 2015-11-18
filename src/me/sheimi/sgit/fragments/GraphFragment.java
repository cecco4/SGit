package me.sheimi.sgit.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import me.sheimi.android.activities.SheimiFragmentActivity;
import me.sheimi.sgit.R;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.repo.tasks.repo.GetGraphTask;
import me.sheimi.sgit.repo.tasks.repo.RepoOpTask;
import me.sheimi.sgit.repo.tasks.repo.StatusTask;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

/**
 * Created by cecco on 17/11/15.
 */
public class GraphFragment extends RepoDetailFragment {

    private Repo mRepo;
    private TextView mStatus;
    private ProgressBar mLoadding;
    private LineChart chart;

    public static GraphFragment newInstance(Repo mRepo) {
        GraphFragment fragment = new GraphFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Repo.TAG, mRepo);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_graph, container, false);
        getRawActivity().setGraphFragment(this);

        Bundle bundle = getArguments();
        mRepo = (Repo) bundle.getSerializable(Repo.TAG);
        if (mRepo == null && savedInstanceState != null) {
            mRepo = (Repo) savedInstanceState.getSerializable(Repo.TAG);
        }
        if (mRepo == null) {
            return v;
        }
        mStatus = (TextView) v.findViewById(R.id.status);
        mLoadding = (ProgressBar) v.findViewById(R.id.loading);
        chart = (LineChart) v.findViewById(R.id.chart);
        reset();
        return v;
    }


    @Override
    public SheimiFragmentActivity.OnBackClickListener getOnBackClickListener() {
        return null;
    }

    @Override
    public void reset() {
        if (mLoadding == null || mStatus == null)
            return;
        mLoadding.setVisibility(View.VISIBLE);
        mStatus.setVisibility(View.GONE);
        chart.setVisibility(View.GONE);
        GetGraphTask task = new GetGraphTask(mRepo, new GetGraphTask.GetGraphCallback() {
            @Override
            public void postGraph(Vector<RevCommit> mResult) {
                mStatus.setText("Commits per weeks: ");

                ArrayList<Entry> val = new ArrayList<Entry>();

                int week=1;
                int count=0;
                for(int i=mResult.size()-1; i>=0; i--) {
                    Date d = mResult.get(i).getAuthorIdent().getWhen();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(d);

                    int curWeek = cal.get(Calendar.WEEK_OF_YEAR);

                    if(week == curWeek) {
                        count++;
                    } else {
                        val.add(new Entry(count, week));

                        for (int j=week+1; j<curWeek; j++)
                            val.add(new Entry(0, j));

                        week = curWeek;
                        count = 0;
                    }
                }
                val.add(new Entry(count, week));

                for (int j=week+1; j<=52; j++)
                    val.add(new Entry(0, j));

                LineDataSet setComp1 = new LineDataSet(val, "2015");
                setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
                setComp1.setDrawCubic(true);
                setComp1.setDrawCircles(false);
                setComp1.setDrawCircleHole(false);
                setComp1.setDrawFilled(true);
                setComp1.setDrawValues(false);

                ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
                dataSets.add(setComp1);

                ArrayList<String> xVals = new ArrayList<String>();
                for(Entry e : val) {
                    xVals.add("Week "+e.getXIndex());
                }

                LineData data = new LineData(xVals, dataSets);
                chart.setData(data);
                chart.invalidate();

                chart.setVisibility(View.VISIBLE);
                mLoadding.setVisibility(View.GONE);
                mStatus.setVisibility(View.VISIBLE);
            }
        });
        task.executeTask();
    }
}
