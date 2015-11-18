package me.sheimi.sgit.repo.tasks.repo;

import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.exception.StopTaskException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by cecco on 17/11/15.
 */
public class GetGraphTask extends RepoOpTask {

    private GetGraphCallback mCallback;
    private Vector<RevCommit> mResult;

    public static interface GetGraphCallback {
        public void postGraph(Vector<RevCommit> mResult);
    }


    public GetGraphTask(Repo repo) {
        super(repo);
    }

    public void executeTask() {
        execute();
    }

    public GetGraphTask(Repo repo, GetGraphCallback callback) {
        super(repo);
        mCallback = callback;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        //TODO: Calculate graphs
        try {
            Iterable<RevCommit> list = mRepo.getGit().log().call();
            mResult = new Vector<RevCommit>();
            for (RevCommit commit : list) {
                mResult.add(commit);
            }
        } catch (GitAPIException e) {
            e.printStackTrace();
        } catch (StopTaskException e) {
            e.printStackTrace();
        }
        return true;
    }

    protected void onPostExecute(Boolean isSuccess) {
        super.onPostExecute(isSuccess);
        if (mCallback != null) {
            mCallback.postGraph(mResult);
        }
    }
}
