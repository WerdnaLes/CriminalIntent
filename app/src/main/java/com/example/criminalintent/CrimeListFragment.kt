package com.example.criminalintent

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.criminalintent.databinding.FragmentCrimeListBinding
import kotlinx.coroutines.launch
import java.util.*

class CrimeListFragment : Fragment() {

    private val crimeListViewModel: CrimeListViewModel by viewModels()

    private var _binding: FragmentCrimeListBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            FragmentCrimeListBinding.inflate(inflater, container, false)

        binding.crimeRecyclerView.layoutManager =
            LinearLayoutManager(context)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Adding a new crime with Button (challenge):
        binding.addCrimeBtn.setOnClickListener { showNewCrime() }
        // Repeats specific suspend function when the View is in STARTED state
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                crimeListViewModel.crimes.collect { crimes ->
                    binding.apply {
                        // Set the text and button visibility if the crime list is empty:
                        emptyText.isVisible = crimes.isEmpty()
                        addCrimeBtn.isVisible = crimes.isEmpty()
                        // Set the adapter:
                        crimeRecyclerView.adapter =
                            CrimeListAdapter(crimes) { crimeId ->
                                findNavController()
                                    .navigate(CrimeListFragmentDirections.showCrimeDetail(crimeId))
                            }
                    }
                }
            }
        }

        /*
        onCreateOptionsMenu(Menu, MenuInflater) is now deprecated. Instead use MenuProvider implementation
        in the onViewCreated() cycle:
        */

        // Adding the menu provider anonymous object that implements menu methods:
        val menuHost: MenuHost = requireActivity() // Adding menu for Fragment requires Activity.
        menuHost.addMenuProvider(object : MenuProvider {

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_crime_list, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.new_crime -> {
                        showNewCrime()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showNewCrime() {
        viewLifecycleOwner.lifecycleScope.launch {
            val newCrime = Crime(
                id = UUID.randomUUID(),
                title = "",
                date = Date(),
                isSolved = false
            )
            crimeListViewModel.addCrime(newCrime)
            findNavController()
                .navigate(CrimeListFragmentDirections.showCrimeDetail(newCrime.id))
        }
    }
}