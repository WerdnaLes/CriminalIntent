package com.example.criminalintent

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.DateFormat.*
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.text.format.DateFormat
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
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
import java.io.File
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

    // PickContact result callback:
    private val selectSuspect = registerForActivityResult(
        ActivityResultContracts.PickContact()
    ) { uri ->
        uri?.let { parseContactSelection(it) }
    }

    // Get result from RequestPermission for READ_CONTACTS:
    private val callSuspect = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Disable/Enable buttons according to the permission granted:
        binding.isReadContactsPermissionGranted(isGranted)
    }

    // Get result from take photo button:
    private val takePhoto = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { didTakePhoto ->
        if (didTakePhoto && photoName != null) {
            crimeDetailViewModel.updateCrime { oldCrime ->
                oldCrime.copy(photoFileName = photoName)
            }
        }
    }

    private lateinit var onBackPressedCallback: OnBackPressedCallback

    private var photoName: String? = null

    // Removed blank title bug by moving onBackPressedCallback to onAttach():
    override fun onAttach(context: Context) {
        super.onAttach(context)
        /* Prevent going back if the title is blank. The callback will be called in at least
        STARTED state. */
        onBackPressedCallback = requireActivity()
            .onBackPressedDispatcher
            .addCallback(this) {
                Toast.makeText(
                    requireContext(),
                    R.string.title_is_blank_toast,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding =
            FragmentCrimeDetailBinding.inflate(inflater, container, false)

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

            // IsCrimeSolved checkbox checked listener:
            crimeSolved.setOnCheckedChangeListener { _, isChecked ->
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(isSolved = isChecked)
                }
            }

            // Choose a suspect from user's contacts:
            crimeSuspect.setOnClickListener {
                selectSuspect.launch(null)
            }

            // Ensure that the user has appropriate app to receive the intent:
            val selectSuspectIntent =
                selectSuspect.contract.createIntent(
                    requireContext(),
                    null
                )
            crimeSuspect.isEnabled =
                canResolveIntent(selectSuspectIntent)

            // Take photo listener:
            crimeCamera.setOnClickListener {
                photoName = "IMG_${Date()}.JPG"
                val photoFile =
                    File(
                        requireContext().applicationContext.filesDir,
                        photoName!!
                    )
                val photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.example.criminalintent.fileprovider",
                    photoFile
                )

                takePhoto.launch(photoUri)
            }

            // UPD: can't find activity to resolve the intent so it crashes the app =/
//            val captureImageIntent =
//                takePhoto.contract.createIntent(
//                    requireContext(),
//                    null
//                )
            // Used this instead:
            // Ensure that the device has an app to resolve the intent (disable button otherwise).
            crimeCamera.isEnabled =
                canResolveCameraAction()

        }

        // UI update FROM the backEnd (CrimeListDetailViewModel)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                checkReadContactsPermission()
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

    // Remove crime via Action Bar view:
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
            crimeDate.text = getDateInstance(FULL).format(crime.date)
//                DateFormat.format("EEEE, MMM dd, yyyy", crime.date)
//                    .toString() // Orig: crime.date.toString() -> old one
            // Showing Dialog Fragment:
            crimeDate.setOnClickListener {
                findNavController()
                    .navigate(CrimeDetailFragmentDirections.selectDate(crime.date))
            }
            crimeTime.text = getTimeInstance(SHORT).format(crime.date)
