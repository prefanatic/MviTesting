package io.prefanatic.sample.detail

import com.geckohealth.mvi.MviPresenter
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Created by cgoldberg02 on 3/23/17.
 */
class FruitDetailPresenter(val interactor: FruitDetailInteractor) : com.geckohealth.mvi.MviPresenter<FruitDetailView, FruitDetailState>() {
    override fun bind() {
        val searchIntent = interactor.load()
                .observeOn(AndroidSchedulers.mainThread())

        setViewStateObservable(searchIntent)
    }
}