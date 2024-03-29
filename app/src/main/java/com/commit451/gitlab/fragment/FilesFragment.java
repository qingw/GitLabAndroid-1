package com.commit451.gitlab.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.commit451.gitlab.GitLabApp;
import com.commit451.gitlab.R;
import com.commit451.gitlab.activity.ProjectActivity;
import com.commit451.gitlab.adapter.BreadcrumbAdapter;
import com.commit451.gitlab.adapter.FilesAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.event.ProjectReloadEvent;
import com.commit451.gitlab.model.api.Project;
import com.commit451.gitlab.model.api.RepositoryTreeObject;
import com.commit451.gitlab.util.IntentUtil;
import com.commit451.gitlab.util.NavigationManager;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

public class FilesFragment extends BaseFragment {

    public static FilesFragment newInstance() {
        return new FilesFragment();
    }

    @Bind(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.list) RecyclerView mFilesListView;
    @Bind(R.id.breadcrumb) RecyclerView mBreadcrumbListView;
    @Bind(R.id.message_text) TextView mMessageView;

    private Project mProject;
    private String mBranchName;
    private EventReceiver mEventReceiver;
    private FilesAdapter mFilesAdapter;
    private BreadcrumbAdapter mBreadcrumbAdapter;
    private String mCurrentPath = "";

    private class FilesCallback implements Callback<List<RepositoryTreeObject>> {
        private final String mNewPath;

        public FilesCallback(String newPath) {
            this.mNewPath = newPath;
        }

        @Override
        public void onResponse(Response<List<RepositoryTreeObject>> response, Retrofit retrofit) {
            if (getView() == null) {
                return;
            }

            mSwipeRefreshLayout.setRefreshing(false);

            if (!response.isSuccess()) {
                Timber.e("Files response was not a success: %d", response.code());
                mMessageView.setVisibility(View.VISIBLE);
                mMessageView.setText(R.string.connection_error_files);
                mFilesAdapter.setData(null);

                mCurrentPath = mNewPath;
                updateBreadcrumbs();
                return;
            }

            if (!response.body().isEmpty()) {
                mMessageView.setVisibility(View.GONE);
            } else {
                Timber.d("No files found");
                mMessageView.setVisibility(View.VISIBLE);
                mMessageView.setText(R.string.no_files_found);
            }

            mFilesAdapter.setData(response.body());
            mFilesListView.scrollToPosition(0);
            mCurrentPath = mNewPath;
            updateBreadcrumbs();
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);

            if (getView() == null) {
                return;
            }

            mSwipeRefreshLayout.setRefreshing(false);

            mMessageView.setVisibility(View.VISIBLE);
            mMessageView.setText(R.string.connection_error);
            mFilesAdapter.setData(null);

            mCurrentPath = mNewPath;
            updateBreadcrumbs();
        }
    }

    private final FilesAdapter.Listener mFilesAdapterListener = new FilesAdapter.Listener() {
        @Override
        public void onFolderClicked(RepositoryTreeObject treeItem) {
            loadData(mCurrentPath + treeItem.getName() + "/");
        }

        @Override
        public void onFileClicked(RepositoryTreeObject treeItem) {
            String path = mCurrentPath + treeItem.getName();
            NavigationManager.navigateToFile(getActivity(), mProject.getId(), path, mBranchName);
        }

        @Override
        public void onCopyClicked(RepositoryTreeObject treeItem) {
            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);

            // Creates a new text clip to put on the clipboard
            ClipData clip = ClipData.newPlainText(treeItem.getName(), treeItem.getUrl(mProject, mBranchName, mCurrentPath).toString());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getActivity(), R.string.copied_to_clipboard, Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        public void onShareClicked(RepositoryTreeObject treeItem){
            IntentUtil.share(getView(), treeItem.getUrl(mProject, mBranchName, mCurrentPath));
        }

        @Override
        public void onOpenInBrowserClicked(RepositoryTreeObject treeItem){
            IntentUtil.openPage(getView(), treeItem.getUrl(mProject, mBranchName, mCurrentPath));
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_files, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mEventReceiver = new EventReceiver();
        GitLabApp.bus().register(mEventReceiver);

        mFilesAdapter = new FilesAdapter(mFilesAdapterListener);
        mFilesListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFilesListView.setAdapter(mFilesAdapter);

        mBreadcrumbAdapter = new BreadcrumbAdapter();
        mBreadcrumbListView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        mBreadcrumbListView.setAdapter(mBreadcrumbAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        if (getActivity() instanceof ProjectActivity) {
            mProject = ((ProjectActivity) getActivity()).getProject();
            mBranchName = ((ProjectActivity) getActivity()).getBranchName();
            loadData("");
        } else {
            throw new IllegalStateException("Incorrect parent activity");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        GitLabApp.bus().unregister(mEventReceiver);
    }

    @Override
    protected void loadData() {
        loadData(mCurrentPath);
    }

    public void loadData(String newPath) {
        if (getView() == null) {
            return;
        }

        if (mProject == null || TextUtils.isEmpty(mBranchName)) {
            mSwipeRefreshLayout.setRefreshing(false);
            return;
        }

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }
        });

        GitLabClient.instance().getTree(mProject.getId(), mBranchName, newPath).enqueue(new FilesCallback(newPath));
    }

    @Override
    public boolean onBackPressed() {
        if (mBreadcrumbAdapter.getItemCount() > 1) {
            BreadcrumbAdapter.Breadcrumb breadcrumb = mBreadcrumbAdapter.getValueAt(mBreadcrumbAdapter.getItemCount() - 2);
            if (breadcrumb != null && breadcrumb.getListener() != null) {
                breadcrumb.getListener().onClick();
                return true;
            }
        }

        return false;
    }

    private void updateBreadcrumbs() {
        List<BreadcrumbAdapter.Breadcrumb> breadcrumbs = new ArrayList<>();
        breadcrumbs.add(new BreadcrumbAdapter.Breadcrumb(mProject.getName(), new BreadcrumbAdapter.Listener() {
            @Override
            public void onClick() {
                loadData("");
            }
        }));

        String newPath = "";

        String[] segments = mCurrentPath.split("/");
        for (String segment : segments) {
            if (segment.isEmpty()) {
                continue;
            }

            newPath += segment + "/";

            final String finalPath = newPath;
            breadcrumbs.add(new BreadcrumbAdapter.Breadcrumb(segment, new BreadcrumbAdapter.Listener() {
                @Override
                public void onClick() {
                    loadData(finalPath);
                }
            }));
        }

        mBreadcrumbAdapter.setData(breadcrumbs);
        mBreadcrumbListView.scrollToPosition(mBreadcrumbAdapter.getItemCount() - 1);
    }

    private class EventReceiver {
        @Subscribe
        public void onProjectReload(ProjectReloadEvent event) {
            mProject = event.mProject;
            mBranchName = event.mBranchName;

            loadData("");
        }
    }
}