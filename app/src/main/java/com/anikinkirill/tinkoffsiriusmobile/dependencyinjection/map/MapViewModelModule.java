package com.anikinkirill.tinkoffsiriusmobile.dependencyinjection.map;

import androidx.lifecycle.ViewModel;

import com.anikinkirill.tinkoffsiriusmobile.ui.map.MapViewModel;
import com.anikinkirill.tinkoffsiriusmobile.viewmodel.ViewModelKey;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

/**
 * CREATED BY ANIKINKIRILL
 */

@Module
public abstract class MapViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(MapViewModel.class)
    abstract ViewModel bindMapViewModel(MapViewModel viewModel);

}
