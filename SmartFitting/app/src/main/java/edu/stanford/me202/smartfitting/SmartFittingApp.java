package edu.stanford.me202.smartfitting;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by czhang on 5/18/17.
 */

public class SmartFittingApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize realm once for all activities and services
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name(Realm.DEFAULT_REALM_NAME)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
    }
}