//                DateFormat.format("h:mm a", crime.date).toString() -> old one
            // Showing TimePickerDialog (Challenge):
            crimeTime.setOnClickListener {
                findNavController()
                    .navigate(CrimeDetailFragmentDirections.selectTime(crime.date))
            }

            crimeSolved.isChecked = crime.isSolved

            // Send Crime report button:
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

                // Always ask which app should perform ACTION_SEND:
                val chooserIntent = Intent.createChooser(
                    reportIntent,
                    getString(R.string.send_report)
                )
                startActivity(chooserIntent)
            }

            crimeCallBtn.isVisible = crime.suspectPhoneNumber.isNotEmpty()

            // Commit DIAL with a suspect phone number:
            crimeCallBtn.setOnClickListener {
                // URI HAS TO START WITH "tel:" !!!
                val number = Uri.parse("tel:${crime.suspectPhoneNumber}")
                val dialIntent =
                    Intent(Intent.ACTION_DIAL, number)

                startActivity(dialIntent)
            }

            // Set default text if there is no suspect
            crimeSuspect.text = crime.suspect.ifEmpty {
                getString(R.string.crime_suspect_text)
            }

            updatePhoto(crime.photoFileName)
            // Enabling callback if the title is blank:
            onBackPressedCallback.isEnabled = crime.title.isEmpty()

            crimePhoto.setOnClickListener {
                crime.photoFileName?.let {
                    findNavController().navigate(CrimeDetailFragmentDirections.zoomPicture(it))
                }
            }
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
    private fun parseContactSelection(contactUri: Uri) {
        // Create cursor:
        val queryFields =
            arrayOf(
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.HAS_PHONE_NUMBER
            )

        val queryCursor = requireActivity().contentResolver
            .query(contactUri, queryFields, null, null, null)

        // use() is a closeable function that executes the given block and closes it down correctly
        // Navigate through the cursor and retrieve the String value from the [0] column:
        queryCursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                // Get contact name:
                val suspect =
                    cursor.getString(0)
                // Get contact ID:
                val suspectId =
                    cursor.getString(1)
                // Check if the contact has a number:
                val hasPhoneNumber =
                    cursor.getString(2).toInt()

                when (hasPhoneNumber > 0) {
                    // Proceed querying for the number by contact's ID:
                    true -> retrieveSuspectPhoneNumber(suspectId, suspect)
                    // Else disable crimeCallButton and show Toast:
                    else -> {
                        Toast.makeText(
                            requireContext(),
                            R.string.no_number_toast,
                            Toast.LENGTH_SHORT
                        ).show()
                        crimeDetailViewModel.updateCrime { oldCrime ->
                            oldCrime.copy(suspect = suspect, suspectPhoneNumber = "")
                        }
                    }
                }
            }
        }
    }

    // Retrieve suspect's phone number if it exists:
    private fun retrieveSuspectPhoneNumber(
        suspectId: String?,
        suspect: String
    ) {
        val queryFields =
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val phoneCursor = requireActivity().contentResolver
            .query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                queryFields,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                arrayOf(suspectId),
                null
            )

        phoneCursor?.use { numberCursor ->
            if (numberCursor.moveToFirst()) {
                // Get the number in String format and parse it to URI in the callSuspect Button onClickListener:
                numberCursor.getString(0)
                    .let { phoneNumber ->
                        crimeDetailViewModel.updateCrime { oldCrime ->
                            oldCrime.copy(suspect = suspect, suspectPhoneNumber = phoneNumber)
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

    private fun canResolveCameraAction(): Boolean {
        val captureImageIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        return captureImageIntent.resolveActivity(requireActivity().packageManager) != null
    }

    // Measure and/or rotate Photo BEFORE it appears in the ImageView
    private fun updatePhoto(photoFileName: String?) {
        if (binding.crimePhoto.tag != photoFileName) {
            val photoFile = photoFileName?.let {
                File(requireContext().applicationContext.filesDir, it)
            }

            if (photoFile?.exists() == true) {
                binding.crimePhoto.doOnLayout { measuredView ->
                    val scaledBitmap = getScaledBitmap(
                        photoFile.path,
                        measuredView.width,
                        measuredView.height
                    ).let {
                        // Perform rotation if needed:
                        correctImageOrientation(photoFile.path, it)
                    }

                    binding.crimePhoto.setImageBitmap(scaledBitmap)
                    binding.crimePhoto.tag = photoFileName
                }
            } else {
                binding.crimePhoto.setImageBitmap(null)
                binding.crimePhoto.tag = null
            }
        }
    }

    // Check READ_CONTACTS permission:
    private fun checkReadContactsPermission() {
        when {
            ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.

                // Create an AlertDialog to ask for permission:
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle(R.string.permission_read_contacts_title)
                    .setMessage(R.string.permission_read_contacts_message)
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

    // Custom function to enable/disable buttons depending on whether the permission was granted
    private fun FragmentCrimeDetailBinding.isReadContactsPermissionGranted(
        isGranted: Boolean
    ) {
        crimeSuspect.isEnabled = isGranted
        crimeReport.isEnabled = isGranted
        crimeCallBtn.isEnabled = isGranted
    }
}
