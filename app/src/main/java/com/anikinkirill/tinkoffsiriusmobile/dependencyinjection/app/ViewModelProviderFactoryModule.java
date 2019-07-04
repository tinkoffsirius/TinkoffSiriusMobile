package com.anikinkirill.tinkoffsiriusmobile.dependencyinjection.app;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.anikinkirill.tinkoffsiriusmobile.viewmodel.ViewModelProviderFactory;

import dagger.Binds;
import dagger.Module;

/**
 * CREATED BY ANIKINKIRILL
 */

@Module
public abstract class ViewModelProviderFactoryModule {

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(ViewModelProviderFactory providerFactory);

}
