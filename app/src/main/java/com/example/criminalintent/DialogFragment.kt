package com.example.criminalintent

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.criminalintent.databinding.FragmentDialogBinding
import java.io.File

// Challenge chap 17(Zoom selected picture in this Fragment)
class DialogFragment : Fragment() {

    private var _binding: FragmentDialogBinding? = null
    private val binding: FragmentDialogBinding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val args: DialogFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding =
            FragmentDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Get selected photo, rotate it and show it in full size:
        val zoomedPhoto = File(
            requireContext().applicationContext.filesDir,
            args.photoFile
        ).let { photoFile ->
            correctImageOrientation(photoFile.path, BitmapFactory.decodeFile(photoFile.path))
        }

        binding.expandedImage.setImageBitmap(zoomedPhoto)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}