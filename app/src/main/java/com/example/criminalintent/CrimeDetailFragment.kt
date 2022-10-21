package com.example.criminalintent

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.text.format.DateFormat
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.criminalintent.databinding.FragmentCrimeDetailBinding
import kotlinx.coroutines.launch
import java.util.*

private const val DATE_FORMAT = "EEE, MMM, dd"

class CrimeDetailFragment : Fragment() {

    private var _binding: FragmentCrimeDetailBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val args: CrimeDetailFragmentArgs by navArgs()

    private val crimeDetailViewModel: CrimeDetailViewModel by viewModels {
        CrimeDetailViewModelFactory(args.crimeId)
    }

    private lateinit var onBackPressedCallback: OnBackPressedCallback

    // PickContact result callback:
    private val selectSuspect = registerForActivityResult(
        ActivityResultContracts.PickContact()
    ) { uri ->
        uri?.let { parseContactSelection(it) }
    }

    private val callSuspect = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        binding.apply {
            // Disable/Enable buttons according to the permission granted:
            isReadContactsPermissionGranted(isGranted)
        }
    }

    private lateinit var phoneNumber: Uri

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding =
            FragmentCrimeDetailBinding.inflate(inflater, container, false)

        /*
        Prevent going back if the title is blank. The callback will be called in at least
        STARTED state.
        */
        onBackPressedCallback = requireActivity()
            .onBackPressedDispatcher
            .addCallback(this) {
                Toast.makeText(
                    requireContext(),
                    "Title can not be blank!",
                    Toast.LENGTH_SHORT
                ).show()
            }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // UI update TO the backEnd (CrimeListDetailViewModel)
        binding.apply {
            crimeTitle.doOnTextChanged { text, _, _, _ ->
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(title = text.toString())
                }
            }

            crimeSolved.setOnCheckedChangeListener { _, isChecked ->
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(isSolved = isChecked)
                }
            }

            // Choose Suspect Button onClickListener:
            crimeSuspect.setOnClickListener {
                selectSuspect.launch(null)
            }

            crimeCallBtn.setOnClickListener {
                // If the number was found, start a new Intent
                if (this@CrimeDetailFragment::phoneNumber.isInitialized) {
                    val reportIntent =
                        Intent(Intent.ACTION_DIAL, phoneNumber)

                    startActivity(reportIntent)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "You should choose the suspect first!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }

            // Ensure that the user has appropriate app to receive the intent:
            val selectSuspectIntent =
                selectSuspect.contract.createIntent(
                    requireContext(),
                    null
                )
            crimeSuspect.isEnabled =
                canResolveIntent(selectSuspectIntent)
        }

        // UI update FROM the backEnd (CrimeListDetailViewModel)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                checkDialPermission()
                crimeDetailViewModel.crime.collect { crime ->
                    crime?.let {
                        updateUi(it)
                    }
                }
            }
        }

        // Set listener for DatePickerFragment results:
        setFragmentResultListener(
            DatePickerFragment.REQUEST_KEY_DATE
        ) { _, bundle ->
            val newDate =
                bundle.getSerializable(DatePickerFragment.BUNDLE_KEY_DATE) as Date
            crimeDetailViewModel.updateCrime { it.copy(date = newDate) }
        }

        // Set listener for TimePickerFragment results (Challenge):
        setFragmentResultListener(
            TimePickerFragment.REQUEST_KEY_DATE
        ) { _, bundle ->
            val newTime =
                bundle.getSerializable(TimePickerFragment.BUNDLE_KEY_DATE) as Date
            crimeDetailViewModel.updateCrime { it.copy(date = newTime) }
        }

        // Add the remove button to Action Bar (Challenge):
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_crime_detail, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.remove_crime -> {
                        crimeRemoval()
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

    private fun crimeRemoval() {
        viewLifecycleOwner.lifecycleScope.launch {
            crimeDetailViewModel.crime.collect { crime ->
                crime?.let {
                    crimeDetailViewModel.removeCrime(it)
                    findNavController().navigate(CrimeDetailFragmentDirections.crimeRemoval())
                }
            }
        }
    }

    private fun updateUi(crime: Crime) {
        binding.apply {
            if (crimeTitle.text.toString() != crime.title) {
                crimeTitle.setText(crime.title)
            }
            // Switched from custom date (SimpleDateFormat) to DateFormat:
            crimeDate.text =
                DateFormat.format("EEEE, MMM dd, yyyy", crime.date)
                    .toString() // Orig: crime.date.toString()
            // Showing Dialog Fragment:
            crimeDate.setOnClickListener {
                findNavController()
                    .navigate(CrimeDetailFragmentDirections.selectDate(crime.date))
            }
            crimeTime.text =
                DateFormat.format("h:mm a", crime.date).toString()
            // Showing TimePickerDialog (Challenge):
            crimeTime.setOnClickListener {
                findNavController()
                    .navigate(CrimeDetailFragmentDirections.selectTime(crime.date))
            }

            crimeSolved.isChecked = crime.isSolved

            crimeReport.setOnClickListener {
                val reportIntent =
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, getCrimeReport(crime))
                        putExtra(
                            Intent.EXTRA_SUBJECT,
                            getString(R.string.crime_report_subject)
                        )
                    }

                val chooserIntent = Intent.createChooser(
                    reportIntent,
                    getString(R.string.send_report)
                )
                startActivity(chooserIntent)
            }

            crimeSuspect.text = crime.suspect.ifEmpty {
                getString(R.string.crime_suspect_text)
            }
            // Enabling callback if the title is blank:
            onBackPressedCallback.isEnabled = crime.title == ""
        }
    }

    // Parsing the crime report:
    private fun getCrimeReport(crime: Crime): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }

        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
        val suspectText = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }

        return getString(
            R.string.crime_report,
            crime.title, dateString, solvedString, suspectText
        )
    }

    // Parse the Uri result picked from Contacts selected and update the Crime:
    @SuppressLint("Range")
    private fun parseContactSelection(contactUri: Uri) {
        // Create cursor:
        val queryCursor = requireActivity().contentResolver
            .query(contactUri, null, null, null, null)

        // Navigate through the cursor and retrieve the String value from the [0] column:
        queryCursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                // Get contact name:
                val suspect =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                // Get contact ID:
                val suspectId =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(suspect = suspect)
                }

                // Check if the contact has a number:
                val hasPhoneNumber =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                        .toInt()
                // Proceed querying for the number by contact's ID:
                if (hasPhoneNumber > 0) {
                    val phoneCursor = requireActivity().contentResolver
                        .query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(suspectId),
                            null
                        )
                    phoneCursor?.use { cursor1 ->
                        if (cursor1.moveToFirst()) {
                            // Get the number in String format and parse it to URI so it is suitable for the Intent:
                            // URI HAS TO START WITH "tel:" !!!
                            phoneNumber = cursor1.getString(
                                cursor1.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            )
                                .let { number ->
                                    Uri.parse("tel:$number")
                                }
                        }
                    }
                }
            }
        }
    }

    private fun canResolveIntent(intent: Intent): Boolean {
        val packageManager: PackageManager =
            requireActivity().packageManager
        val resolvedActivity =
            packageManager.resolveActivity(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
        return resolvedActivity != null
    }

    private fun checkDialPermission() {
        when {
            ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.

                // Create an AlertDialog to ask for permission:
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Permission needed")
                    .setMessage("We need the permission to read contacts in order to choose a suspect and the ability to call them.")
                    .setPositiveButton(
                        android.R.string.ok
                    ) { _, _ ->
                        callSuspect.launch(
                            android.Manifest.permission.READ_CONTACTS
                        )
                    }
                        // Open app settings to grant permissions:
                    .setNegativeButton(R.string.move_to_settings) { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", requireContext().packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                builder.create().show()

            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                callSuspect.launch(
                    android.Manifest.permission.READ_CONTACTS
                )
            }

        }
    }

    private fun FragmentCrimeDetailBinding.isReadContactsPermissionGranted(
        isGranted: Boolean
    ) {
        crimeSuspect.isEnabled = isGranted
        crimeReport.isEnabled = isGranted
        crimeCallBtn.isEnabled = isGranted
    }
}
