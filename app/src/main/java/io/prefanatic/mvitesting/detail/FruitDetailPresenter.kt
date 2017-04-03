package io.prefanatic.mvitesting.detail

import io.prefanatic.mvitesting.MviPresenterImpl
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Created by cgoldberg02 on 3/23/17.
 */
class FruitDetailPresenter(val interactor: FruitDetailInteractor) : MviPresenterImpl<FruitDetailView, FruitDetailState>() {
    override fun bind() {
        val searchIntent = interactor.load()
                .observeOn(AndroidSchedulers.mainThread())

        setViewStateObservable(searchIntent) {
            view!!.render(it)
        }
    }
}