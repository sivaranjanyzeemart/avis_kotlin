package zeemart.asia.buyers.adapter

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.reportsimport.ReportsSearchUi
import java.util.regex.Pattern

/**
 * Created by Muthumari on 24/2/2020.
 */
class ReportsSearchSupplierListAdapter(
    private val context: Context,
    lstSearchData: ArrayList<ReportsSearchUi>,
    private val listener: ItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    private val searchedString = ""
    var lstSearchDataArraylist: ArrayList<ReportsSearchUi>

    init {
        lstSearchDataArraylist = lstSearchData
    }

    override fun getItemViewType(position: Int): Int {
        val data: ReportsSearchUi = lstSearchDataArraylist[position]
        return if (data.isHeader) {
            VIEW_TYPE_HEADER
        } else if (data.detailSupplierDataModel != null) {
            SUPPLIER_VIEW_TYPE
        } else if (data.products != null) {
            PRODUCT_VIEW_TYPE
        } else {
            0
        }
    }

    override fun getItemCount(): Int {
        return lstSearchDataArraylist.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val itemView: View = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_header_dashboard, parent, false)
            ViewHolderHeader(itemView)
        } else if (viewType == SUPPLIER_VIEW_TYPE) {
            val itemView: View = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_report_search_supplier_list, parent, false)
            ViewHolderSupplier(itemView)
        } else {
            val itemView: View = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_report_search_sku_list, parent, false)
            ViewHolderProducts(itemView)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (lstSearchDataArraylist[position].isHeader) {
            val viewHolderHeader = holder as ViewHolderHeader
            viewHolderHeader.txtHeader.setText(lstSearchDataArraylist[position].header)
        } else if (lstSearchDataArraylist[position].detailSupplierDataModel != null) {
            val viewHolderSupplier = holder as ViewHolderSupplier
            if (searchedString == "") {
                viewHolderSupplier.txtSupplierName.setText(
                    lstSearchDataArraylist[position].detailSupplierDataModel?.supplier
                        ?.supplierName
                )
            } else {
                val itemName: String =
                    lstSearchDataArraylist[position].detailSupplierDataModel?.supplier
                        ?.supplierName + ""
                val sb = SpannableStringBuilder(itemName)
                val p = Pattern.compile(searchedString, Pattern.LITERAL or Pattern.CASE_INSENSITIVE)
                val m = p.matcher(itemName)
                while (m.find()) {
                    sb.setSpan(
                        ForegroundColorSpan(context.resources.getColor(R.color.text_blue)),
                        m.start(),
                        m.end(),
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE
                    )
                }
                viewHolderSupplier.txtSupplierName.setText(sb)
            }
            viewHolderSupplier.lytSupplierList.setOnClickListener(View.OnClickListener {
                listener.onItemClick(
                    lstSearchDataArraylist[position].detailSupplierDataModel?.supplier
                        ?.supplierId,
                    lstSearchDataArraylist[position].detailSupplierDataModel?.supplier
                        ?.supplierName
                )
            })
            if (StringHelper.isStringNullOrEmpty(
                    lstSearchDataArraylist[position].detailSupplierDataModel?.supplier
                        ?.logoURL
                )
            ) {
                viewHolderSupplier.imgSupplier.visibility = View.INVISIBLE
                viewHolderSupplier.lytSupplierThumbNail.setVisibility(View.VISIBLE)
                viewHolderSupplier.lytSupplierThumbNail.setBackgroundColor(
                    CommonMethods.SupplierThumbNailBackgroundColor(
                        lstSearchDataArraylist[position].detailSupplierDataModel?.supplier
                            ?.supplierName!!, context
                    )
                )
                viewHolderSupplier.txtSupplierThumbNail.setText(
                    CommonMethods.SupplierThumbNailShortCutText(
                        lstSearchDataArraylist[position].detailSupplierDataModel?.supplier
                            ?.supplierName!!
                    )
                )
                viewHolderSupplier.txtSupplierThumbNail.setTextColor(
                    CommonMethods.SupplierThumbNailTextColor(
                        lstSearchDataArraylist[position].detailSupplierDataModel?.supplier
                            ?.supplierName!!, context
                    )
                )
            } else {
                viewHolderSupplier.imgSupplier.visibility = View.VISIBLE
                viewHolderSupplier.lytSupplierThumbNail.setVisibility(View.GONE)
                Picasso.get().load(
                    lstSearchDataArraylist[position].detailSupplierDataModel?.supplier
                        ?.logoURL
                ).placeholder(
                    R.drawable.placeholder_all
                ).resize(
                    ViewHolderSupplier.PLACE_HOLDER_SUPPLIER_IMAGE_WIDTH,
                    ViewHolderSupplier.PLACE_HOLDER_SUPPLIER_IMAGE_HEIGHT
                ).into(viewHolderSupplier.imgSupplier)
            }
        } else if (lstSearchDataArraylist[position].products != null) {
            val viewHolderProducts = holder as ViewHolderProducts
            val params: ConstraintLayout.LayoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ViewHolderProducts.CONSTRAINT_LAYOUT_HEIGHT
            )
            viewHolderProducts.lytProductName.setLayoutParams(params)
            if (searchedString == "") {
                viewHolderProducts.txtItemName.setText(
                    lstSearchDataArraylist[position].products?.productName
                )
            } else {
                val itemName: String =
                    lstSearchDataArraylist[position].products?.productName + ""
                val sb = SpannableStringBuilder(itemName)
                val p = Pattern.compile(searchedString, Pattern.LITERAL or Pattern.CASE_INSENSITIVE)
                val m = p.matcher(itemName)
                while (m.find()) {
                    sb.setSpan(
                        ForegroundColorSpan(context.resources.getColor(R.color.text_blue)),
                        m.start(),
                        m.end(),
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE
                    )
                }
                viewHolderProducts.txtItemName.setText(sb)
            }
            if (!StringHelper.isStringNullOrEmpty(
                    lstSearchDataArraylist[position].products?.supplierName
                )
            ) viewHolderProducts.txtSupplierName.setText(
                lstSearchDataArraylist[position].products?.supplierName
            )
            viewHolderProducts.lytProductName.setOnClickListener(View.OnClickListener {
                listener.onSKUItemClick(
                    lstSearchDataArraylist[position].products?.sku,
                    lstSearchDataArraylist[position].products?.productName
                )
            })
        }
    }

