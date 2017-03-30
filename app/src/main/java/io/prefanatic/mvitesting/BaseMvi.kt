package io.prefanatic.mvitesting

import com.geckohealth.Diary
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.internal.operators.observable.ObservableRange
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by cgoldberg02 on 3/23/17.
 */

abstract class MviPresenterImpl<View : BaseView, State> : BasePresenterImpl<View>() {
    var hasBoundToView = false

    var viewStateObservable: Observable<out State>? = null
    var viewStateBlock: ((State) -> Unit)? = null

    /**
     * Behavior Subject that is responsible for forwarding view state events out to the view.
     *
     * This subject is bound by the Observable assigned in [setViewStateObservable]
     *
     * Upon detach, this subject will be disposed.
     *
     * Upon attach, this subject will be subscribed.
     */
    val viewStateSubject: BehaviorSubject<State> = BehaviorSubject.create()

    /**
     *
     */
    var viewIntents: List<PublishSubject<*>> = ArrayList()

    /**
     *
     */
    var intentObservables: MutableList<Observable<*>> = ArrayList()

    /**
     * Composite Disposable used to carry the combined disposables of all currently-subscribed
     * view intents.
     *
     * These disposables are active subscriptions to intents subjected to a merge into the view
     * state.
     *
     * Upon detach, this disposable will be disposed.
     *
     * Upon attach, a new disposable will be created, and pending view intents will be resubscribed,
     * and assigned to this disposable.
     */
    var intentDisposable: CompositeDisposable = CompositeDisposable()

    /**
     * Disposable used to carry the view state subscription.
     *
     * Upon detach, this disposable will be disposed.
     *
     * Upon attach, a new disposable will be created, and the Observable given by [viewStateObservable]
     * will be subscribed to.
     */
    var viewStateDisposable: Disposable? = null

    abstract fun bind()

    override fun attachView(view: View) {
        super.attachView(view)

        intentDisposable = CompositeDisposable()

        if (!hasBoundToView) {
            bind()
            hasBoundToView = true
        }

        subscribeViewState()
    }

    override fun detachView() {
        super.detachView()

        viewStateDisposable?.dispose()
        intentDisposable.dispose()
    }

    private fun subscribeViewState() {
        assert(viewStateObservable != null) {
            "Cannot subscribe to a null ViewState Observable"
        }
        assert(viewStateBlock != null) {
            "Cannot subscribe when there is no ViewState invocation block."
        }

        viewStateDisposable = viewStateObservable?.subscribe({
            Diary.d("State -> $it")
            viewStateBlock?.invoke(it)
        }, { throwable ->
            Diary.e(throwable, "Wowzers")
        })
    }

    /**
     * Attaches all known intents to the view.
     */
    private fun attachIntents() {
        intentObservables.forEach {
            it.subscribe()
        }
    }

    fun setViewStateObservable(observable: Observable<out State>, block: (State) -> Unit) {
        if (viewStateObservable != null) {
            throw RuntimeException("A ViewState Observable already has been subscribed." +
                    "This method should only ever run once.")
        }

        this.viewStateObservable = observable
        this.viewStateBlock = block
    }

    fun <T> asBehavior(observable: Observable<T>): Observable<T> = BehaviorSubject.create<T>()
            .apply {
                observable
                        .doOnSubscribe { intentDisposable.add(it) }
                        .doOnDispose { Chapter.d("Hello I've been disposed.") }
                        .subscribe(this)
            }

    fun <T> intent(block: View.() -> Observable<T>): Observable<T> {
        val observable = block.invoke(view!!)
        intentObservables.add(observable)

        return observable
    }
}

/**
 * Used to hold a relationship pair between an intent observable, and it's subject.
 */
data class IntentForwardingPair<T>(
        val observable: Observable<T>,
        val subject: PublishSubject<T>
)

//AIzaSyDXy8wLt3NgwvqQsNjCdRmiGSaDt3Uaxec

open class BasePresenterImpl<View : BaseView> : BasePresenter<View> {
    override val uuid: UUID by lazy { UUID.randomUUID() }

    var view: View? by WeakProperty<View?>()


    override fun attachView(view: View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }
}

class WeakProperty<T> : ReadWriteProperty<Any?, T?> {
    private var weak: WeakReference<T>? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T? = weak?.get()

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        if (value == null) {
            weak?.clear()
            weak = null
            return
        }

        weak = WeakReference(value)
    }

}

interface BasePresenter<in View : BaseView> {
    val uuid: UUID

    fun attachView(view: View)
    fun detachView()
}


sealed class MviState
data class Error(val throwable: Throwable) : MviState()
class Loading() : MviState()

interface MviView<in State> : BaseView {
    fun render(state: State)
}

interface BaseView