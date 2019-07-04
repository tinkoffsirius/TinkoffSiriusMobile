package com.anikinkirill.tinkoffsiriusmobile;

import com.anikinkirill.tinkoffsiriusmobile.dependencyinjection.app.DaggerAppComponent;

import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;

/**
 * CREATED BY ANIKINKIRILL
 */

public class BaseApplication extends DaggerApplication {
    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        return DaggerAppComponent.builder().application(this).build();
    }
}
