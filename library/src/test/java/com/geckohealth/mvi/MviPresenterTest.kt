package com.geckohealth.mvi

import io.reactivex.Observable
import io.reactivex.schedulers.TestScheduler
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * Created by cgoldberg02 on 4/11/17.
 */
typealias TestView = MviView<MviPresenterTest.TestState>
typealias TestPresenter = MviPresenter<TestView, MviPresenterTest.TestState>

class MviPresenterTest {
    class TestState(val number: Long)

    @Before
    fun setUp() {

    }

    @After
    fun tearDown() {

    }

    @Test
    fun testBindIsOnlyCalledOnce() {
        // bind() should only be called once, upon the first attachment of the view.
        // We would expect detaching and reattaching would not issue another bind().
        var bindCount = 0
        val presenter = presenter {
            bindCount++
        }
        val view = view()

        presenter.attachView(view)

        assertTrue("MviPresenter.attachView() failed to mark that the view has been bound.",
                presenter.hasBoundToView)

        presenter.attachView(view)

        assertEquals("MviPresenter.bind() has been called more than once.", 1, bindCount)
    }

    @Test
    fun testIntentObservableSurvivesAttachDetach() {
        // We expect Observables (assigned as Intents) to stop emitting values into
        // the stream after a detach - and then start emitting again after the attach.
        val scheduler = TestScheduler()
        val presenter = presenter {
            val intent = intent {
                Observable.interval(1, TimeUnit.SECONDS, scheduler)
                        .map(::TestState)
            }

            setViewStateObservable(intent)
        }
        var stateEmissions = -1
        val view = view {
            stateEmissions++
        }

        presenter.attachView(view)
        assertEquals("There should be no emissions until the scheduler is triggered.", -1, stateEmissions)

        scheduler.advanceTimeTo(1, TimeUnit.SECONDS)
        assertEquals("There should be an emission now that the scheduler has triggered.", 0, stateEmissions)

        presenter.detachView()
        scheduler.advanceTimeTo(2, TimeUnit.SECONDS)
        assertEquals("We shouldn't see any emissions after a detach.", 0, stateEmissions)

        presenter.attachView(view)
        assertEquals("There should be an emission after the reattach.", 1, stateEmissions)
    }

    @Test
    fun testInternalObservableStillEmitsThroughAttachDetach() {
        // We expect Observables (NOT marked as Intent Observables) to continue to emit values
        // within the presenter, through attach and detach events.
        val scheduler = TestScheduler()
        var presenterStateValue = -1
        val presenter = presenter {
            val observable = Observable.interval(1, TimeUnit.SECONDS, scheduler)
                    .doOnNext { presenterStateValue = it.toInt() }
                    .map(::TestState)

            setViewStateObservable(observable)
        }

        var viewStateValue = -1
        var stateEmissions = 0
        val view = view {
            viewStateValue = it.number.toInt()
            stateEmissions++
        }

        presenter.attachView(view)
        assertEquals("There should be no emissions until the scheduler is triggered.",
                -1, viewStateValue)

        scheduler.advanceTimeTo(1, TimeUnit.SECONDS)
        assertEquals("There should be one emission, due to the advancing scheduler.",
                0, viewStateValue)

        assertEquals("Both the Presenter's state value and View's state value should be equal.",
                presenterStateValue, viewStateValue)
        presenter.detachView()

        scheduler.advanceTimeTo(2, TimeUnit.SECONDS)
        assertEquals("The Presenter's state value should still increment when detached.",
                presenterStateValue, 1)
        assertEquals("The View's state value should not have incremented when detached.",
                0, viewStateValue)

        presenter.attachView(view)
        assertEquals("The View should now have the same value as the Presenter, upon reattaching.",
                presenterStateValue, viewStateValue)

        scheduler.advanceTimeTo(3, TimeUnit.SECONDS)
        assertEquals("The View and Presenter should emit in sync, upon reattaching.",
                presenterStateValue, viewStateValue)
    }

    private fun presenter(): TestPresenter = presenter {}

    private inline fun presenter(crossinline bind: TestPresenter.() -> Unit): TestPresenter {
        return object : MviPresenter<TestView, TestState>() {
            override fun bind() {
                bind.invoke(this)
            }
        }
    }

    private fun view(): TestView = view {}

    private inline fun view(crossinline render: TestView.(TestState) -> Unit): TestView {
        return object : TestView {
            override fun render(state: TestState) {
                render.invoke(this, state)
            }
        }
    }
}