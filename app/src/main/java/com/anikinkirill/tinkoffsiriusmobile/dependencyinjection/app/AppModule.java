package com.anikinkirill.tinkoffsiriusmobile.dependencyinjection.app;

import com.anikinkirill.tinkoffsiriusmobile.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * CREATED BY ANIKINKIRILL
 */

@Module
public class AppModule {
    
    private static Gson gson = new GsonBuilder().create();

    @Provides
    static Retrofit provideRetrofitInstance(){
        return new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

}
