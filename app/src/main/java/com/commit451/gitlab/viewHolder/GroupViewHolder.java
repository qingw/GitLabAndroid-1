package com.commit451.gitlab.viewHolder;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.api.Group;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * View associated with a group
 */
public class GroupViewHolder extends RecyclerView.ViewHolder{

    public static GroupViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new GroupViewHolder(view);
    }

    @Bind(R.id.image) public ImageView mImageView;
    @Bind(R.id.name) public TextView mNameView;

    public GroupViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(Group group) {
        mNameView.setText(group.getName());

        if (group.getAvatarUrl() != null && !group.getAvatarUrl().equals(Uri.EMPTY)) {
            GitLabClient.getPicasso()
                    .load(group.getAvatarUrl())
                    .into(mImageView);
        }
    }
}
