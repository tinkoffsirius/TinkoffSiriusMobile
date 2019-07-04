package com.anikinkirill.tinkoffsiriusmobile.dependencyinjection.app;

import com.anikinkirill.tinkoffsiriusmobile.dependencyinjection.auth.AuthViewModelModule;
import com.anikinkirill.tinkoffsiriusmobile.ui.auth.SignInActivity;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
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
    abstract SignInActivity contributeSignInActivity();


}
