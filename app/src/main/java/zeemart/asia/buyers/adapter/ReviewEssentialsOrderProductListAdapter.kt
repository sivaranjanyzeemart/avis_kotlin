package zeemart.asia.buyers.adapter

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.essentials.ReviewEssentialsActivity
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.DealProductQuantityChangeDialogHelper
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.helperimportimport.CustomChangeQuantityDialog
import zeemart.asia.buyers.models.PriceDetails
import zeemart.asia.buyers.models.Supplier
import zeemart.asia.buyers.models.UnitSizeModel
import zeemart.asia.buyers.models.marketlist.Product

/**
 * Created by ParulBhandari on 12/15/2017.
 */
class ReviewEssentialsOrderProductListAdapter(
    private val context: Context,
    private val reviewOrderItemChangeListener: ReviewEssentialsOrderItemChangeListener,
    private val products: MutableList<Product>,
    private val supplierDetails: Supplier
) : RecyclerView.Adapter<ReviewEssentialsOrderProductListAdapter.ViewHolder>(),
    DealProductQuantityChangeDialogHelper.ProductDataChangeListener {
    private lateinit var dialog: CustomChangeQuantityDialog
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_review_order_product_row, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (!StringHelper.isStringNullOrEmpty(products[position].customName)) {
            holder.txtProductCustomName.visibility = View.VISIBLE
            holder.txtProductCustomName.text = products[position].productName
            holder.txtItemName.text = products[position].customName
        } else {
            holder.txtItemName.text = products[position].productName
            holder.txtProductCustomName.visibility = View.GONE
        }
        holder.btnReduceQuantity.visibility = View.VISIBLE
        holder.btnIncreaseQuantity.visibility = View.VISIBLE
        setItemPriceText(position, holder)
        holder.edtProductQuantity.setBackgroundResource(R.color.faint_grey)
        holder.edtProductQuantity.setTextColor(context.resources.getColor(R.color.black))
        val measure = UnitSizeModel.returnShortNameValueUnitSize(products[position].unitSize)
        holder.edtProductQuantity.text = products[position].quantityDisplayValue + " "
        holder.edtProductQuantity.append(changeTextFontSize(measure!!, products[position].quantity))
        if (products[position].totalPrice == null) {
            calculateTotalPriceQuantityChanged(position)
            setTotalPriceText(position, holder)
        } else {
            setTotalPriceText(position, holder)
        }
        holder.btnIncreaseQuantity.setOnClickListener {
            val quantity = products[position].quantity + 1
            products[position].quantity = (quantity)
            incDecQuantityClick(position, holder, quantity)
        }
        holder.btnReduceQuantity.setOnClickListener {
            var quantity = products[position].quantity - 1
            if (quantity < 0) {
                quantity = 0.0
            }
            products[position].quantity = (quantity)
            incDecQuantityClick(position, holder, quantity)
        }

        //check for the quantity
        if (products[position].quantity == 0.0) {
            holder.btnDelete.setImageResource(R.drawable.delete_blue)
            holder.btnReduceQuantity.visibility = View.INVISIBLE
            holder.edtProductQuantity.setTextColor(context.resources.getColor(R.color.pinky_red))

            //display quantity cannot be 0 message
            val message = context.getString(R.string.txt_bold_quantity_cannot_be_0)
            displayErrorMessages(holder, message, position)
        } else if (products[position].quantity < products[position].moq!!) {
            holder.btnDelete.setImageResource(R.drawable.trash_grey)
            holder.btnReduceQuantity.visibility = View.VISIBLE
            //used to display the moq
            val moqVal = products[position].moqDisplayValue
            val txtStockMessageText = String.format(
                context.getString(R.string.format_moq_is),
                moqVal,
                UnitSizeModel.returnShortNameValueUnitSize(
                    products[position].unitSize
                )
            )
            displayErrorMessages(holder, txtStockMessageText, position)
        } else {
            holder.btnDelete.setImageResource(R.drawable.trash_grey)
            holder.btnReduceQuantity.visibility = View.VISIBLE
            holder.btnReduceQuantity.visibility = View.VISIBLE
            //hide MOQ message
            val quantityProduct = products[position].quantityDisplayValue
            hideErrorMessages(
                holder,
                quantityProduct,
                products[position].quantity,
                products[position].unitSize,
                position
            )
        }
        setUIFont(holder)
        holder.btnDelete.setOnClickListener {
            AnalyticsHelper.logAction(context, AnalyticsHelper.TAP_ORDER_REVIEW_DELETE_PRODUCT)
            val builder = AlertDialog.Builder(
                context
            )
            builder.setPositiveButton(context.getString(R.string.txt_yes)) { dialog, which ->
                dialog.dismiss()
                products.removeAt(position)
                notifyDataSetChanged()
                //push the data to the review order activity
                reviewOrderItemChangeListener.onItemQuantityChanged(products)
            }
            builder.setNegativeButton(context.getString(R.string.dialog_cancel_button_text)) { dialog, which -> dialog.dismiss() }
            val dialog = builder.create()
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setTitle(context.getString(R.string.txt_delete_product))
            dialog.setMessage(context.getString(R.string.txt_delete_the_product))
            dialog.show()
        }

        //call the changeQuantity dialog
        holder.edtProductQuantity.setOnClickListener {
            if (!products[position].isProductManuallyAdded) {
                if (dialog == null || dialog != null && !dialog!!.isShowing) {
                    val createOrderHelperDialog = DealProductQuantityChangeDialogHelper(
                        products[position], context, this@ReviewEssentialsOrderProductListAdapter
                    )
                    dialog = createOrderHelperDialog.createChangeQuantityDialog()
                    dialog.getWindow()!!
                        .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                }
            }
        }
        holder.imgNotes.setOnClickListener { createAddNoteDialog(products[position]) }
        holder.txtAddNotes.setOnClickListener {
            AnalyticsHelper.logAction(context, AnalyticsHelper.TAP_ORDER_REVIEW_ADD_NOTES)
            createAddNoteDialog(products[position])
        }
        if (StringHelper.isStringNullOrEmpty(products[position].notes)) {
            holder.imgNotes.setImageResource(R.drawable.notesgrey)
            holder.txtAddNotes.text = context.getString(R.string.txt_add_notes)
            holder.txtAddNotes.setTextColor(context.resources.getColor(R.color.grey_medium))
        } else {
            holder.imgNotes.setImageResource(R.drawable.notesblue)
            holder.txtAddNotes.text = context.getString(R.string.notes)
            holder.txtAddNotes.setTextColor(context.resources.getColor(R.color.text_blue))
        }
    }

    private fun incDecQuantityClick(position: Int, holder: ViewHolder, quantity: Double) {
        if (quantity == 0.0) {
            holder.btnDelete.setImageResource(R.drawable.delete_blue)
            holder.btnReduceQuantity.visibility = View.INVISIBLE
            //display quantity cannot be 0 message
            val message = context.getString(R.string.txt_bold_quantity_cannot_be_0)
            displayErrorMessages(holder, message, position)
        } else if (quantity < products[position].moq!!) {
            holder.btnReduceQuantity.visibility = View.VISIBLE
            holder.btnDelete.setImageResource(R.drawable.trash_grey)
            //used to display the moq
            val moqVal = products[position].moqDisplayValue
            val txtStockMessageText = String.format(
                context.getString(R.string.format_moq_is),
                moqVal,
                UnitSizeModel.returnShortNameValueUnitSize(
                    products[position].unitSize
                )
            )
            displayErrorMessages(holder, txtStockMessageText, position)
        } else {
            holder.btnDelete.setImageResource(R.drawable.trash_grey)
            holder.btnReduceQuantity.visibility = View.VISIBLE
            //hide MOQ message
            val quantityProduct = products[position].quantityDisplayValue
            hideErrorMessages(
                holder,
                quantityProduct,
                products[position].quantity,
                products[position].unitSize,
                position
            )
        }
        calculateTotalPriceQuantityChanged(position)
        holder.txtTotalPrice.text = products[position].totalPrice?.displayValue
        //push the data to the review order activity
        reviewOrderItemChangeListener.onItemQuantityChanged(products)
    }

    private fun setTotalPriceText(position: Int, holder: ViewHolder) {
        if (!StringHelper.isStringNullOrEmpty(products[position].totalPrice?.displayValue)) {
            holder.txtTotalPrice.visibility = View.VISIBLE
            holder.txtTotalPrice.text = products[position].totalPrice?.displayValue
        } else {
            holder.txtTotalPrice.visibility = View.GONE
        }
    }

    private fun setItemPriceText(position: Int, holder: ViewHolder) {
        val itemPrice = products[position].unitPrice?.getDisplayValueWithUomAndBullet(
            UnitSizeModel.returnShortNameValueUnitSize(
                products[position].unitSize
            )
        )
        if (!StringHelper.isStringNullOrEmpty(itemPrice)) {
            holder.txtItemPerPrice.visibility = View.VISIBLE
            holder.txtItemPerPrice.text = itemPrice
        } else {
            holder.txtItemPerPrice.visibility = View.GONE
        }
    }

    /**
     * calculates the total price by multiplying unitPrice and quantity
     * round up the totalprice if in decimal
     * save the rounded cent value to the total Price object
     *
     * @param position
     */
    private fun calculateTotalPriceQuantityChanged(position: Int) {
        val totalPrice = products[position].unitPrice?.amount?.times(products[position].quantity)
        //Create new total PriceDetail Object
        val totPrice = PriceDetails()
        totPrice.currencyCode = (products[position].unitPrice?.currencyCode)
        totPrice.amount = totalPrice
        products[position].totalPrice = totPrice
    }

    override fun getItemCount(): Int {
        return products.size
    }

    override fun onDataChange(product: Product?) {
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtItemName: TextView
        var txtItemPerPrice: TextView
        var btnReduceQuantity: ImageButton
        var btnIncreaseQuantity: ImageButton
        var txtTotalPrice: TextView
        var edtProductQuantity: TextView
        var btnDelete: ImageButton

        //TextView txtMoq;
        var imgNotes: ImageButton
        var txtAddNotes: TextView
        var lytStockMessage: LinearLayout
        var txtStockMessage: TextView
        var imgWarning: ImageView
        var txtProblematicProductIndicator: TextView
        var txtProductCustomName: TextView

        init {
            txtItemName = itemView.findViewById(R.id.txt_product_name)
            btnDelete = itemView.findViewById(R.id.btn_delete)
            txtTotalPrice = itemView.findViewById(R.id.txt_per_price)
            btnReduceQuantity = itemView.findViewById(R.id.btn_reduce_quantity)
            btnIncreaseQuantity = itemView.findViewById(R.id.btn_inc_quantity)
            edtProductQuantity = itemView.findViewById(R.id.txt_quantity_value)
            txtItemPerPrice = itemView.findViewById(R.id.txt_price_product)
            //txtMoq = itemView.findViewById(R.id.txt_moq_text);
            imgNotes = itemView.findViewById(R.id.img_add_notes)
            txtAddNotes = itemView.findViewById(R.id.txt_add_notes)
            lytStockMessage = itemView.findViewById(R.id.lyt_stock_message)
            txtStockMessage = itemView.findViewById(R.id.txt_stock_message)
            imgWarning = itemView.findViewById(R.id.img_warning)
            txtProblematicProductIndicator =
                itemView.findViewById(R.id.problematic_product_indicator)
            txtProductCustomName = itemView.findViewById(R.id.txt_item_custom_name)
            ZeemartBuyerApp.setTypefaceView(
                txtAddNotes,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }
    }

    private fun changeTextFontSize(text: String, quantity: Double): SpannableString {
        val unitSize = UnitSizeModel.returnShortNameValueUnitSize(text)
        val ss1 = SpannableString(unitSize)
        ss1.setSpan(AbsoluteSizeSpan(12, true), 0, ss1.length, 0)
        if (quantity == 0.0) {
            ss1.setSpan(
                ForegroundColorSpan(context.resources.getColor(R.color.pinky_red)),
                0,
                ss1.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } else {
            ss1.setSpan(
                ForegroundColorSpan(context.resources.getColor(R.color.text_blue)),
                0,
                ss1.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return ss1
    }

    private fun changeTextFontSize(text: String, isError: Boolean): SpannableString {
        val unitSize = UnitSizeModel.returnShortNameValueUnitSize(text)
        val ss1 = SpannableString(unitSize)
        ss1.setSpan(AbsoluteSizeSpan(12, true), 0, ss1.length, 0)
        if (isError) {
            ss1.setSpan(
                ForegroundColorSpan(context.resources.getColor(R.color.pinky_red)),
                0,
                ss1.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } else {
            ss1.setSpan(
                ForegroundColorSpan(context.resources.getColor(R.color.text_blue)),
                0,
                ss1.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return ss1
    }

    private fun setUIFont(holder: ViewHolder) {
        ZeemartBuyerApp.setTypefaceView(
            holder.edtProductQuantity,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            holder.txtItemName,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            holder.txtItemPerPrice,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            holder.txtTotalPrice,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        //ZeemartBuyerApp.setTypefaceView(holder.txtMoq,ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR);
        ZeemartBuyerApp.setTypefaceView(
            holder.txtStockMessage,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            holder.txtProductCustomName,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_ITALICS
        )
    }

    private fun createAddNoteDialog(product: Product) {
        val dialog = Dialog(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)
        dialog.window!!.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
        dialog.setContentView(R.layout.layout_add_product_notes)
        val edtNotes = dialog.findViewById<EditText>(R.id.edt_notes_product)
        ZeemartBuyerApp.setTypefaceView(
            edtNotes,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val txtNotesHeading = dialog.findViewById<TextView>(R.id.textView5)
        ZeemartBuyerApp.setTypefaceView(
            txtNotesHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        if (product.notes != null && product.notes!!.length > 0) {
            edtNotes.setText(product.notes)
        }
        val btnDone = dialog.findViewById<Button>(R.id.btn_add_notes_done)
        ZeemartBuyerApp.setTypefaceView(
            btnDone,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        btnDone.setOnClickListener {
            product.notes = edtNotes.text.toString()
            dialog.dismiss()
            notifyDataSetChanged()
        }
        dialog.show()
    }

    /**
     * hides the error messages
     *
     * @param holder
     * @param quantity
     * @param unitSize
     */
    private fun hideErrorMessages(
        holder: ViewHolder,
        quantityDisplay: String,
        quantity: Double?,
        unitSize: String,
        position: Int
    ) {
        holder.lytStockMessage.visibility = View.GONE
        holder.txtProblematicProductIndicator.visibility = View.INVISIBLE
        setItemPriceText(position, holder)
        setTotalPriceText(position, holder)
        holder.edtProductQuantity.setBackgroundResource(R.color.faint_grey)
        holder.edtProductQuantity.setTextColor(context.resources.getColor(R.color.black))
        holder.edtProductQuantity.text = "$quantityDisplay "
        holder.edtProductQuantity.append(changeTextFontSize(unitSize, false))
        if (quantity != null) {
            if (quantity == 0.0) {
                holder.btnReduceQuantity.visibility = View.GONE
            } else {
                holder.btnReduceQuantity.visibility = View.VISIBLE
            }
        }
        products[position].isItemAvailable = true
        setPlaceOrderButtonState()
    }

    /**
     * set the place order button status
     */
    private fun setPlaceOrderButtonState() {
        var isActive = true
        for (i in products.indices) {
            if (!products[i].isItemAvailable) {
                isActive = false
            }
        }
        if (isActive) {
            //calls the review order method which checks for various supplier settings
            (context as ReviewEssentialsActivity).calculateTotalPrice()
        } else {
            (context as ReviewEssentialsActivity).calculateTotalPrice()
        }
    }

    /**
     * display the error message
     *
     * @param holder
     * @param message
     */
    private fun displayErrorMessages(holder: ViewHolder, message: String, position: Int) {
        holder.lytStockMessage.visibility = View.VISIBLE
        holder.txtProblematicProductIndicator.visibility = View.VISIBLE
        holder.txtItemPerPrice.visibility = View.GONE
        holder.txtTotalPrice.visibility = View.GONE
        holder.txtStockMessage.text = Html.fromHtml(message)
        holder.edtProductQuantity.setBackgroundResource(R.drawable.pinky_rect_border)
        holder.edtProductQuantity.setTextColor(context.resources.getColor(R.color.pinky_red))
        holder.edtProductQuantity.text = products[position].quantityDisplayValue + " "
        holder.edtProductQuantity.append(changeTextFontSize(products[position].unitSize, true))
        if (products[position].quantity != null) {
            val quantityDouble = products[position].quantity
            if (quantityDouble == 0.0) {
                holder.btnReduceQuantity.visibility = View.GONE
            } else {
                holder.btnReduceQuantity.visibility = View.VISIBLE
            }
        }
        products[position].isItemAvailable = false
        setPlaceOrderButtonState()
    }

    interface ReviewEssentialsOrderItemChangeListener {
        fun onItemQuantityChanged(selectedProducts: List<Product>?)
    }
}