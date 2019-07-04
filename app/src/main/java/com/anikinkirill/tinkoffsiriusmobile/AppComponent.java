package com.anikinkirill.tinkoffsiriusmobile;

import android.app.Application;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;

/**
 * CREATED BY ANIKINKIRILL
 */

@Singleton
@Component
public abstract class AppComponent implements AndroidInjector {
    
    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);

        Application build();
    }

}
