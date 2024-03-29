package com.commit451.gitlab.activity;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.commit451.easel.Easel;
import com.commit451.gitlab.R;
import com.commit451.gitlab.adapter.GroupPagerAdapter;
import com.commit451.gitlab.api.GitLabClient;
import com.commit451.gitlab.model.api.Group;
import com.commit451.gitlab.model.api.GroupDetail;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.parceler.Parcels;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * See the things about the group
 * Created by John on 10/14/15.
 */
public class GroupActivity extends BaseActivity {

    private static final String KEY_GROUP = "key_group";
    private static final String KEY_GROUP_ID = "key_group_id";

    public static Intent newInstance(Context context, Group group) {
        Intent intent = new Intent(context, GroupActivity.class);
        intent.putExtra(KEY_GROUP, Parcels.wrap(group));
        return intent;
    }

    public static Intent newInstance(Context context, long groupId) {
        Intent intent = new Intent(context, GroupActivity.class);
        intent.putExtra(KEY_GROUP_ID, groupId);
        return intent;
    }

    @Bind(R.id.root) View mRoot;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbarLayout;
    @Bind(R.id.viewpager) ViewPager mViewPager;
    @Bind(R.id.tabs) TabLayout mTabLayout;
    @Bind(R.id.backdrop) ImageView mBackdrop;

    private final Target mImageLoadTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mBackdrop.setImageBitmap(bitmap);
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                public void onGenerated(Palette p) {
                    bindPalette(p);
                }
            });
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {}

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {}
    };

    private final Callback<GroupDetail> mGroupCallback = new Callback<GroupDetail>() {
        @Override
        public void onResponse(Response<GroupDetail> response, Retrofit retrofit) {
            if (!response.isSuccess()) {
                showError();
                return;
            }
            bind(response.body());
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.e(t, null);
            showError();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        ButterKnife.bind(this);

        mToolbar.setNavigationIcon(R.drawable.ic_back_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if (getIntent().hasExtra(KEY_GROUP)) {
            Group group = Parcels.unwrap(getIntent().getParcelableExtra(KEY_GROUP));
            bind(group);
        } else {
            long groupId = getIntent().getLongExtra(KEY_GROUP_ID, -1);
            GitLabClient.instance().getGroup(groupId).enqueue(mGroupCallback);
        }
    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
    }

    private void bind(Group group) {
        GitLabClient.getPicasso()
                .load(group.getAvatarUrl())
                .into(mImageLoadTarget);

        mViewPager.setAdapter(new GroupPagerAdapter(this, getSupportFragmentManager(), group));
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void bindPalette(Palette palette) {
        int animationTime = 1000;
        int vibrantColor = palette.getVibrantColor(Easel.getThemeAttrColor(this, R.attr.colorPrimary));
        int darkerColor = Easel.getDarkerColor(vibrantColor);

        if (Build.VERSION.SDK_INT >= 21) {
            Easel.getNavigationBarColorAnimator(getWindow(), darkerColor)
                    .setDuration(animationTime)
                    .start();
        }

        ObjectAnimator.ofObject(mCollapsingToolbarLayout, "contentScrimColor", new ArgbEvaluator(),
                ((ColorDrawable) mCollapsingToolbarLayout.getContentScrim()).getColor(), vibrantColor)
                .setDuration(animationTime)
                .start();

        ObjectAnimator.ofObject(mCollapsingToolbarLayout, "statusBarScrimColor", new ArgbEvaluator(),
                ((ColorDrawable) mCollapsingToolbarLayout.getStatusBarScrim()).getColor(), darkerColor)
                .setDuration(animationTime)
                .start();

        ObjectAnimator.ofObject(mToolbar, "titleTextColor", new ArgbEvaluator(),
                Color.WHITE, palette.getDarkMutedColor(Color.BLACK))
                .setDuration(animationTime)
                .start();
    }

    private void showError() {
        Snackbar.make(mRoot, R.string.connection_error, Snackbar.LENGTH_SHORT)
                .show();
    }
}
