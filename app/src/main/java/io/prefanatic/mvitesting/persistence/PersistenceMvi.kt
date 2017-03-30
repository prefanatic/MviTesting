package io.prefanatic.mvitesting.persistence

import io.prefanatic.mvitesting.BasePresenter
import io.prefanatic.mvitesting.MviPresenterImpl
import io.prefanatic.mvitesting.MviView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by cgoldberg02 on 3/24/17.
 */

class PersistencePresenterImpl : MviPresenterImpl<PersistenceView, PersistenceState>(), PersistencePresenter {
    var shouldEmit = AtomicBoolean(true)

    override fun bind() {
        val foreverItems: MutableList<Int> = mutableListOf()

        val foreverObservable = Observable.interval(1, TimeUnit.SECONDS)
                .filter { shouldEmit.get() }
                .doOnNext { foreverItems.add(0, foreverItems.size + 1) }
                .map { Result(foreverItems) }
                .observeOn(AndroidSchedulers.mainThread())

        val foreverIntent = intent { foreverObservable }

        setViewStateObservable(asBehavior(foreverIntent)) {
            view!!.render(it)
        }
    }

    override fun toggleEmissions() {
        shouldEmit.set(!shouldEmit.get())
    }
}

interface PersistencePresenter : BasePresenter<PersistenceView> {
    fun toggleEmissions()
}

interface PersistenceView : MviView<PersistenceState>

sealed class PersistenceState
data class Result(
        val items: List<Int>
) : PersistenceState()