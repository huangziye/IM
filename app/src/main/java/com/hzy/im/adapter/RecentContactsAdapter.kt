package com.hzy.im.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hzy.im.R
import com.netease.nimlib.sdk.msg.model.RecentContact
import java.text.SimpleDateFormat

/**
 *
 * @author: ziye_huang
 * @date: 2019/5/16
 */
class RecentContactsAdapter(val context: Context, val recentContactsList: MutableList<RecentContact>) :
    RecyclerView.Adapter<RecentContactsAdapter.RecentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentViewHolder {
        return RecentViewHolder(LayoutInflater.from(context).inflate(R.layout.item_recent_contacts, parent, false))
    }

    override fun getItemCount(): Int {
        return recentContactsList.size
    }

    override fun onBindViewHolder(holder: RecentViewHolder, position: Int) {
        val contact = recentContactsList[position]
        holder.nameTextView.text = contact.fromNick
        holder.timeTextView.text = SimpleDateFormat("yyyy-MM-dd").format(contact.time)
        holder.contentTextView.text = contact.content
        holder.llRoot.setOnClickListener { v -> listener?.setOnItemClickListener(v, position) }
        setUnreadCount(holder,contact.unreadCount)
    }

    private fun setUnreadCount(holder: RecentViewHolder,unreadCount: Int) {
        if (unreadCount > 0) {
            holder.unreadCountTextView.visibility = View.VISIBLE
            holder.unreadCountTextView.text = if(unreadCount > 99) "99" else unreadCount.toString()
        } else {
            holder.unreadCountTextView.visibility = View.GONE
        }
    }

    class RecentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val llRoot = itemView.findViewById<LinearLayout>(R.id.llRoot)
        val portraitImageView = itemView.findViewById<ImageView>(R.id.portraitImageView)
        val unreadCountTextView = itemView.findViewById<TextView>(R.id.unreadCountTextView)
        val nameTextView = itemView.findViewById<TextView>(R.id.nameTextView)
        val timeTextView = itemView.findViewById<TextView>(R.id.timeTextView)
        val promptTextView = itemView.findViewById<TextView>(R.id.promptTextView)
        val statusImageView = itemView.findViewById<ImageView>(R.id.statusImageView)
        val contentTextView = itemView.findViewById<TextView>(R.id.contentTextView)
        val slient = itemView.findViewById<ImageView>(R.id.slient)
    }

    private var listener: OnRecentItemClickListener? = null

    fun setOnItemClickListener(listener: OnRecentItemClickListener) {
        this.listener = listener
    }

    interface OnRecentItemClickListener {
        fun setOnItemClickListener(view: View, position: Int)
    }
}