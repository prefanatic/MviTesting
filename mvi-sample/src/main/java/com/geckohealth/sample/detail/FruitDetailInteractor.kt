package io.prefanatic.sample.detail

import io.prefanatic.sample.FruitDataSource
import io.reactivex.Observable

/**
 * Created by cgoldberg02 on 3/23/17.
 */
typealias StateObservable = Observable<out FruitDetailState>

class FruitDetailInteractor {
    fun load(): StateObservable {
        return FruitDataSource.getSearchItems("apple fruit")
                .toObservable()
                .map { Result(it.items[0].link) }
                .cast(FruitDetailState::class.java)
                .startWith(Loading())
                .onErrorReturn(::Error)
    }
}