package com.bradcypert.textico.views.fragments


import android.app.Fragment
import android.app.FragmentManager
import android.os.Bundle
import android.support.v13.app.FragmentPagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.bradcypert.textico.R
import com.bradcypert.textico.transformers.DepthPageTransformer
import io.reactivex.subjects.Subject

class TourContainer : Fragment(), Tour3.OnDBBuiltListener {
    override fun onDBBuilt() {
        val a = activity as OnTourCompleteListener
        a.onTourComplete()
    }

    interface OnTourCompleteListener {
        fun onTourComplete()
    }

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private var mPager: ViewPager? = null

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private var mPagerAdapter: PagerAdapter? = null

    private var previous: Button? = null
    private var next: Button? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup,
                              savedInstanceState: Bundle?): View {

        val view = inflater.inflate(R.layout.tour_container_fragment, container, false) as ViewGroup

        mPager = view.findViewById(R.id.tourPager) as ViewPager
        mPagerAdapter = ScreenSlidePagerAdapter(childFragmentManager)
        mPager!!.adapter = mPagerAdapter

        previous = view.findViewById(R.id.tour_previous)
        previous!!.visibility = View.INVISIBLE
        next = view.findViewById(R.id.tour_next)

        mPager!!.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                when(position) {
                    0 -> {
                        previous!!.visibility = View.INVISIBLE
                        next!!.visibility = View.VISIBLE
                    }
                    1 -> {
                        previous!!.visibility = View.VISIBLE
                        next!!.visibility = View.VISIBLE
                    }
                    2 -> {
                        previous!!.visibility = View.VISIBLE
                        next!!.visibility = View.INVISIBLE
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        previous!!.setOnClickListener {
            mPager!!.currentItem = mPager!!.currentItem - 1
        }

        next!!.setOnClickListener {
            mPager!!.currentItem = mPager!!.currentItem + 1
        }

        mPager!!.setPageTransformer(true, DepthPageTransformer())

        return view
    }

    /**
     * A simple pager adapter that represents 3 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getCount(): Int {
            return NUM_PAGES
        }

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> Tour1()
                1 -> Tour2()
                else -> Tour3()
            }
        }
    }

    companion object {
        private const val NUM_PAGES = 3
    }
}