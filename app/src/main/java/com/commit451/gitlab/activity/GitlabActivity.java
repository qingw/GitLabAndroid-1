package com.commit451.gitlab.activity;

import android.app.Activity;
import android.os.Bundle;

import com.commit451.gitlab.BuildConfig;
import com.commit451.gitlab.data.Prefs;
import com.commit451.gitlab.model.Account;
import com.commit451.gitlab.util.NavigationManager;

import java.util.List;

import timber.log.Timber;

/**
 * This activity acts as switching platform for the application directing the user to the appropriate
 * activity based on their logged in state
 */
public class GitlabActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int savedVersion = Prefs.getSavedVersion(this);
        if (savedVersion != -1 && savedVersion < BuildConfig.VERSION_CODE) {
            Timber.d("Performing upgrade");
            performUpgrade(savedVersion, BuildConfig.VERSION_CODE);
            Prefs.setSavedVersion(this);
        }
        List<Account> accounts = Account.getAccounts(this);
        if(accounts.isEmpty()) {
            NavigationManager.navigateToLogin(this);
        } else {
            NavigationManager.navigateToProjects(this);
        }

        // Always finish this activity
        finish();
    }

    /**
     * Perform an upgrade from one version to another. This should only be one time upgrade things
     */
    private void performUpgrade(int previousVersion, int currentVersion) {

    }
}
