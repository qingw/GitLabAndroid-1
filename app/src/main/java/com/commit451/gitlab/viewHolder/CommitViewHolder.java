package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.api.RepositoryCommit;
import com.commit451.gitlab.util.DateUtils;
import com.commit451.gitlab.util.ImageUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Shows a commit
 */
public class CommitViewHolder extends RecyclerView.ViewHolder {

    public static CommitViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_commit, parent, false);
        return new CommitViewHolder(view);
    }

    @Bind(R.id.commit_image) ImageView mImageView;
    @Bind(R.id.commit_message) TextView mMessageView;
    @Bind(R.id.commit_author) TextView mAuthorView;
    @Bind(R.id.commit_time) TextView mTimeView;

    public CommitViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(RepositoryCommit commit) {
        GitLabClient.getPicasso()
                .load(ImageUtil.getAvatarUrl(commit.getAuthorEmail(), itemView.getResources().getDimensionPixelSize(R.dimen.image_size)))
                .into(mImageView);

        mMessageView.setText(commit.getTitle());
        mAuthorView.setText(commit.getAuthorName());
        mTimeView.setText(DateUtils.getRelativeTimeSpanString(itemView.getContext(), commit.getCreatedAt()));
    }
}
