package com.sensel.driverapp.api;


import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ServiceFactory {

    private static int CONNECT_TIMEOUT_MILLIS = 10000;
    private static int READ_TIMEOUT_MILLIS = 10000;


    /**
     * Creates a retrofit service from an arbitrary class (clazz)
     *
     * @param clazz    Java interface of the retrofit service
     * @param endPoint REST endpoint url
     * @return retrofit service with defined endpoint
     */
    public static <T> T createRetrofitService(final Class<T> clazz, final String endPoint) throws NullPointerException {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        final OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        httpClient.addInterceptor(interceptor);
        OkHttpClient client = httpClient
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException, NullPointerException {
                        Request request = chain.request();
                        Response response = null;
                        boolean responseOK = false;
                        int tryCount = 0;

                        while (!responseOK && tryCount < 3) {
                            try {
                                response = chain.proceed(request);
                                responseOK = response.isSuccessful();
                            } catch (Exception e) {
                                Log.d("intercept", "Request is not successful - " + tryCount);
                            } finally {
                                tryCount++;
                            }
                        }

                        // otherwise just pass the original response on
                        return response;

                    }
                })
                .build();

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(endPoint)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        T service = retrofit.create(clazz);

        return service;
    }
}
