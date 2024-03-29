package com.footprint.footprint.ui.main.calendar

import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.footprint.footprint.R
import com.footprint.footprint.data.dto.TagWalksDTO
import com.footprint.footprint.data.dto.UserDateWalkDTO
import com.footprint.footprint.databinding.FragmentSearchResultBinding
import com.footprint.footprint.ui.BaseFragment
import com.footprint.footprint.ui.adapter.WalkDateRVAdapter
import com.footprint.footprint.ui.adapter.WalkRVAdapter
import com.footprint.footprint.utils.ErrorType
import com.footprint.footprint.viewmodel.TagSearchViewModel
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchResultFragment() :
    BaseFragment<FragmentSearchResultBinding>(FragmentSearchResultBinding::inflate) {
    private var isInitialized = false
    private lateinit var currentTag: String

    private val tagSearchVM: TagSearchViewModel by viewModel()
    private lateinit var networkErrSb: Snackbar

    override fun initAfterBinding() {
        if (!isInitialized) {
            currentTag = navArgs<SearchResultFragmentArgs>().value.tag
            setBinding()
            observe()

            isInitialized = true
        }

        // Tag API
        getTagWalks()
    }

    private fun getTagWalks() {
        tagSearchVM.getTagWalks(currentTag.drop(1))
        binding.searchResultLoadingPb.visibility = View.VISIBLE
        binding.searchResultHintTv.visibility = View.VISIBLE
        binding.searchResultWalkDatesRv.visibility = View.GONE
    }

    private fun observe() {
        tagSearchVM.mutableErrorType.observe(viewLifecycleOwner, Observer {
            when (it) {
                ErrorType.NETWORK -> showSnackBar(getString(R.string.error_network))
                else -> startErrorActivity("SearchResultFragment")
            }
        })

        tagSearchVM.tagWalks.observe(viewLifecycleOwner, Observer { tagWalks ->
            binding.searchResultLoadingPb.visibility = View.GONE

            if (tagWalks.isNotEmpty()) {
                binding.searchResultHintTv.visibility = View.GONE
                binding.searchResultWalkDatesRv.visibility = View.VISIBLE

                initAdapter(tagWalks)
            }
        })
    }

    private fun setBinding() {
        binding.searchResultSearchBarTv.text = currentTag

        binding.searchResultSearchBarTv.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.searchResultBackIv.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.searchResultBackgroundV.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.searchResultSearchIv.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initAdapter(walkDates: List<TagWalksDTO>) {
        val adapter = WalkDateRVAdapter(requireContext())

        adapter.setWalkDates(walkDates)

        if (::currentTag.isInitialized) {
            adapter.setCurrentTag(currentTag)
        }

        adapter.setWalkClickListener(object : WalkRVAdapter.OnItemClickListener {
            override fun onItemClick(walk: UserDateWalkDTO) {
                val action =
                    SearchResultFragmentDirections.actionSearchResultFragmentToWalkDetailActivity(
                        walk.walkIdx
                    )
                findNavController().navigate(action)
            }
        })

        binding.searchResultWalkDatesRv.adapter = adapter
    }


    private fun showSnackBar(errorMessage: String) {
        networkErrSb = Snackbar.make(
            requireView(),
            errorMessage,
            Snackbar.LENGTH_INDEFINITE
        ).setAction(getString(R.string.action_retry)) {
            // Tag API
            getTagWalks()
        }
        networkErrSb.show()
    }

    override fun onStop() {
        super.onStop()
        if (::networkErrSb.isInitialized && networkErrSb.isShown)
            networkErrSb.dismiss()
    }
}