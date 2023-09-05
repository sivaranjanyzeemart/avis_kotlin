package zeemart.asia.buyers.orders.createordersimport

import android.R
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.OrderDetailSwipeAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.models.order.Orders

/**
 * A simple [Fragment] subclass.
 * Use the [OrderDetailDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OrderDetailDialogFragment : DialogFragment() {
    private var pastOrderList: ArrayList<Orders>? = null
    private lateinit var txtSwipeIndexer: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.Theme_DeviceDefault_Dialog_NoActionBar)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val back = ColorDrawable(requireActivity().resources.getColor(zeemart.asia.buyers.R.color.white))
        val inset = InsetDrawable(
            back,
            0,
            OrderDetailDialogFragment.Companion.INSERT_DRAWABLE_TOP_WIDTH,
            0,
            OrderDetailDialogFragment.Companion.INSERT_DRAWABLE_BOTTOM_WIDTH
        )
        dialog!!.window!!.setBackgroundDrawable(inset)
        val bundle = arguments
        val json = bundle!!.getString("PAST_ORDER_LIST_DATA")
        val position = bundle.getInt("SELECTED_PAST_ORDER_POSITION")
        val horizontalRowId = bundle.getInt("SELECTED_HORIZONTAL_ROW_ID")
        pastOrderList = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
            json,
            object : TypeToken<ArrayList<Orders?>?>() {}.type
        )

        // Inflate the layout for this fragment
        val v = inflater.inflate(zeemart.asia.buyers.R.layout.order_detail_layout, container, false)
        val pager = v.findViewById<ViewPager>(zeemart.asia.buyers.R.id.pgr_view_order_swipe)
        pager.adapter =
            OrderDetailSwipeAdapter(childFragmentManager, pastOrderList!!, position, horizontalRowId)
        txtSwipeIndexer = v.findViewById(zeemart.asia.buyers.R.id.txt_order_detail_swipe_indexer)
        val pageIndex = (position + 1).toString() + " Of " + pastOrderList?.size
        txtSwipeIndexer.setText(pageIndex)
        setTypefaceView(txtSwipeIndexer, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        pager.currentItem = position
        pager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                val pageIndex = (position + 1).toString() + " Of " + pastOrderList?.size
                txtSwipeIndexer.setText(pageIndex)
            }

            override fun onPageSelected(position: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}
        })
        return v
    }

    companion object {
        private val INSERT_DRAWABLE_TOP_WIDTH = CommonMethods.dpToPx(100)
        private val INSERT_DRAWABLE_BOTTOM_WIDTH = CommonMethods.dpToPx(20)

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment OrderDetailDialogFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(
            pastOrderList: String?,
            position: Int,
            horizontalRowId: Int
        ): OrderDetailDialogFragment {
            val fragment = OrderDetailDialogFragment()
            val bundle = Bundle()
            bundle.putString("PAST_ORDER_LIST_DATA", pastOrderList)
            bundle.putInt("SELECTED_PAST_ORDER_POSITION", position)
            bundle.putInt("SELECTED_HORIZONTAL_ROW_ID", horizontalRowId)
            fragment.arguments = bundle
            return fragment
        }
    }
}