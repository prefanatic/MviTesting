package com.geckohealth.mvi

import android.os.Bundle

const val PRESENTER_ID = "presenter.id"

class MvpDelegateImpl<in Presenter : BasePresenter<View>, in View : BaseView>
    : MvpDelegate<Presenter, View> {

    private var presenter: Presenter? = null

    override fun attachPresenter(view: Any, presenter: Presenter) {
        view as? View ?: throw RuntimeException("Failed to attach." +
                "  This view did not inherit the proper View." +
                "  Did you forget to implement your view interface?")

        Chapter.d("Attach :: Presenter:${presenter.uuid}")
        this.presenter = presenter

        Chapter.d("Attach :: ${presenter.javaClass.simpleName} -> ${view.javaClass.simpleName}")
        presenter.attachView(view)
    }

    override fun detachPresenter() {

        Chapter.d("Detach :: ${presenter?.javaClass?.simpleName}")
        presenter?.detachView()
                ?: throw RuntimeException("Presenter was null when trying to detach. Ruh-roh.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val presenterId = savedInstanceState?.getString(PRESENTER_ID)

    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putString(PRESENTER_ID, presenter?.uuid.toString())
    }
}

interface MvpDelegate<in Presenter : BasePresenter<View>, in View : BaseView> {
    fun attachPresenter(view: Any, presenter: Presenter)
    fun detachPresenter()

    fun onCreate(savedInstanceState: Bundle?)
    fun onSaveInstanceState(outState: Bundle?)
}