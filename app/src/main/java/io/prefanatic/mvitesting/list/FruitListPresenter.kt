package io.prefanatic.mvitesting.list

import com.geckohealth.Diary
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Created by cgoldberg02 on 3/22/17.
 */
class FruitListPresenter(val interactor: FruitListInteractor) : MviPresenter<FruitListView, FruitListState>() {
    override fun bind() {
        val results = view!!.searchIntent()
                .switchMap { interactor.search(it) }
                .observeOn(AndroidSchedulers.mainThread())

        val load = view!!.loadTestIntent()
                .switchMap { interactor.load() }
                .observeOn(AndroidSchedulers.mainThread())

        val error = view!!.errorTestIntent()
                .switchMap { interactor.error() }
                .observeOn(AndroidSchedulers.mainThread())

        val allState = Observable.merge<FruitListState>(results, load, error)

        subscribeViewState(allState) {
            view!!.render(it)
        }
    }
}

abstract class MviPresenter<View, State> : BasePresenter<View>() {

    abstract fun bind()

    override fun attach(view: View) {
        super.attach(view)

        bind()
    }

    fun subscribeViewState(observable: Observable<out State>, block: (State) -> Unit) {
        observable.subscribe({
            Diary.d("State -> $it")
            block.invoke(it)
        }, { throwable ->
            Diary.e(throwable, "Wowzers")
        })
    }
}

//AIzaSyDXy8wLt3NgwvqQsNjCdRmiGSaDt3Uaxec

open class BasePresenter<View> {
    var view: View? = null

    open fun attach(view: View) {
        this.view = view
    }

    fun detach() {
        this.view = null
    }
}