package com.example.criminalintent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.criminalintent.databinding.ListItemCrimeBinding
import java.util.*

class CrimeHolder(
    private val binding: ListItemCrimeBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        crime: Crime,
        onCrimeClicked: (crimeId:UUID) -> Unit
    ) {
        binding.crimeTitle.text = crime.title
        binding.crimeDate.text = crime.date.toString()
        binding.crimeSolved.visibility = if (crime.isSolved) {
            View.VISIBLE
        } else {
            View.GONE
        }

        binding.root.setOnClickListener {
            onCrimeClicked(crime.id)
        }
    }
}

class CrimeListAdapter(
    private val crimes: List<Crime>,
    private val onCrimeClicked: (crimeId:UUID) -> Unit
) : RecyclerView.Adapter<CrimeHolder>() {

    // Creates specific ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding =
            ListItemCrimeBinding.inflate(inflater, parent, false)
        return CrimeHolder(binding)
    }

    // Binds the ViewHolder with an information
    override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
        val crime = crimes[position]
        holder.bind(crime, onCrimeClicked)
    }

    // Represents how many times adapter will call the onCreateViewHolder()
    override fun getItemCount(): Int {
        return crimes.size
    }
}