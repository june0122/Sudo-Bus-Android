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
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.june0122.bis_sample.R
import com.june0122.bis_sample.ui.adapter.SearchResultViewPagerAdapter
import com.june0122.bis_sample.utils.dp
import com.june0122.bis_sample.utils.setStrictMode
import com.june0122.bis_sample.utils.textColor
import kotlinx.android.synthetic.main.fragment_search.*
import java.util.*

fun EditText.setAttributes(hint: String, inputType: Int) {
    this.hint = hint
    this.inputType = inputType
}

class SearchInfoFragment : Fragment() {
    val previewBusFragment = PreviewBusFragment()
    val previewStationFragment = PreviewStationFragment()

    var timer: Timer? = null
    var inputData = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPagerAdapter = SearchResultViewPagerAdapter(childFragmentManager)
                .apply {
                    addFragment(previewBusFragment, "버스")
                    addFragment(previewStationFragment, "정류장")
                }

        searchResultViewPager.adapter = viewPagerAdapter

        searchTypeTabs.run {
            setupWithViewPager(searchResultViewPager, false)  // autoRefresh 검증 필요
            getTabAt(0)?.setIcon(R.drawable.ic_bus_front)
            getTabAt(1)?.setIcon(R.drawable.ic_bus_station)

            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabSelected(tab: TabLayout.Tab?) {
                    Log.d("INPUT", inputData)
                    when (tab?.position) {
                        0 -> {
                            numberTypeKeyboardButton.isSelected = true
                            textTypeKeyboardButton.isSelected = false
                            searchEditText
                                    .setAttributes(getString(R.string.search_tab_hint_1), InputType.TYPE_CLASS_NUMBER)
                            numberTypeKeyboardButton.textColor(view, R.color.white)
                            textTypeKeyboardButton.textColor(view, R.color.gray_alpha_20)
                        }
                        1 -> {
                            textTypeKeyboardButton.isSelected = true
                            numberTypeKeyboardButton.isSelected = false
                            searchEditText
                                    .setAttributes(getString(R.string.search_tab_hint_2), InputType.TYPE_CLASS_TEXT)
                            textTypeKeyboardButton.textColor(view, R.color.white)
                            numberTypeKeyboardButton.textColor(view, R.color.gray_alpha_20)
                        }
                    }
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }
            })
        }

        val searchFragmentLayout = view.findViewById<ConstraintLayout>(R.id.searchFragmentLayout)
        val keyboardTabLayout = view.findViewById<ConstraintLayout>(R.id.keyboardTabLayout)

        numberTypeKeyboardButton.isSelected = true
        textTypeKeyboardButton.textColor(view, R.color.gray_alpha_20)

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
            numberTypeKeyboardButton.textColor(view, R.color.white)
            textTypeKeyboardButton.textColor(view, R.color.gray_alpha_20)
            searchEditText.inputType = InputType.TYPE_CLASS_NUMBER
        }

        textTypeKeyboardButton.setOnClickListener {
            textTypeKeyboardButton.isSelected = true
            numberTypeKeyboardButton.isSelected = false
            textTypeKeyboardButton.textColor(view, R.color.white)
            numberTypeKeyboardButton.textColor(view, R.color.gray_alpha_20)
            searchEditText.inputType = InputType.TYPE_CLASS_TEXT
        }

        setStrictMode()

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                timer?.let { timer?.cancel() }

                timer = Timer()

                timer?.schedule(object : TimerTask() {
                    override fun run() {

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

                        activity?.runOnUiThread {
                            if(s.toString().isEmpty()) {
                                Log.d("XXXXX", "Empty")
                                inputData = ""
                            }
                            viewPagerAdapter.notifyDataSetChanged()
                        }
                    }
                }, 600)
            }

            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
        })

    }
}