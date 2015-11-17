package me.sheimi.sgit.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import me.sheimi.android.activities.SheimiFragmentActivity;
import me.sheimi.sgit.R;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.repo.tasks.repo.GetGraphTask;
import me.sheimi.sgit.repo.tasks.repo.RepoOpTask;
import me.sheimi.sgit.repo.tasks.repo.StatusTask;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.Vector;

/**
 * Created by cecco on 17/11/15.
 */
public class GraphFragment extends RepoDetailFragment {

    private Repo mRepo;
    private TextView mStatus;
    private ProgressBar mLoadding;

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
        View v = inflater.inflate(R.layout.fragment_status, container, false);
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
        GetGraphTask task = new GetGraphTask(mRepo, new GetGraphTask.GetGraphCallback() {
            @Override
            public void postGraph(Vector<RevCommit> mResult) {
                String s ="";
                for(RevCommit r : mResult) {
                    s = s + r.getName() + "\n";
                }
                mStatus.setText(s);
                mLoadding.setVisibility(View.GONE);
                mStatus.setVisibility(View.VISIBLE);
            }
        });
        task.executeTask();
    }
}
