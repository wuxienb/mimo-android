package com.carboncode.mimo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView

class ChatAdapter : ListAdapter<ChatMessage, ChatAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val textView = MaterialTextView(parent.context).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                val dp8 = (8 * parent.context.resources.displayMetrics.density).toInt()
                val dp4 = (4 * parent.context.resources.displayMetrics.density).toInt()
                setPadding(dp8 * 2, dp8, dp8 * 2, dp8)
                setMargins(dp8, dp4, dp8, dp4)
            }
            maxWidth = (parent.width * 0.8f).toInt()
            setTextIsSelectable(true)
        }
        return ViewHolder(textView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val textView: MaterialTextView) : RecyclerView.ViewHolder(textView) {
        fun bind(msg: ChatMessage) {
            textView.text = msg.content
            if (msg.isUser) {
                textView.apply {
                    setBackgroundResource(R.drawable.bg_message_user)
                    setTextColor(0xFFFFFFFF.toInt())
                }
                (textView.layoutParams as ViewGroup.MarginLayoutParams).marginEnd =
                    (24 * textView.context.resources.displayMetrics.density).toInt()
            } else {
                textView.apply {
                    setBackgroundResource(R.drawable.bg_message_ai)
                    setTextColor(textView.context.getColor(android.R.color.secondary_text_material_dark))
                }
                (textView.layoutParams as ViewGroup.MarginLayoutParams).marginStart =
                    (24 * textView.context.resources.displayMetrics.density).toInt()
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(a: ChatMessage, b: ChatMessage) = a === b
        override fun areContentsTheSame(a: ChatMessage, b: ChatMessage) = a.content == b.content && a.isUser == b.isUser
    }
}
