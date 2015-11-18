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

                int startYear=0;
                int curYear=0;
                int week=0;
                int count=0;
                for(int i=mResult.size()-1; i>=0; i--) {
                    Date d = mResult.get(i).getAuthorIdent().getWhen();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(d);

                    curYear = cal.get(Calendar.YEAR);
                    int day = cal.get(Calendar.DAY_OF_YEAR);
                    int curWeek = cal.get(Calendar.WEEK_OF_YEAR);
                    if(day > 365-7 && curWeek == 1) {
                        curYear++;
                    }
                    if(day < 7 && curWeek == 5) {
                        curYear--;
                    }

                    if(startYear == 0)
                        startYear = curYear;

                    curWeek += 52*(curYear-startYear);

                    if(week == curWeek) {
                        count++;
                    } else {
                        if(week >0) {
                            val.add(new Entry(count, week));
                        }

                        for (int j = week + 1; j < curWeek; j++)
                            val.add(new Entry(0, j));

                        week = curWeek;
                        count = 0;
                    }
                }
                val.add(new Entry(count, week));

                LineDataSet setComp1 = new LineDataSet(val, startYear + " - " + curYear);
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
                    int id = e.getXIndex();
                    xVals.add(id%52+1 +"/"+(startYear+(id/52)));
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
