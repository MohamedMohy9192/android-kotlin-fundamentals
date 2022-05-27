/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.SleepNightAdapter
import com.example.android.trackmysleepquality.SleepNightListener
import com.example.android.trackmysleepquality.database.SleepDatabase
import com.example.android.trackmysleepquality.databinding.FragmentSleepTrackerBinding
import com.google.android.material.snackbar.Snackbar

/**
 * A fragment with buttons to record start and end times for sleep, which are saved in
 * a database. Cumulative data is displayed in a simple scrollable TextView.
 * (Because we have not learned about RecyclerView yet.)
 */
class SleepTrackerFragment : Fragment() {

    /**
     * Called when the Fragment is ready to display content to the screen.
     *
     * This function uses DataBindingUtil to inflate R.layout.fragment_sleep_quality.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentSleepTrackerBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_sleep_tracker, container, false
        )

        val application = requireNotNull(activity).application
        val dataSource = SleepDatabase.getInstance(application).sleepDatabaseDao()
        // Create an instance of the ViewModel Factory
        val viewModelFactory = SleepTrackerViewModelFactory(dataSource, application)
        // Get a reference to the ViewModel associated with this fragment
        val sleepTrackerViewModel =
            ViewModelProvider(this, viewModelFactory)[SleepTrackerViewModel::class.java]
        binding.sleepTrackerViewModel = sleepTrackerViewModel

        binding.lifecycleOwner = viewLifecycleOwner

        val adapter = SleepNightAdapter(SleepNightListener { nightId ->
            Toast.makeText(context, "$nightId", Toast.LENGTH_SHORT).show()
            sleepTrackerViewModel.onSleepNightClicked(nightId)
        })
        binding.sleepListRecyclerView.adapter = adapter

        sleepTrackerViewModel.nights
            .observe(viewLifecycleOwner) { nights ->
                nights?.let { sleepNights ->
                    adapter.submitList(sleepNights)
                }
            }

        sleepTrackerViewModel.navigateToSleepQuality
            .observe(viewLifecycleOwner) { sleepNight ->
                sleepNight?.let { night ->
                    val action = SleepTrackerFragmentDirections
                        .actionSleepTrackerFragmentToSleepQualityFragment(night.nightId)
                    findNavController().navigate(action)
                    sleepTrackerViewModel.doneNavigation()
                }
            }

        sleepTrackerViewModel.showSnackBarEvent
            .observe(viewLifecycleOwner) { showSnackBar ->
                if (showSnackBar) {
                    Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        getString(R.string.cleared_message),
                        Snackbar.LENGTH_SHORT
                    ).show()
                    sleepTrackerViewModel.doneShowingSnackBar()
                }

            }

        sleepTrackerViewModel.navigateToSleepDetail
            .observe(viewLifecycleOwner) { nightId ->
                nightId?.let {
                    findNavController().navigate(
                        SleepTrackerFragmentDirections
                            .actionSleepTrackerFragmentToSleepDetailFragment(nightId)
                    )
                    sleepTrackerViewModel.onSleepDetailNavigated()
                }
            }

        val manager = GridLayoutManager(activity, 3, GridLayoutManager.VERTICAL, false)
        binding.sleepListRecyclerView.layoutManager = manager
        return binding.root
    }
}
