package io.prefanatic.mvitesting.persistence

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.prefanatic.mvitesting.DependencyInjection
import io.prefanatic.mvitesting.MvpActivity
import io.prefanatic.mvitesting.R
import kotlinx.android.synthetic.main.activity_persistence.*
import kotlinx.android.synthetic.main.item_persistence.view.*

class PersistenceActivity : MvpActivity<PersistencePresenter, PersistenceView>(), PersistenceView {
    private lateinit var adapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_persistence)

        adapter = Adapter()
        recycler_view.adapter = adapter
        recycler_view.layoutManager = LinearLayoutManager(this)

        fab.setOnClickListener {
            supplyPresenter().toggleEmissions()
        }
    }

    override fun supplyPresenter(): PersistencePresenter = DependencyInjection.persistencePresenter

    override fun render(state: PersistenceState) {
        when (state) {
            is Result -> {
                adapter.data = state.items
            }
        }
    }
}

class Adapter : RecyclerView.Adapter<Adapter.ViewHolder>() {
    var data: List<Int> = ArrayList()
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder!!.itemView.title.text = data[position].toString()
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent?.context)
                .inflate(R.layout.item_persistence, parent, false)
        val viewHolder = ViewHolder(view)

        return viewHolder
    }


    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView)
}