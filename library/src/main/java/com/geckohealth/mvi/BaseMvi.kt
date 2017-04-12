package com.geckohealth.mvi

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by cgoldberg02 on 3/23/17.
 */

abstract class MviPresenter<View : MviView<State>, State> : BasePresenterImpl<View>() {
    /**
     * Flag that indicates the presenter has bound to the view.  Binding to the view should
     * only ever happen once, in that the implementor of this presenter need not care about the
     * lifecycle changes behind the scenes.
     */
    var hasBoundToView = false

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
     * Pairs of Observables with Subjects that face the top-side detachment layer.
     *
     * These pairs are iterated through upon attach to re-subscribe with their associated Subject.
     * The disposables created from this subscription are collected into [intentDisposable] and
     * disposed of during a detach event.
     */
    var intentForwardingPairs: MutableList<IntentForwardingPair<View, *>> = ArrayList()

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
     * Disposable used to carry the subscription between the internal presenter chain to the View State.
     */
    var viewStateDisposable: Disposable? = null

    /**
     * Disposable used to carry the subscription between the [viewStateSubject] and the View's render.
     */
    var viewStateForwardingDisposable: Disposable? = null

    override fun attachView(view: View) {
        super.attachView(view)

        intentDisposable = CompositeDisposable()

        if (!hasBoundToView) {
            bind()
            hasBoundToView = true
        }

        attachIntents()
        attachViewState()
    }

    override fun detachView() {
        super.detachView()

        viewStateForwardingDisposable?.dispose()
        intentDisposable.dispose()
    }

    private fun attachViewState() {
        val observer = ViewStateObserver(view!!)
        viewStateForwardingDisposable = viewStateSubject.subscribeWith(observer)
    }

    /**
     * Attaches all known intent forwarding pairs.
     */
    private fun attachIntents() {
        intentDisposable = CompositeDisposable()

        intentForwardingPairs.forEach { (binder, subject) ->
            val observer = RelayObserver(subject as Subject<Any>)

            Chapter.d("Attaching forwarding pair to subject $subject")
            intentDisposable += binder.bind(view!!)
                    .subscribeWith(observer)
        }
    }

    /**
     * Binds the Presenter to the View.
     *
     * This method is only called once, upon first attachment to the view.  Implementors are expected
     * to combine their Observable Intents using [intent], and then passing the reduced state result
     * to [setViewStateObservable]
     */
    abstract fun bind()

    /**
     * Sets the overall state Observable.
     *
     * The state Observable is coupled with a state Subject, which faces the bottom-most detachment
     * layer.
     *
     * TODO - Revisit the comment below when we determine destruction cases.
     * The disposable generated from coupling the Observable and Subject is never disposed of.
     */
    fun setViewStateObservable(observable: Observable<out State>) {
        if (viewStateDisposable != null) {
            throw RuntimeException("A ViewState Observable already has been subscribed." +
                    "This method should only ever run once.")
        }

        // Lock in the observable to the view state subject.
        // TODO: Do we ever unsubscribe from this?  This is internal - detach / attach does not confuse this portion.
        val observer = RelayObserver(viewStateSubject)
        viewStateDisposable = observable.subscribeWith(observer)
    }

    /**
     * Adds an Observable as an View Intent.
     *
     * This allows the framework to handle subscribing and disposing of this Observable
     * appropriately, upon lifecycle and detach events.
     *
     * Observables bound with the [IntentBinder] are added to a list of possible forwarding
     * pairs.  These forwarding pairs are iterated through upon attach to rebind them to their
     * associated Subject, through a [RelayObserver].  On view detach, the disposables produced
     * from both the [RelayObserver] and the Subject are disposed of.
     */
    fun <T> intent(binder: IntentBinder<View, T>): Observable<T> {
        val subject = PublishSubject.create<T>()
        intentForwardingPairs.add(IntentForwardingPair(
                binder = binder,
                subject = subject
        ))

        return subject
    }

    /**
     * Syntactic sugar variant of [intent].
     */
    fun <T> intent(binder: View.() -> Observable<T>): Observable<T> {
        return intent(object : IntentBinder<View, T> {
            override fun bind(view: View): Observable<T> = binder.invoke(view)
        })
    }
}

/**
 * An Observer used to join the rendered state (as directed by the implementor presenter) and the
 * View's [MviView.render] together.
 *
 * This Observer only allows onNext emissions.  Any onComplete or onError will throw a RuntimeException.
 *
 * This Observer is recreated upon every attach event.
 */
class ViewStateObserver<out View : MviView<T>, T>(val view: View) : DisposableObserver<T>() {
    override fun onComplete() {
        throw RuntimeException("The View State must never complete.")
    }

    override fun onNext(t: T) {
        Chapter.d("State -> $t")

        view.render(t)
    }

    override fun onError(e: Throwable?) {
        throw RuntimeException("Errors sent through to the View State must be caught and handled with " +
                "onErrorResume or another applicable catcher.", e)
    }
}

/**
 * A "loose" Observer, used to join an Intent and a Subject together.
 *
 * This Observer does not emit out onError or onComplete to the Subject.
 *
 * TODO: The nature of this Observer does not mean much to the implementor,
 * in terms of what is allowed and what isn't.
 */
class RelayObserver<T>(val subject: Subject<T>) : DisposableObserver<T>() {
    override fun onNext(t: T) {
        subject.onNext(t)
    }

    override fun onError(e: Throwable?) {
        // Nope.
    }

    override fun onComplete() {
        // Nope.
    }
}

/**
 * Reference pair between an [IntentBinder] and a Subject.
 *
 * This forwarding pair is analyzed during view attach events.  The binder within this pair
 * is executed to determine the [Observable] used to emit values into the pair's subject.
 *
 * Upon attach, the Observable from the binder is subscribed to the subject.
 *
 * The subject in this forwarding pair will always be subscribed to, by the presenter's reducer.
 */
data class IntentForwardingPair<in View, T>(
        val binder: IntentBinder<View, T>,
        val subject: PublishSubject<T>
)

/**
 * Interface used to construct an Intent Observable from a View
 */
interface IntentBinder<in View, T> {
    fun bind(view: View): Observable<T>
}

interface ViewStateBinder<in View> {
    fun bind(view: View)
}

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