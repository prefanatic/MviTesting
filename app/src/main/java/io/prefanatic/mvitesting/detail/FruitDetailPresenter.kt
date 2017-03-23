package io.prefanatic.mvitesting.detail

import io.prefanatic.mvitesting.list.MviPresenter
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Created by cgoldberg02 on 3/23/17.
 */
class FruitDetailPresenter(val interactor: FruitDetailInteractor) : MviPresenter<FruitDetailView, FruitDetailState>() {
    override fun bind() {
        val searchIntent = interactor.load()
                .observeOn(AndroidSchedulers.mainThread())

        subscribeViewState(searchIntent) {
            view!!.render(it)
        }
    }
}