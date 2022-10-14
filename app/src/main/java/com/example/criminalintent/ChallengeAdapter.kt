package com.example.criminalintent

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.criminalintent.databinding.ChallengeItemBinding
import com.example.criminalintent.databinding.ListItemCrimeBinding

class MyViewHolder(
    private val binding: ListItemCrimeBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(crime: Crime) {
        binding.crimeTitle.text = crime.title
        binding.crimeDate.text = crime.date.toString()

        binding.root.setOnClickListener {
            Toast.makeText(
                binding.root.context,
                "${crime.title} clicked!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

class MyViewHolder2(
    private val binding: ChallengeItemBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind() {
        binding.contactPoliceButton.setOnClickListener {
            Toast.makeText(
                binding.root.context,
                "Police called!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

class ChallengeAdapter(
    private val crimes: List<Crime>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> {
                val binding =
                    ListItemCrimeBinding.inflate(inflater, parent, false)
                MyViewHolder(binding)
            }
            else -> {
                val binding =
                    ChallengeItemBinding.inflate(inflater, parent, false)
                MyViewHolder2(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val crime = crimes[position]
        when (holder.itemViewType) {
            0 -> {
                val temp = holder as MyViewHolder
                temp.bind(crime)
            }
            2 -> {
                val temp = holder as MyViewHolder2
                temp.bind()
            }
        }
    }

    override fun getItemCount(): Int {
        return crimes.size
    }

    override fun getItemViewType(position: Int): Int {
        val crime = crimes[position]
        return if (crime.isSolved) 2 else 0
    }
}