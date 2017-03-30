package io.prefanatic.mvitesting.list

import io.prefanatic.mvitesting.FruitDataSource
import io.prefanatic.mvitesting.Fruits
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import java.util.concurrent.TimeUnit

/**
 * Created by cgoldberg02 on 3/22/17.
 */
typealias StateObservable = Observable<out FruitListState>

class FruitListInteractor {

    fun search(content: String): StateObservable {
        return Observable.fromIterable(Fruits.fruits)
                .doOnNext { if (content == "error") throw RuntimeException(";)") }
                .filter { it.name.contains(content, ignoreCase = true) }
                .toList()
                .toObservable()
                .map(::Result)
                .cast(FruitListState::class.java)
                .startWith(Loading())
                .onErrorReturn(::Error)
    }

    fun error(): StateObservable {
        return Observable.timer(2, TimeUnit.SECONDS)
                .doOnNext { throw RuntimeException("hah") }
                .cast(FruitListState::class.java)
                .startWith(Loading())
                .onErrorReturn(::Error)
    }

    fun error2(): StateObservable {
        return Observable.just(Error(RuntimeException("Meh")))
    }

    fun load(): StateObservable {
        return FruitDataSource.getSearchItems("apple fruit")
                .toObservable()
                .map { NetworkResult(it.items[0].link) }
                .cast(FruitListState::class.java)
                .startWith(Loading())
                .onErrorReturn(::Error)
    }
}