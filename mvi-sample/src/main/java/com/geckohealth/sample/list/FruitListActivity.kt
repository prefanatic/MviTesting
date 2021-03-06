package io.prefanatic.sample.list

import android.content.Intent
import android.os.Bundle
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.transition.TransitionManager
import android.view.*
import com.bumptech.glide.Glide
import com.jakewharton.rxbinding2.support.v7.widget.RxToolbar
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxSearchView
import io.prefanatic.sample.Fruit
import com.geckohealth.mvi.MviView
import io.prefanatic.sample.MvpActivity
import io.prefanatic.sample.R
import io.prefanatic.sample.detail.FruitDetailActivity
import io.prefanatic.sample.persistence.PersistenceActivity
import io.reactivex.Observable
import io.reactivex.observables.ConnectableObservable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_fruit_list.*
import kotlinx.android.synthetic.main.item_fruit.view.*

class FruitListActivity : MvpActivity<FruitListPresenter, FruitListView>(), FruitListView {

    lateinit var adapter: Adapter

    val toolbarClickObservable: Observable<MenuItem> by lazy {
        RxToolbar.itemClicks(toolbar)
                .publish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fruit_list)
        setSupportActionBar(toolbar)

        adapter = Adapter()
        recycler_view.adapter = adapter
        recycler_view.layoutManager = LinearLayoutManager(this)

        adapter.clickSubject.subscribe {
            startActivity(Intent(this, FruitDetailActivity::class.java))
        }

        toolbarClickObservable.filter { it.itemId == R.id.action_persistence }
                .subscribe { startActivity(Intent(this, PersistenceActivity::class.java)) }

        (toolbarClickObservable as ConnectableObservable<MenuItem>).connect()
    }

    override fun supplyPresenter(): FruitListPresenter {
        return FruitListPresenter(FruitListInteractor())
    }

    override fun render(state: FruitListState) {
        TransitionManager.beginDelayedTransition(coordinator)

        progress_bar.visibility = when (state) {
            is Loading -> View.VISIBLE
            else -> View.GONE
        }
        error_text.visibility = when (state) {
            is Error -> View.VISIBLE
            else -> View.GONE
        }
        recycler_view.visibility = when (state) {
            is Result -> View.VISIBLE
            else -> View.GONE
        }
        image_view.visibility = when (state) {
            is NetworkResult -> View.VISIBLE
            else -> View.GONE
        }

        if (state is Result) {
            adapter.data = state.items
        }

        if (state is NetworkResult) {
            Glide.with(this)
                    .load(state.imageUrl)
                    .into(image_view)
        }
    }

    override fun searchIntent(): Observable<String> = RxSearchView.queryTextChanges(search_view)
            .map { it.toString() }

    override fun loadTestIntent(): Observable<Boolean> = toolbarClickObservable
            .filter { it.itemId == R.id.action_set_loading }
            .map { true }

    override fun errorTestIntent(): Observable<Boolean> = toolbarClickObservable
            .filter { it.itemId == R.id.action_error }
            .map { true }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_fruit_list, menu)
        return true
    }

}

interface FruitListView : com.geckohealth.mvi.MviView<FruitListState> {
    fun searchIntent(): Observable<String>

    fun loadTestIntent(): Observable<Boolean>

    fun errorTestIntent(): Observable<Boolean>
}

class Adapter : RecyclerView.Adapter<Adapter.ViewHolder>() {
    val clickSubject: PublishSubject<Fruit> = PublishSubject.create<Fruit>()

    var data: List<Fruit> = ArrayList()
        set(value) {
            DiffUtil.calculateDiff(FruitDiffUtilCallback(field, value))
                    .dispatchUpdatesTo(this)

            field = value
        }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.item_fruit, parent, false)
        val viewHolder = ViewHolder(view)

        RxView.clicks(viewHolder.itemView)
                .map { data[viewHolder.adapterPosition] }
                .takeUntil(RxView.detaches(parent as View))
                .subscribe(clickSubject)

        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val fruit = data[position]

        holder!!.itemView.title.text = fruit.name
    }

    override fun getItemCount(): Int = data.size

    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView)
}

class FruitDiffUtilCallback(val old: List<Fruit>, val new: List<Fruit>) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = old.size

    override fun getNewListSize(): Int = new.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return old[oldItemPosition].uuid == new[newItemPosition].uuid
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return old[oldItemPosition] == new[newItemPosition]
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }
}
