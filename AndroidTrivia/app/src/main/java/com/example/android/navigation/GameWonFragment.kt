/*
 * Copyright 2018, The Android Open Source Project
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

package com.example.android.navigation

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.android.navigation.databinding.FragmentGameWonBinding


class GameWonFragment : Fragment() {

    private val args: GameWonFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentGameWonBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_game_won, container, false
        )
        // Add OnClick Handler for Next Match button
        binding.nextMatchButton.setOnClickListener {
            findNavController().navigate(
                GameWonFragmentDirections.actionGameWonFragmentToGameFragment()
            )
        }
        Toast.makeText(
            context,
            "NumCorrect: ${args.numCorrect}, NumQuestions: ${args.numQuestions}",
            Toast.LENGTH_LONG
        ).show()
        return binding.root
    }

    private fun getShareIntent(): Intent {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(
            Intent.EXTRA_TEXT,
            getString(R.string.share_success_text, args.numCorrect, args.numQuestions)
        )

        return shareIntent
    }

    private fun shareSuccess() {
        startActivity(getShareIntent())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.winner_menu, menu)
        // check if the activity resolves
        if (requireActivity().packageManager.resolveActivity(getShareIntent(), 0) == null) {
            // hide the menu item if it doesn't resolve
            menu.findItem(R.id.share).isVisible = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.share -> {
                shareSuccess()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
