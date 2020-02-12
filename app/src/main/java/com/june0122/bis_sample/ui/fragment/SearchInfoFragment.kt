package com.june0122.bis_sample.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.june0122.bis_sample.R
import com.june0122.bis_sample.ui.adapter.SearchResultViewPagerAdapter
import com.june0122.bis_sample.utils.dp
import com.june0122.bis_sample.utils.setStrictMode
import kotlinx.android.synthetic.main.fragment_search.*

fun EditText.setAttributes(hint: String, inputType: Int) {
    this.hint = hint
    this.inputType = inputType
}

class SearchInfoFragment : Fragment() {
    val previewBusFragment = PreviewBusFragment()
    val previewStationFragment = PreviewStationFragment()

    var inputData = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPagerAdapter = SearchResultViewPagerAdapter(childFragmentManager)
        viewPagerAdapter.addFragment(previewBusFragment, "버스")
        viewPagerAdapter.addFragment(previewStationFragment, "정류장")
        searchResultViewPager.adapter = viewPagerAdapter
        searchTypeTabs.setupWithViewPager(searchResultViewPager, false)  // autoRefresh 검증 필요

        searchTypeTabs.getTabAt(0)?.setIcon(R.drawable.ic_bus_front)
        searchTypeTabs.getTabAt(1)?.setIcon(R.drawable.ic_bus_station)
        searchTypeTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                Log.d("INPUT", inputData)
                when (tab?.position) {
                    0 -> {
                        numberTypeKeyboardButton.isSelected = true
                        textTypeKeyboardButton.isSelected = false
                        searchEditText.setAttributes(getString(R.string.search_tab_hint_1), InputType.TYPE_CLASS_NUMBER)


                        numberTypeKeyboardButton.setTextColor(resources.getColor(R.color.white))
                        textTypeKeyboardButton.setTextColor(resources.getColor(R.color.gray_alpha_20))
                    }
                    1 -> {
                        textTypeKeyboardButton.isSelected = true
                        numberTypeKeyboardButton.isSelected = false
                        searchEditText.setAttributes(getString(R.string.search_tab_hint_2), InputType.TYPE_CLASS_TEXT)

                        textTypeKeyboardButton.setTextColor(resources.getColor(R.color.white))
                        numberTypeKeyboardButton.setTextColor(resources.getColor(R.color.gray_alpha_20))
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        val searchFragmentLayout = view.findViewById<ConstraintLayout>(R.id.searchFragmentLayout)
        val keyboardTabLayout = view.findViewById<ConstraintLayout>(R.id.keyboardTabLayout)

        numberTypeKeyboardButton.isSelected = true
        textTypeKeyboardButton.setTextColor(ContextCompat.getColor(view.context, R.color.gray_alpha_20))

        keyboardTabLayout?.viewTreeObserver?.addOnGlobalLayoutListener {
            val rootViewHeight = searchFragmentLayout.rootView.height
            val searchFragmentLayoutHeight = searchFragmentLayout.height
            val gapOfHeight = rootViewHeight - searchFragmentLayoutHeight

            if (gapOfHeight > 200.dp()) {
                keyboardTabLayout.visibility = View.VISIBLE
            } else {
                keyboardTabLayout.visibility = View.GONE
            }
        }

        numberTypeKeyboardButton.setOnClickListener {
            numberTypeKeyboardButton.isSelected = true
            textTypeKeyboardButton.isSelected = false
            numberTypeKeyboardButton.setTextColor(ContextCompat.getColor(view.context, R.color.white))
            textTypeKeyboardButton.setTextColor(ContextCompat.getColor(view.context, R.color.gray_alpha_20))

            searchEditText.inputType = InputType.TYPE_CLASS_NUMBER
        }

        textTypeKeyboardButton.setOnClickListener {
            textTypeKeyboardButton.isSelected = true
            numberTypeKeyboardButton.isSelected = false
            textTypeKeyboardButton.setTextColor(ContextCompat.getColor(view.context, R.color.white))
            numberTypeKeyboardButton.setTextColor(ContextCompat.getColor(view.context, R.color.gray_alpha_20))
            searchEditText.inputType = InputType.TYPE_CLASS_TEXT
        }


        setStrictMode()

        activity?.runOnUiThread {

            searchEditText.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    inputData = searchEditText.text.toString()


                    when (searchTypeTabs.selectedTabPosition) {
                        0 -> {

                            previewBusFragment.inputBusNumber(inputData)


                            viewPagerAdapter.updateBusFragment(previewBusFragment)
                        }
                        1 -> {
                            previewStationFragment.inputStationArsId(inputData)
                            viewPagerAdapter.updateStationFragment(previewStationFragment)
                        }
                    }

                    viewPagerAdapter.notifyDataSetChanged()

                }

                override fun afterTextChanged(s: Editable?) {

                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }
            })

        }
    }
}