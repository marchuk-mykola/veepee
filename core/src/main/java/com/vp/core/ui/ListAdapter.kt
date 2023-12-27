package com.vp.core.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.vp.core.R
import com.vp.core.di.GlideApp
import com.vp.core.models.ListItem
import com.vp.core.ui.ListAdapter.OnItemClickListener

class ListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun interface OnItemClickListener {
        fun onItemClick(imdbID: String)
    }

    companion object {
        const val VIEW_TYPE_ITEM = 0
        const val VIEW_TYPE_FOOTER = 1
        private const val NO_IMAGE = "N/A"
    }

    private var listItems: List<ListItem> = emptyList()
    private var showFooter: Boolean = false
    private val EMPTY_ON_ITEM_CLICK_LISTENER: OnItemClickListener = OnItemClickListener { }
    private var onItemClickListener: OnItemClickListener = EMPTY_ON_ITEM_CLICK_LISTENER

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_FOOTER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.footer_loading, parent, false)
            FooterViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
            ListViewHolder(view, onItemClickListener)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_ITEM) {
            listItems.getOrNull(position)?.let { listItem ->
                if (listItem.poster != NO_IMAGE) {
                    (holder as ListViewHolder).bind(listItem)
                } else {
                    (holder as ListViewHolder).image.setImageResource(R.drawable.placeholder)
                }
            }
        }
    }

    override fun getItemCount(): Int = listItems.size + if (showFooter) 1 else 0

    override fun getItemViewType(position: Int): Int {
        return if (position >= listItems.size) VIEW_TYPE_FOOTER else VIEW_TYPE_ITEM
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener ?: EMPTY_ON_ITEM_CLICK_LISTENER
    }

    fun showFooter(show: Boolean) {
        showFooter = show
        notifyDataSetChanged()
    }

    fun setItems(listItems: List<ListItem>) {
        this.listItems = listItems
        notifyDataSetChanged()
    }

    fun clearItems() {
        this.listItems = emptyList()
        notifyDataSetChanged()
    }

    inner class ListViewHolder(itemView: View, private val onItemClickListener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val image: ImageView = itemView.findViewById(R.id.poster)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            listItems.getOrNull(bindingAdapterPosition)?.imdbID?.let { onItemClickListener.onItemClick(it) }
        }

        fun bind(listItem: ListItem) {
            listItem.poster.let { poster ->
                val density = image.resources.displayMetrics.density
                GlideApp
                    .with(image)
                    .load(poster)
                    .override((300 * density).toInt(), (600 * density).toInt())
                    .into(image)
            }
        }
    }

    class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}
