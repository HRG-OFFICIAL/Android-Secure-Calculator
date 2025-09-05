package com.android.calculator.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.calculator.databinding.HistoryItemBinding

class HistoryAdapter(
    private val historyList: MutableList<History>,
    private val onItemClick: (History) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(private val binding: HistoryItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(history: History, onItemClick: (History) -> Unit) {
            binding.historyItemCalculation.text = history.calculation
            binding.historyItemResult.text = history.result
            
            binding.root.setOnClickListener {
                onItemClick(history)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = HistoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(historyList[position], onItemClick)
    }

    override fun getItemCount(): Int = historyList.size
}
