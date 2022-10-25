package com.example.criminalintent

import android.content.Context
import android.icu.text.DateFormat.*
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
        context: Context,
        onCrimeClicked: (crimeId: UUID) -> Unit
    ) {
        binding.apply {
            crimeTitle.text = crime.title
            crimeDate.text =
                getDateTimeInstance(FULL, SHORT).format(crime.date)
//            DateFormat.format("EEE, MMMM dd, h:mm a, yyyy", crime.date).toString() -> old one

            val isSolved: String
            crimeSolved.visibility = if (crime.isSolved) {
                isSolved = "The crime is solved"
                View.VISIBLE
            } else {
                isSolved = "The crime is not solved yet"
                View.GONE
            }

            // Add View description for TalkBack to describe the crime:
            root.contentDescription =
                context.resources.getString(
                    R.string.crime_content_description,
                    crime.title, isSolved, crimeDate.text
                )

            root.setOnClickListener {
                onCrimeClicked(crime.id)
            }
        }
    }
}

class CrimeListAdapter(
    private val crimes: List<Crime>,
    private val context: Context,
    private val onCrimeClicked: (crimeId: UUID) -> Unit
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
        holder.bind(crime, context, onCrimeClicked)
    }

    // Represents how many times adapter will call the onCreateViewHolder()
    override fun getItemCount(): Int {
        return crimes.size
    }
}