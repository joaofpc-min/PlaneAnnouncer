package online.bukabuku.v3planeannouncer.adapters

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import online.bukabuku.v3planeannouncer.database.Planes
import online.bukabuku.v3planeannouncer.databinding.PlanesItemBinding
import java.sql.Date
import java.text.SimpleDateFormat

class PlanesAdapter(private val onItemClicked: (Planes) -> Unit) : ListAdapter<Planes, PlanesAdapter.PlanesViewHolder>(DiffCallback) {

    class PlanesViewHolder(private var binding: PlanesItemBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SimpleDateFormat")
        fun bind(planes: Planes) {
            binding.planeNameTextView.text = planes.aircraft_icao
            binding.planeDepTextView.text = planes.dep_iata
            binding.planeArrTextView.text = planes.arr_iata
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanesViewHolder {
        val viewHolder = PlanesViewHolder(
            PlanesItemBinding.inflate(
                LayoutInflater.from( parent.context),
                parent,
                false
            )
        )
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.adapterPosition
            onItemClicked(getItem(position))
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: PlanesViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Planes>() {
            override fun areItemsTheSame(oldItem: Planes, newItem: Planes): Boolean {
                return oldItem.hex == newItem.hex
            }

            override fun areContentsTheSame(oldItem: Planes, newItem: Planes): Boolean {
                return oldItem == newItem
            }
        }
    }

}

