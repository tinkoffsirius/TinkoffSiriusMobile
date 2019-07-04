package com.anikinkirill.tinkoffsiriusmobile.dependencyinjection.auth;

import androidx.lifecycle.ViewModel;

import com.anikinkirill.tinkoffsiriusmobile.ui.auth.SignInViewModel;
import com.anikinkirill.tinkoffsiriusmobile.viewmodel.ViewModelKey;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

/**
 * CREATED BY ANIKINKIRILL
 */

@Module
public abstract class AuthViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(SignInViewModel.class)
    abstract ViewModel bindSignInViewModel(SignInViewModel viewModel);

}
