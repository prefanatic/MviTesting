package io.prefanatic.sample.list

import com.geckohealth.mvi.MviPresenter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Created by cgoldberg02 on 3/22/17.
 */
class FruitListPresenter(val interactor: FruitListInteractor) : com.geckohealth.mvi.MviPresenter<FruitListView, FruitListState>() {
    override fun bind() {
        val results = intent {
            searchIntent()
                    .switchMap { interactor.search(it) }
                    .observeOn(AndroidSchedulers.mainThread())
        }

        val load = intent {
            loadTestIntent()
                    .switchMap { interactor.load() }
                    .observeOn(AndroidSchedulers.mainThread())
        }

        val error = intent {
            errorTestIntent()
                    .switchMap { interactor.error() }
                    .observeOn(AndroidSchedulers.mainThread())
        }

        val allState = Observable.merge<FruitListState>(results, load, error)

        setViewStateObservable(allState)
    }
}