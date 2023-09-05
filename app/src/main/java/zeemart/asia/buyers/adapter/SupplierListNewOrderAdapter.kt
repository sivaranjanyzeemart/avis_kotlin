package zeemart.asia.buyers.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.UserPermission.HasManageSuppliers
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel
import zeemart.asia.buyers.orders.OrderSettingsActivity
import java.util.*
import java.util.regex.Pattern

/**
 * Created by ParulBhandari on 12/7/2017.
 */
class SupplierListNewOrderAdapter(
    private val context: Context,
    private val supplierList: List<DetailSupplierDataModel>,
    supplierClickListener: SupplierClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    private var filterSuppliers: List<DetailSupplierDataModel>
    private var searchedString = ""
    private val supplierClickListener: SupplierClickListener

    init {
        filterSuppliers = supplierList
        this.supplierClickListener = supplierClickListener
    }

    override fun getItemViewType(position: Int): Int {
        return if (filterSuppliers[position].isFavoriteProducts) FAVORITE_VIEW_TYPE else if (filterSuppliers[position].isFillToPar
        ) BELOW_PAR_VIEW_TYPE else SUPPLIER_VIEW_TYPE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == FAVORITE_VIEW_TYPE) {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.lyt_browse_fav_products, parent, false)
            ViewHolderFavorite(itemView)
        } else if (viewType == BELOW_PAR_VIEW_TYPE) {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_supplier_list_new_order, parent, false)
            ViewHolder(itemView)
        } else {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_supplier_list_new_order, parent, false)
            ViewHolder(itemView)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        if (getItemViewType(position) == FAVORITE_VIEW_TYPE) {
            val holderFav = holder as ViewHolderFavorite
            for (i in filterSuppliers.indices) {
                if (filterSuppliers[i].isFillToPar) {
//                    RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 62);
//                    relativeParams.setMargins(0, 0, 0, 0);
//                    holderFav.lytFav.setLayoutParams(relativeParams);
                    break
                }
            }
            holderFav.itemView.setOnClickListener {
                supplierClickListener.onItemSelected(
                    filterSuppliers[position]
                )
            }
        } else if (getItemViewType(position) == BELOW_PAR_VIEW_TYPE) {
            val holderSupplier = holder as ViewHolder
            val relativeParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            relativeParams.setMargins(0, 2, 0, 3)
            holderSupplier.lytSupplierList.layoutParams = relativeParams
            holderSupplier.txtSupplierName.text = context.getString(R.string.txt_below_par_items)
            holderSupplier.txtDateType.text = filterSuppliers[position].supplier.shortDesc
            holderSupplier.txtSupplierName.setTextColor(context.resources.getColor(R.color.pinky_red))
            holderSupplier.imgSupplier.setImageDrawable(context.resources.getDrawable(R.drawable.below_par_icon))
            holderSupplier.lytSupplierList.setOnClickListener {
                supplierClickListener.onItemSelected(
                    filterSuppliers[position]
                )
            }
        } else {
            val holderSupplier = holder as ViewHolder
            if (searchedString == "") {
                holderSupplier.txtSupplierName.text =
                    filterSuppliers[position].supplier.supplierName
            } else {
                val itemName = filterSuppliers[position].supplier.supplierName + ""
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
                holderSupplier.txtSupplierName.text = sb
            }
            if (filterSuppliers[position].isOrderDisabled()) {
                if (!StringHelper.isStringNullOrEmpty(filterSuppliers[position].disableOrderingReason)) {
                    if (filterSuppliers[position].disableOrderingReason == ApiParamsHelper.OrderDisablingReason.ORDER_SETTINGS_UN_VERIFIED.value) {
                        holderSupplier.txtDateType.text =
                            context.resources.getString(R.string.txt_unavailable_order_settings_un_verified)
                    } else if (filterSuppliers[position].disableOrderingReason == ApiParamsHelper.OrderDisablingReason.PAYMENT_OUT_STANDING.value) {
                        holderSupplier.txtDateType.text =
                            context.resources.getString(R.string.txt_unavailable_payment_out_standing)
                    }
                } else {
                    holderSupplier.txtDateType.text =
                        context.resources.getString(R.string.txt_currently_unavailable)
                }
                holderSupplier.itemView.alpha = 0.5f
            } else if (filterSuppliers[position].deliveryDates != null && filterSuppliers[position].deliveryDates!!.size > 0) {
                holderSupplier.txtDateType.text = String.format(
                    context.getString(R.string.format_next_delivery),
                    DateHelper.getDateInDateMonthFormat(
                        filterSuppliers[position].deliveryDates!![0].deliveryDate
                    )
                )
                holderSupplier.itemView.alpha = 1f
            }
            holderSupplier.lytSupplierList.setOnClickListener {
                if (filterSuppliers[position].isOrderDisabled()) {
                    if (!StringHelper.isStringNullOrEmpty(filterSuppliers[position].disableOrderingReason) && filterSuppliers[position].disableOrderingReason == ApiParamsHelper.OrderDisablingReason.ORDER_SETTINGS_UN_VERIFIED.value) {
                        if (HasManageSuppliers()) {
                            ShowOrderSettingsVerifyDialog()
                        } else {
                            ShowOrderSettingsNormalDialog()
                        }
                    } else {
                        val DIALOG_LAYOUT_WIDTH = CommonMethods.dpToPx(250)
                        val builder = AlertDialog.Builder(
                            context
                        )
                        val inflater =
                            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val view = inflater.inflate(R.layout.supplier_unavailable_dialog, null)
                        builder.setView(view)
                        builder.setCancelable(false)
                        val txtHeading = view.findViewById<TextView>(R.id.txt_small_dialog_failure)
                        txtHeading.setText(R.string.txt_unavailable_supplier)
                        val txtMessage =
                            view.findViewById<TextView>(R.id.txt_small_dialog_failure_message)
                        txtMessage.setText(R.string.txt_unavailable_supplier_msg)
                        val btnOK = view.findViewById<Button>(R.id.btn_ok)
                        btnOK.setText(R.string.dialog_ok_button_text)
                        setTypefaceView(
                            txtHeading,
                            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
                        )
                        setTypefaceView(
                            txtMessage,
                            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
                        )
                        setTypefaceView(
                            btnOK,
                            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
                        )
                        val dialog = builder.create()
                        dialog.show()
                        dialog.window!!.setLayout(
                            DIALOG_LAYOUT_WIDTH,
                            WindowManager.LayoutParams.WRAP_CONTENT
                        )
                        btnOK.setOnClickListener { dialog.dismiss() }
                    }
                } else {
                    supplierClickListener.onItemSelected(filterSuppliers[position])
                }
            }
            if (StringHelper.isStringNullOrEmpty(filterSuppliers[position].supplier.logoURL)) {
                holderSupplier.imgSupplier.visibility = View.INVISIBLE
                holderSupplier.lytSupplierThumbNail.visibility = View.VISIBLE
                holderSupplier.lytSupplierThumbNail.setBackgroundColor(
                    CommonMethods.SupplierThumbNailBackgroundColor(
                        filterSuppliers[position].supplier.supplierName!!, context
                    )
                )
                holderSupplier.txtSupplierThumbNail.text =
                    CommonMethods.SupplierThumbNailShortCutText(
                        filterSuppliers[position].supplier.supplierName!!
                    )
                holderSupplier.txtSupplierThumbNail.setTextColor(
                    CommonMethods.SupplierThumbNailTextColor(
                        filterSuppliers[position].supplier.supplierName!!, context
                    )
                )
            } else {
                holderSupplier.imgSupplier.visibility = View.VISIBLE
                holderSupplier.lytSupplierThumbNail.visibility = View.GONE
                Picasso.get().load(filterSuppliers[position].supplier.logoURL)
                    .placeholder(R.drawable.placeholder_all).resize(
                    ViewHolder.PLACE_HOLDER_SUPPLIER_IMAGE_WIDTH,
                    ViewHolder.PLACE_HOLDER_SUPPLIER_IMAGE_HEIGHT
                ).into(holderSupplier.imgSupplier)
            }
            if (position == filterSuppliers.size - 1) {
                holderSupplier.lytMoreThanDays.alpha = 1f
                holderSupplier.lytMoreThanDays.visibility = View.VISIBLE
            } else {
                holderSupplier.lytMoreThanDays.visibility = View.GONE
            }
            holderSupplier.lytMoreThanDays.isClickable = false
        }
    }

    override fun getItemCount(): Int {
        return filterSuppliers.size
    }

    override fun getFilter(): Filter {
        return FilterSuppliers()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgSupplier: ImageView
        var txtSupplierName: TextView
        var txtDateType: TextView
        var lytSupplierList: RelativeLayout
        var lytMoreThanDays: RelativeLayout
        var txtMoreThanDays: TextView
        var lytSupplierThumbNail: RelativeLayout
        var txtSupplierThumbNail: TextView

        init {
            imgSupplier = itemView.findViewById(R.id.img_supplier_new_order)
            txtSupplierName = itemView.findViewById(R.id.txt_supplier_name_new_order)
            setTypefaceView(txtSupplierName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            txtDateType = itemView.findViewById(R.id.txt_date_type_new_order)
            setTypefaceView(txtDateType, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            lytMoreThanDays = itemView.findViewById(R.id.lyt_more_than)
            txtMoreThanDays = itemView.findViewById(R.id.txt_more_than)
            setTypefaceView(txtMoreThanDays, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            lytSupplierList = itemView.findViewById(R.id.layout_supplier_list)
            lytSupplierThumbNail = itemView.findViewById(R.id.lyt_supplier_thumbnail)
            txtSupplierThumbNail = itemView.findViewById(R.id.txt_supplier_thumbnail)
            setTypefaceView(
                txtSupplierThumbNail,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
        }

        companion object {
            val PLACE_HOLDER_SUPPLIER_IMAGE_WIDTH = CommonMethods.dpToPx(30)
            val PLACE_HOLDER_SUPPLIER_IMAGE_HEIGHT = CommonMethods.dpToPx(30)
        }
    }

    inner class ViewHolderFavorite(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgSupplier: ImageView? = null
        var txtFav: TextView
        var lytFav: RelativeLayout

        init {
            txtFav = itemView.findViewById(R.id.txt_favorites_browse_new_order)
            lytFav = itemView.findViewById(R.id.lyt_favorites_browse_new_order)
            setTypefaceView(txtFav, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        }
    }

    private inner class FilterSuppliers : Filter() {
        override fun performFiltering(charSequence: CharSequence): FilterResults {
            val filterResults = FilterResults()
            if (charSequence != null && charSequence.length > 0) {
                searchedString = charSequence as String
                val tempList = ArrayList<DetailSupplierDataModel>()

                // search content in friend list
                Log.d("key", charSequence.toString())
                for (i in supplierList.indices) {
                    if (!supplierList[i].isFavoriteProducts && supplierList[i].supplier.supplierName!!.lowercase(
                            Locale.getDefault()
                        ).contains(charSequence.toString().lowercase(Locale.getDefault()))
                    ) {
                        tempList.add(supplierList[i])
                    }
                }
                filterResults.count = tempList.size
                filterResults.values = tempList
            } else {
                searchedString = ""
                filterResults.count = supplierList.size
                filterResults.values = supplierList
            }
            return filterResults
        }

        override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
            filterSuppliers = filterResults.values as ArrayList<DetailSupplierDataModel>
            notifyDataSetChanged()
        }
    }

    interface SupplierClickListener {
        fun onItemSelected(supplier: DetailSupplierDataModel?)
    }

    fun ShowOrderSettingsVerifyDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setPositiveButton(R.string.txt_verify_now) { dialog, which ->
            dialog.dismiss()
            val intent = Intent(context, OrderSettingsActivity::class.java)
            context.startActivity(intent)
        }
        builder.setNegativeButton(R.string.txt_cancel) { dialog, which -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.setTitle(context.getString(R.string.txt_verify_alert_title))
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setMessage(context.resources.getString(R.string.txt_verify_alert_message))
        dialog.show()
        val negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        negativeButton.setTextColor(context.resources.getColor(R.color.pinky_red))
        val positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
        positiveButton.setTextColor(context.resources.getColor(R.color.color_azul_two))
    }

    fun ShowOrderSettingsNormalDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setPositiveButton(context.resources.getString(R.string.dialog_ok_button_text)) { dialog, which -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.setTitle(context.resources.getString(R.string.txt_order_settings_alert_title))
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setMessage(context.resources.getString(R.string.txt_order_settings_alert_message))
        dialog.show()
        val positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
        positiveButton.setTextColor(context.resources.getColor(R.color.text_blue))
    }

    companion object {
        private const val SUPPLIER_VIEW_TYPE = 1
        private const val FAVORITE_VIEW_TYPE = 2
        private const val BELOW_PAR_VIEW_TYPE = 3
    }
}