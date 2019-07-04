package com.anikinkirill.tinkoffsiriusmobile;

import android.app.Application;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;

/**
 * CREATED BY ANIKINKIRILL
 */

@Singleton
@Component(
        modules = {
                AndroidSupportInjectionModule.class
        }
)
public abstract class AppComponent implements AndroidInjector<BaseApplication> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);

        AppComponent build();
    }

}
