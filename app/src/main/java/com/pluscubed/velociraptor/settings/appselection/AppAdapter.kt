package com.pluscubed.velociraptor.settings.appselection

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pluscubed.velociraptor.databinding.ListItemAppBinding

internal class AppAdapter(
    private val context: Context,
    private val isSelected: (packageName: String) -> Boolean,
    private val onItemClick: (appInfo: AppInfo, checked: Boolean) -> Unit
) : ListAdapter<AppInfo, AppAdapter.ViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = currentList[position]
        Glide.with(context)
            .load(app)
            .crossFade()
            .into(holder.icon)

        holder.title.text = app.name
        holder.desc.text = app.packageName
        holder.checkbox.isChecked = isSelected(app.packageName)
        holder.itemView.setOnClickListener {
            holder.checkbox.toggle()

            if (holder.adapterPosition != RecyclerView.NO_POSITION) {
                onItemClick(currentList[holder.adapterPosition], holder.checkbox.isChecked)
            }
        }
    }

    internal class ViewHolder(binding: ListItemAppBinding) : RecyclerView.ViewHolder(binding.root) {
        val icon: ImageView = binding.imageApp
        val title: TextView = binding.textName
        val desc: TextView = binding.textDesc
        val checkbox: CheckBox = binding.checkbox
    }

    private object DiffCallback : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem.name == newItem.name
        }
    }
}
