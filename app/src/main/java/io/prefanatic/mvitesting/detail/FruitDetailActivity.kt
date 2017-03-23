package io.prefanatic.mvitesting.detail

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.bumptech.glide.Glide
import io.prefanatic.mvitesting.R
import kotlinx.android.synthetic.main.activity_fruit.*

class FruitDetailActivity : AppCompatActivity(), FruitDetailView {

    val presenter: FruitDetailPresenter by lazy { FruitDetailPresenter(FruitDetailInteractor()) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fruit)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        presenter.attach(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        presenter.detach()
    }

    override fun render(state: FruitDetailState) {
        if (state is Result) {
            Glide.with(this)
                    .load(state.imageUrl)
                    .into(image_view)
        }
    }
}
