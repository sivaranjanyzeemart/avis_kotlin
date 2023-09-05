package zeemart.asia.buyers.helper

import android.content.Context
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastRed
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.ProductDataHelper.calculateTotalPriceChanged
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.helperimportimport.CustomChangeQuantityDialog
import zeemart.asia.buyers.models.UnitSizeModel.Companion.isDecimalAllowed
import zeemart.asia.buyers.models.UnitSizeModel.Companion.returnShortNameValueUnitSize
import zeemart.asia.buyers.models.marketlist.Product
import java.text.DecimalFormatSymbols

/**
 * Created by ParulBhandari on 8/13/2018.
 */
class DealProductQuantityChangeDialogHelper(
    private var product: Product?,
    private val context: Context,
    private val productDataChangeListener: ProductDataChangeListener
) {
    var tw: TextWatcher? = null
    var beforeTextChanged = ""

    interface ProductDataChangeListener {
        fun onDataChange(product: Product?)
    }

    /**
     * Create change product quantity dialog
     */
    fun createChangeQuantityDialog(): CustomChangeQuantityDialog {
        val customChangeQuantityDialog: CustomChangeQuantityDialog =
            object : CustomChangeQuantityDialog(
                context, R.style.CustomDialogTheme
            ) {
                override fun onUnitSizeSelectedCalled(d: CustomChangeQuantityDialog) {
                    val edtQuantity = d.findViewById<EditText>(R.id.edtChangeQuantity)
                    val txtErrorMessage = d.findViewById<TextView>(R.id.txt_error_message)
                    val lytReduceQuantity = d.findViewById<LinearLayout>(R.id.lyt_decrease_quantity)
                    if (isDecimalAllowed(product!!.unitSize)) {
                        edtQuantity.setText(product!!.moqEditValue)
                        edtQuantity.setSelection(edtQuantity.text.length)
                    } else {
                        edtQuantity.setText(product!!.moqEditValue)
                        edtQuantity.setSelection(edtQuantity.text.length)
                    }

                    //display the particular message
                    setTheMessagesForChangeQuantityDialog(
                        txtErrorMessage,
                        edtQuantity,
                        lytReduceQuantity
                    )
                }
            }
        customChangeQuantityDialog.setContentView(R.layout.layout_change_product_quantity)
        //RecyclerView lstOrderItems = customChangeQuantityDialog.findViewById(R.id.lst_unit_size);
        val imgDecQuantity = customChangeQuantityDialog.findViewById<ImageView>(R.id.img_dec_quant)
        //final TextView txtDDecQuantityMoq = customChangeQuantityDialog.findViewById(R.id.txt_dec_quant_moq);
        val btnIncreaseQuantity =
            customChangeQuantityDialog.findViewById<ImageButton>(R.id.btn_increase_quantity)
        val edtChangeQuantity =
            customChangeQuantityDialog.findViewById<EditText>(R.id.edtChangeQuantity)
        val txtErrorMessage =
            customChangeQuantityDialog.findViewById<TextView>(R.id.txt_error_message)
        val lytReduceQuantity =
            customChangeQuantityDialog.findViewById<LinearLayout>(R.id.lyt_decrease_quantity)
        val btnDone = customChangeQuantityDialog.findViewById<Button>(R.id.btnchange_quantity_done)
        val txtChangeQuantityHeader =
            customChangeQuantityDialog.findViewById<TextView>(R.id.txt_change_quantity_header)
        //set the font styling for change quantity dialog
        setTypefaceView(btnDone, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(edtChangeQuantity, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtErrorMessage, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(
            txtChangeQuantityHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        if (product != null) {
            if (isDecimalAllowed(product!!.unitSize)) {
                edtChangeQuantity.setText(product!!.quantityEditValue)
                edtChangeQuantity.setSelection(edtChangeQuantity.text.length)
            } else {
                edtChangeQuantity.setText(product!!.quantityEditValue)
                edtChangeQuantity.setSelection(edtChangeQuantity.text.length)
            }
        }
        tw = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                beforeTextChanged = s.toString()
                Log.e("TextWatcher", "beforeTextChanged$s")
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Log.e("TextWatcher", "onTextChanged$s")
                edtChangeQuantity.removeTextChangedListener(tw)
                try {
                    val symbols =
                        DecimalFormatSymbols(ZeemartAppConstants.Market.`this`.locale)
                    val sep = symbols.decimalSeparator
                    if (s.toString().length == 0) {
                        product!!.quantity = (0.0)
                    } else if (isDecimalAllowed(product!!.unitSize)) {
                        val `val` = s.toString().replace(",".toRegex(), ".").toDouble()
                        val splitter =
                            `val`.toString().split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                        if ((`val` < 0 || splitter.size == 2) && splitter[1].length > 3) {
                            throw NumberFormatException()
                        }
                        product!!.quantity = (`val`)
                    } else {
                        if (s.toString().replace(",".toRegex(), ".").contains(",")) {
                            throw NumberFormatException()
                        }
                        val `val` = s.toString().replace(",".toRegex(), ".").toInt()
                        if (`val` < 0) {
                            throw NumberFormatException()
                        }
                        product!!.quantity = (`val`.toDouble())
                    }
                    edtChangeQuantity.setText(
                        edtChangeQuantity.text.toString().replace(".", "" + sep)
                    )
                    edtChangeQuantity.addTextChangedListener(tw)
                    edtChangeQuantity.setSelection(start + count)
                    setTheMessagesForChangeQuantityDialog(
                        txtErrorMessage,
                        edtChangeQuantity,
                        lytReduceQuantity
                    )
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    edtChangeQuantity.setText(beforeTextChanged)
                    edtChangeQuantity.addTextChangedListener(tw)
                    edtChangeQuantity.setSelection(start)
                }
            }

            override fun afterTextChanged(s: Editable) {}
        }
        edtChangeQuantity.addTextChangedListener(tw)
        btnDone.setOnClickListener {
            if (!StringHelper.isStringNullOrEmpty(edtChangeQuantity.text.toString())) {
                try {
                    val quantity =
                        edtChangeQuantity.text.toString().replace(",".toRegex(), ".").toDouble()
                    product!!.quantity = (quantity)
                    product = calculateTotalPriceChanged(product)
                    val imm =
                        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(edtChangeQuantity.windowToken, 0)
                    customChangeQuantityDialog.dismiss()

                    //notify the adapter of change
                    productDataChangeListener.onDataChange(product)
                } catch (e: NumberFormatException) {
                    getToastRed(context.getString(R.string.txt_enter_valid_quantity))
                }
            } else {
                getToastRed(context.getString(R.string.txt_enter_quantity))
            }
        }
        btnIncreaseQuantity.setOnClickListener {
            val quantity = product!!.quantity + 1
            product!!.quantity = (quantity)
            if (isDecimalAllowed(product!!.unitSize)) {
                edtChangeQuantity.setText(product!!.quantity.toString())
            } else {
                edtChangeQuantity.setText(product!!.quantity.toInt().toString())
            }
            setTheMessagesForChangeQuantityDialog(
                txtErrorMessage,
                edtChangeQuantity,
                lytReduceQuantity
            )
        }
        imgDecQuantity.setOnClickListener {
            var quantity = product!!.quantity - 1
            if (quantity < 0) {
                quantity = 0.0
            }
            product!!.quantity = (quantity)
            if (isDecimalAllowed(product!!.unitSize)) {
                edtChangeQuantity.setText(product!!.quantity.toString().toString())
            } else {
                edtChangeQuantity.setText(product!!.quantity.toInt())
            }
            setTheMessagesForChangeQuantityDialog(
                txtErrorMessage,
                edtChangeQuantity,
                lytReduceQuantity
            )
        }
        setTheMessagesForChangeQuantityDialog(txtErrorMessage, edtChangeQuantity, lytReduceQuantity)
        customChangeQuantityDialog.show()
        return customChangeQuantityDialog
    }

    /**
     * sets the appropriate messages for the change quantity dialog
     *
     * @param txtErrorMessage
     * @param edtQuantity
     * @param lytReduceQuantity
     */
    private fun setTheMessagesForChangeQuantityDialog(
        txtErrorMessage: TextView,
        edtQuantity: EditText,
        lytReduceQuantity: LinearLayout
    ) {
        if (product!!.quantity == 0.0) {
            //display the message that the quantity selected cannot be 0
            val message = context.getString(R.string.txt_bold_quantity_cannot_be_0)
            lytReduceQuantity.visibility = View.INVISIBLE
            txtErrorMessage.visibility = View.VISIBLE
            txtErrorMessage.text = Html.fromHtml(message)
            edtQuantity.setBackgroundResource(R.drawable.pinky_rect_border)
            edtQuantity.setTextColor(context.resources.getColor(R.color.pinky_red))
        } else if (product!!.quantity < product!!.moq!!) {
            val moqVal = product!!.moqDisplayValue
            val txtStockMessageText = String.format(
                context.getString(R.string.format_moq_is), moqVal, returnShortNameValueUnitSize(
                    product!!.unitSize
                )
            )
            lytReduceQuantity.visibility = View.VISIBLE
            txtErrorMessage.visibility = View.VISIBLE
            txtErrorMessage.text = Html.fromHtml(txtStockMessageText)
            edtQuantity.setBackgroundResource(R.drawable.pinky_rect_border)
            edtQuantity.setTextColor(context.resources.getColor(R.color.pinky_red))
        } else {
            lytReduceQuantity.visibility = View.VISIBLE
            txtErrorMessage.visibility = View.GONE
            edtQuantity.setBackgroundResource(R.color.faint_grey)
            edtQuantity.setTextColor(context.resources.getColor(R.color.black))
        }
    }
}