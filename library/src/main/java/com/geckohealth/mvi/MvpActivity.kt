package com.geckohealth.mvi

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

/**
 * Created by cgoldberg02 on 3/24/17.
 */
abstract class MvpActivity<Presenter : BasePresenter<View>, in View : BaseView> : AppCompatActivity() {
    val delegate: MvpDelegate<Presenter, View> by lazy { MvpDelegateImpl<Presenter, View>() }

    abstract fun supplyPresenter(): Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        delegate.onSaveInstanceState(outState)

        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()

        delegate.attachPresenter(this, supplyPresenter())
    }

    override fun onStop() {
        super.onStop()

        delegate.detachPresenter()
    }
}
