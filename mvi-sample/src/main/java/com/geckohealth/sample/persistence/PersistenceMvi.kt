package io.prefanatic.sample.persistence

import com.geckohealth.mvi.BasePresenter
import com.geckohealth.mvi.MviPresenter
import com.geckohealth.mvi.MviView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by cgoldberg02 on 3/24/17.
 */

class PersistencePresenterImpl : com.geckohealth.mvi.MviPresenter<PersistenceView, PersistenceState>(), PersistencePresenter {
    var shouldEmit = AtomicBoolean(true)

    override fun bind() {
        val foreverItems: MutableList<Int> = mutableListOf()

        val foreverObservable = Observable.interval(1, TimeUnit.SECONDS)
                .filter { shouldEmit.get() }
                .doOnNext { foreverItems.add(0, foreverItems.size + 1) }
                .map { PersistenceState(foreverItems) }
                .observeOn(AndroidSchedulers.mainThread())

        val foreverIntent = intent {
            foreverObservable
        }

        setViewStateObservable(foreverIntent)
    }

    override fun toggleEmissions() {
        shouldEmit.set(!shouldEmit.get())
    }
}

interface PersistencePresenter : com.geckohealth.mvi.BasePresenter<PersistenceView> {
    fun toggleEmissions()
}

interface PersistenceView : com.geckohealth.mvi.MviView<PersistenceState>

data class PersistenceState(
        val items: List<Int> = ArrayList()
)