//    val itemCount: Int
//        get() = lstSearchDataArraylist.size

    inner class ViewHolderHeader(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtHeader: TextView

        init {
            txtHeader = itemView.findViewById<TextView>(R.id.txt_dashboard_sublist_header)
            txtHeader.setTextColor(context.resources.getColor(R.color.black))
            ZeemartBuyerApp.setTypefaceView(
                txtHeader,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            txtHeader.setTextSize(16f)
            val params: LinearLayout.LayoutParams =
                txtHeader.getLayoutParams() as LinearLayout.LayoutParams
            params.setMargins(15, 0, 0, 0)
            txtHeader.setLayoutParams(params)
        }
    }

    class ViewHolderSupplier(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgSupplier: ImageView
        var txtSupplierName: TextView
        var lytSupplierList: RelativeLayout
        var lytSupplierThumbNail: RelativeLayout
        var txtSupplierThumbNail: TextView

        init {
            imgSupplier = itemView.findViewById(R.id.img_search_supplier)
            txtSupplierName = itemView.findViewById<TextView>(R.id.txt_supplier_name_report_search)
            ZeemartBuyerApp.setTypefaceView(
                txtSupplierName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            lytSupplierList =
                itemView.findViewById<RelativeLayout>(R.id.layout_supplier_list_search)
            lytSupplierThumbNail =
                itemView.findViewById<RelativeLayout>(R.id.lyt_supplier_thumbnail)
            txtSupplierThumbNail = itemView.findViewById<TextView>(R.id.txt_supplier_thumbnail)
            ZeemartBuyerApp.setTypefaceView(
                txtSupplierThumbNail,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
        }

        companion object {
            val PLACE_HOLDER_SUPPLIER_IMAGE_WIDTH: Int = CommonMethods.dpToPx(36)
            val PLACE_HOLDER_SUPPLIER_IMAGE_HEIGHT: Int = CommonMethods.dpToPx(36)
        }
    }

    class ViewHolderProducts(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtItemName: TextView
        val txtSupplierName: TextView
        val lytProductName: RelativeLayout

        init {
            lytProductName = itemView.findViewById<RelativeLayout>(R.id.layout_supplier_list_search)
            txtItemName = itemView.findViewById<TextView>(R.id.txt_sku_name_report_search)
            txtSupplierName = itemView.findViewById<TextView>(R.id.txt_sku_description)
            ZeemartBuyerApp.setTypefaceView(
                txtItemName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtSupplierName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }

        companion object {
            val CONSTRAINT_LAYOUT_HEIGHT: Int = CommonMethods.dpToPx(66)
        }
    }

    interface ItemClickListener {
        fun onItemClick(item: String?, SupplierName: String?)
        fun onSKUItemClick(item: String?, SKUName: String?)
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 1
        private const val SUPPLIER_VIEW_TYPE = 2
        private const val PRODUCT_VIEW_TYPE = 3
    }
}