package com.commit451.gitlab.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.commit451.gitlab.R;
import com.commit451.gitlab.model.api.UserBasic;
import com.commit451.gitlab.viewHolder.UserViewHolder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Adapter for a list of users
 * Created by John on 9/28/15.
 */
public class UsersAdapter extends RecyclerView.Adapter<UserViewHolder>  {

    public interface Listener {
        void onUserClicked(UserBasic user, UserViewHolder userViewHolder);
    }
    private Listener mListener;
    private ArrayList<UserBasic> mData;

    private final View.OnClickListener mItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag(R.id.list_position);
            UserViewHolder holder = (UserViewHolder) v.getTag(R.id.list_view_holder);
            mListener.onUserClicked(getUser(position), holder);
        }
    };

    public UsersAdapter(Listener listener) {
        mListener = listener;
        mData = new ArrayList<>();
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        UserViewHolder holder = UserViewHolder.inflate(parent);
        holder.itemView.setOnClickListener(mItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(UserViewHolder userViewHolder, int position) {
        userViewHolder.bind(mData.get(position));
        userViewHolder.itemView.setTag(R.id.list_position, position);
        userViewHolder.itemView.setTag(R.id.list_view_holder, userViewHolder);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    private UserBasic getUser(int position) {
        return mData.get(position);
    }

    public void setData(Collection<UserBasic> users) {
        mData.clear();
        addData(users);
    }

    public void addData(Collection<UserBasic> users) {
        if (users != null) {
            mData.addAll(users);
        }
        notifyDataSetChanged();
    }

    public void clearData() {
        mData.clear();
        notifyDataSetChanged();
    }
}
