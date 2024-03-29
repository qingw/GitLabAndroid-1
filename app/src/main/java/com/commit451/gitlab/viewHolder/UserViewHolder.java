package com.commit451.gitlab.viewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.commit451.gitlab.R;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.api.UserBasic;
import com.commit451.gitlab.util.ImageUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Shows a single user
 */
public class UserViewHolder extends RecyclerView.ViewHolder {

    public static UserViewHolder inflate(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Bind(R.id.name) public TextView mUsernameView;
    @Bind(R.id.image) public ImageView mImageView;

    public UserViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bind(UserBasic user) {
        mUsernameView.setText(user.getUsername());
        GitLabClient.getPicasso()
                .load(ImageUtil.getAvatarUrl(user, itemView.getResources().getDimensionPixelSize(R.dimen.user_list_image_size)))
                .into(mImageView);
    }
}
