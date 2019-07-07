package com.anikinkirill.tinkoffsiriusmobile.dependencyinjection.app;

import com.anikinkirill.tinkoffsiriusmobile.dependencyinjection.auth.AuthViewModelModule;
import com.anikinkirill.tinkoffsiriusmobile.dependencyinjection.map.MapViewModelModule;
import com.anikinkirill.tinkoffsiriusmobile.ui.auth.AuthActivity;
import com.anikinkirill.tinkoffsiriusmobile.ui.map.MapActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * CREATED BY ANIKINKIRILL
 */

@Module
public abstract class ActivityBuildersModule {

    @ContributesAndroidInjector(
            modules = {
                    AuthViewModelModule.class
            }
    )
    abstract AuthActivity contributeSignInActivity();

    @ContributesAndroidInjector(
            modules = {
                    MapViewModelModule.class
            }
    )
    abstract MapActivity contributeMapActivity();
}
