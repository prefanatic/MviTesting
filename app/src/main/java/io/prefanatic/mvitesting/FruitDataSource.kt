package io.prefanatic.mvitesting

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by cgoldberg02 on 3/23/17.
 */
object FruitDataSource {
    //https://www.googleapis.com/customsearch/v1?key=&cx=&searchType=image&q='apple fruit'

    val service: FruitService

    const val API_KEY = "AIzaSyDXy8wLt3NgwvqQsNjCdRmiGSaDt3Uaxec"
    const val SEARCH_ENGINE = "003948808906223293774:bxoj09dlxs4"

    init {
        val interceptor = HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BODY)

        val okhttp = OkHttpClient.Builder()
                .addNetworkInterceptor(interceptor)
                .build()

        val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create())
                .baseUrl("https://www.googleapis.com/")
                .client(okhttp)
                .build()

        service = retrofit.create(FruitService::class.java)
    }

    fun getSearchItems(query: String): Single<SearchResults> {
        return service.search(API_KEY, SEARCH_ENGINE, "image", query)
                .subscribeOn(Schedulers.io())
    }

    interface FruitService {
        @GET("customsearch/v1")
        fun search(
                @Query("key") key: String,
                @Query("cx") searchEngine: String,
                @Query("searchType") searchType: String,
                @Query("q") query: String
        ): Single<SearchResults>
    }

    data class SearchResults(
            val items: List<SearchItem>
    )

    data class SearchItem(
            val title: String,
            val link: String
    )
}