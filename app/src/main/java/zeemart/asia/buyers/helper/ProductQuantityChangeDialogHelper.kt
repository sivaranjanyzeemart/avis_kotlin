package zeemart.asia.buyers.helper

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Editable
import android.text.Html
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.adapter.ProductUnitSizeAdapter
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.helperimportimport.CustomChangeQuantityDialog
import zeemart.asia.buyers.models.ProductPriceList
import zeemart.asia.buyers.models.UnitSizeModel
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel
import zeemart.asia.buyers.models.marketlist.Product
import zeemart.asia.buyers.models.marketlist.ProductDetailBySupplier
import zeemart.asia.buyers.models.reportsimport.Products
import java.text.DecimalFormatSymbols

/**
 * Created by ParulBhandari on 8/13/2018.
 */
class ProductQuantityChangeDialogHelper constructor(
    private var product: Product?,
    private val context: Context,
    supplierDetails: DetailSupplierDataModel,
    productDetailData: ProductDetailBySupplier,
    productDataChangeListener: ProductDataChangeListener
) {
    private val supplierDetail: DetailSupplierDataModel
    private val productDetailData: ProductDetailBySupplier?
    private val products: Products? = null
    private val productDataChangeListener: ProductDataChangeListener
    private val productDetailBySuppliers: List<ProductDetailBySupplier>? = null
    var tw: TextWatcher? = null
    var beforeTextChanged: String = ""
    private var priceListwithStocksMap: Map<String?, ProductDataHelper.PriceListwithStock>? = null

    open interface ProductDataChangeListener {
        fun onDataChange(product: Product?)
    }

    init {
        supplierDetail = supplierDetails
        this.productDetailData = productDetailData
        this.productDataChangeListener = productDataChangeListener
        if (supplierDetails.supplier == null) {
            supplierDetails.supplier = productDetailData.supplier!!
        }
    }

    /**
     * Create change product quantity dialog
     */
    fun createChangeQuantityDialog(): CustomChangeQuantityDialog {
        val priceList: MutableList<ProductPriceList> = ArrayList<ProductPriceList>()
        val customChangeQuantityDialog: CustomChangeQuantityDialog =
            object : CustomChangeQuantityDialog(
                context, R.style.CustomDialogTheme
            ) {
                public override fun onUnitSizeSelectedCalled(d: CustomChangeQuantityDialog) {
                    val edtQuantity: EditText = d.findViewById<EditText>(R.id.edtChangeQuantity)
                    val txtErrorMessage: TextView = d.findViewById<TextView>(R.id.txt_error_message)
                    val lytReduceQuantity: LinearLayout =
                        d.findViewById<LinearLayout>(R.id.lyt_decrease_quantity)
                    for (i in priceList.indices) {
                        if (priceList.get(i).selected) {
                            product!!.moq = (priceList.get(i).moq)
                            product!!.unitSize = priceList.get(i).unitSize!!
                            product!!.unitPrice = (priceList.get(i).price)
                            product!!.quantity = (priceList.get(i).moq!!)
                            break
                        }
                    }
                    if (UnitSizeModel.isDecimalAllowed(product!!.unitSize)) {
                        edtQuantity.setText(product!!.moqEditValue)
                        edtQuantity.setSelection(edtQuantity.getText().length)
                    } else {
                        edtQuantity.setText(product!!.moqEditValue)
                        edtQuantity.setSelection(edtQuantity.getText().length)
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
        val lstOrderItems: RecyclerView =
            customChangeQuantityDialog.findViewById<RecyclerView>(R.id.lst_unit_size)
        val imgDecQuantity: ImageView = customChangeQuantityDialog.findViewById(R.id.img_dec_quant)
        //final TextView txtDDecQuantityMoq = customChangeQuantityDialog.findViewById(R.id.txt_dec_quant_moq);
        val btnIncreaseQuantity: ImageButton = customChangeQuantityDialog.findViewById<ImageButton>(
            R.id.btn_increase_quantity
        )
        val edtChangeQuantity: EditText =
            customChangeQuantityDialog.findViewById<EditText>(R.id.edtChangeQuantity)
        val txtErrorMessage: TextView =
            customChangeQuantityDialog.findViewById<TextView>(R.id.txt_error_message)
        val txtOnHandInfo: TextView =
            customChangeQuantityDialog.findViewById<TextView>(R.id.txt_on_hand_info)
        val txtOnHandValue: TextView =
            customChangeQuantityDialog.findViewById<TextView>(R.id.txt_on_hand_value)
        val txtParInfo: TextView =
            customChangeQuantityDialog.findViewById<TextView>(R.id.txt_par_info)
        val txtParValue: TextView =
            customChangeQuantityDialog.findViewById<TextView>(R.id.txt_par_value)
        val txtIncomingInfo: TextView =
            customChangeQuantityDialog.findViewById<TextView>(R.id.txt_incoming)
        val txtIncomingValue: TextView =
            customChangeQuantityDialog.findViewById<TextView>(R.id.txt_incoming_value)
        val lytReduceQuantity: LinearLayout = customChangeQuantityDialog.findViewById<LinearLayout>(
            R.id.lyt_decrease_quantity
        )
        val btnDone: Button = customChangeQuantityDialog.findViewById(R.id.btnchange_quantity_done)
        val btnFillToPar: Button = customChangeQuantityDialog.findViewById(R.id.btn_fill_to_par)
        val txtChangeQuantityHeader: TextView = customChangeQuantityDialog.findViewById<TextView>(
            R.id.txt_change_quantity_header
        )
        //set the font styling for change quantity dialog
        ZeemartBuyerApp.setTypefaceView(
            btnDone,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            btnFillToPar,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            edtChangeQuantity,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtErrorMessage,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtOnHandInfo,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtParInfo,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtOnHandValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtParValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtIncomingValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtIncomingInfo,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtChangeQuantityHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        if (product != null) {
            if (UnitSizeModel.isDecimalAllowed(product!!.unitSize)) {
                edtChangeQuantity.setText(product!!.quantityEditValue)
                edtChangeQuantity.setSelection(edtChangeQuantity.getText().length)
            } else {
                edtChangeQuantity.setText(product!!.quantityEditValue)
                edtChangeQuantity.setSelection(edtChangeQuantity.getText().length)
            }
        }
        if (productDetailData != null) {
            for (i in productDetailData.priceList?.indices!!) {
                if ((productDetailData.priceList!!.get(i).status == ZeemartAppConstants.VISIBLE) && UnitSizeModel.getunitSizeMap()
                        ?.containsKey(productDetailData.priceList!!.get(i).unitSize)!!
                ) {
                    priceList.add(productDetailData.priceList!!.get(i))
                }
            }
            if (priceList.get(0).parLevel != null && priceList.get(0).parLevel != 0.0) {
                var onHand: Double = 0.0
                if (priceList.get(0).onHandQty != null) {
                    onHand = priceList.get(0).onHandQty!!
                }
                var value: Double = priceList.get(0).parLevel!! - onHand
                value = Math.ceil(value)
                if (value > priceList.get(0).moq!!) {
                    btnFillToPar.setVisibility(View.VISIBLE)
                } else {
                    value = priceList.get(0).moq!!
                    btnFillToPar.setVisibility(View.GONE)
                }
                //                txt_fill_to_par_count
                btnFillToPar.setText(
                    String.format(
                        context.getString(R.string.txt_fill_to_par_count),
                        UnitSizeModel.getValueDecimalAllowedCheck(priceList.get(0).unitSize!!, value)
                    )
                )
                //                btnFillToPar.setText("Fill to par (" +  UnitSizeModel.getValueDecimalAllowedCheck(priceList.get(0).getUnitSize(), value) + ")");
            } else {
                btnFillToPar.setVisibility(View.GONE)
            }
            if (priceList.get(0).onHandQty != null) {
                txtOnHandInfo.setText(context.getString(R.string.txt_on_hand))
                txtOnHandValue.setText(
                    UnitSizeModel.getValueDecimal(priceList.get(0).onHandQty) + " " + UnitSizeModel.returnShortNameValueUnitSize(
                        priceList.get(0).unitSize
                    )
                )
                if (priceList.get(0).parLevel != null && priceList.get(0).parLevel != 0.0) {
                    txtParInfo.setText(" • " + context.getString(R.string.txt_par_info))
                    txtParValue.setText(
                        UnitSizeModel.getValueDecimal(priceList.get(0).parLevel) + " " + UnitSizeModel.returnShortNameValueUnitSize(
                            priceList.get(0).unitSize
                        )
                    )
                } else {
                    txtParInfo.setVisibility(View.GONE)
                    txtParValue.setVisibility(View.GONE)
                }
            } else {
                txtOnHandInfo.setVisibility(View.GONE)
                txtOnHandValue.setVisibility(View.GONE)
                txtParInfo.setVisibility(View.GONE)
                txtParValue.setVisibility(View.GONE)
            }
            if (priceList.get(0).incomings != null && priceList.get(0).incomings!!.size > 0) {
                txtIncomingInfo.setVisibility(View.VISIBLE)
                val incoming: String = UnitSizeModel.getValueDecimalAllowedCheck(
                    priceList.get(0).unitSize!!,
                    priceList.get(0).incomings!!.get(0).incomingQty
                ) + " " + UnitSizeModel.returnShortNameValueUnitSize(priceList.get(0).unitSize)
                val incomingInfo: String =
                    context.getString(R.string.txt_already_ordered) + DateHelper.getDateInDateMonthFormat(
                        priceList.get(0).incomings!!.get(0).timeDelivery
                    )
                val info: String = incoming + incomingInfo
                //                (Html.fromHtml("</b>" + incoming + "</b>" + incomingInfo));
                val spanString: SpannableString = SpannableString(info)
                spanString.setSpan(StyleSpan(Typeface.BOLD), 0, incoming.length, 0)
                spanString.setSpan(ForegroundColorSpan(Color.BLACK), 0, incoming.length, 0)
                txtIncomingInfo.setText(spanString)
            } else {
                txtIncomingInfo.setVisibility(View.GONE)
                txtIncomingValue.setVisibility(View.GONE)
            }
        } else {
            for (i in 0 until products?.priceList?.size!!) {
                if (products.priceList!!.get(i).status
                        .equals(ZeemartAppConstants.VISIBLE) && UnitSizeModel.getunitSizeMap()
                        ?.containsKey(products.priceList!!.get(i).unitSize!!) == true
                ) {
                    priceList.add(products.priceList!!.get(i))
                }
            }
        }
        if (product != null && priceList.size > 0) {
            for (j in priceList.indices) {
                if ((product!!.unitSize == priceList.get(j).unitSize)) {
                    priceList.get(j).selected = true
                } else {
                    priceList.get(j).selected = false
                }
            }
        }
        val layoutManager: LinearLayoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        lstOrderItems.setLayoutManager(layoutManager)
        lstOrderItems.setAdapter(
            ProductUnitSizeAdapter(
                context,
                priceList,
                customChangeQuantityDialog
            )
        )
        tw = object : TextWatcher {
            public override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
                beforeTextChanged = s.toString()
                Log.e("TextWatcher", "beforeTextChanged" + s)
            }

            public override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                Log.e("TextWatcher", "onTextChanged" + s)
                edtChangeQuantity.removeTextChangedListener(tw)
                try {
                    val symbols: DecimalFormatSymbols =
                        DecimalFormatSymbols(ZeemartAppConstants.Market.`this`.locale)
                    val sep: Char = symbols.getDecimalSeparator()
                    if (s.toString().length == 0) {
                        product!!.quantity = (0.0)
                    } else if (UnitSizeModel.isDecimalAllowed(product!!.unitSize)) {
                        val `val`: Double = s.toString().replace(",".toRegex(), ".").toDouble()
                        val splitter: Array<String> =
                            `val`.toString().split("\\.".toRegex()).dropLastWhile({ it.isEmpty() })
                                .toTypedArray()
                        if (`val` < 0 || (splitter.size == 2 && splitter.get(1).length > 3)) {
                            throw NumberFormatException()
                        }
                        product!!.quantity = (`val`)
                    } else {
                        if (s.toString().replace(",".toRegex(), ".").contains(",")) {
                            throw NumberFormatException()
                        }
                        val `val`: Int = s.toString().replace(",".toRegex(), ".").toInt()
                        if (`val` < 0) {
                            throw NumberFormatException()
                        }
                        product!!.quantity = (`val`.toDouble())
                    }
                    edtChangeQuantity.setText(
                        edtChangeQuantity.getText().toString().replace(".", "" + sep)
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

            public override fun afterTextChanged(s: Editable) {}
        }
        edtChangeQuantity.addTextChangedListener(tw)
        btnFillToPar.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                if (!StringHelper.isStringNullOrEmpty(edtChangeQuantity.getText().toString())) {
                    for (i in priceList.indices) {
                        if (priceList.get(i).selected) {
                            product!!.moq = (priceList.get(i).moq)
                            product!!.unitSize = priceList.get(i).unitSize!!
                            product!!.unitPrice = (priceList.get(i).price)
                        }
                    }
                    try {
                        val quantity: Double =
                            edtChangeQuantity.getText().toString().replace(",".toRegex(), ".")
                                .toDouble()
                        val onHand: Double = priceList.get(0).onHandQty!!
                        var value: Double = priceList.get(0).parLevel?.minus(onHand)!!
                        value = Math.ceil(value)
                        if (value > priceList.get(0).moq!!) {
                            product!!.quantity = (value)
                        } else {
                            product!!.quantity = (priceList.get(0).moq!!)
                        }
                        product = ProductDataHelper.calculateTotalPriceChanged(product)
                        val imm: InputMethodManager =
                            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(edtChangeQuantity.getWindowToken(), 0)
                        customChangeQuantityDialog.dismiss()

                        //notify the adapter of change
                        productDataChangeListener.onDataChange(product)
                    } catch (e: NumberFormatException) {
                        ZeemartBuyerApp.getToastRed(context.getString(R.string.txt_enter_valid_quantity))
                    }
                } else {
                    ZeemartBuyerApp.getToastRed(context.getString(R.string.txt_enter_quantity))
                }
            }
        })
        btnDone.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                if (!StringHelper.isStringNullOrEmpty(edtChangeQuantity.getText().toString())) {
                    for (i in priceList.indices) {
                        if (priceList.get(i).selected) {
                            product!!.moq = (priceList.get(i).moq)
                            product!!.unitSize = priceList.get(i).unitSize!!
                            product!!.unitPrice = (priceList.get(i).price)
                        }
                    }
                    try {
                        val quantity: Double =
                            edtChangeQuantity.getText().toString().replace(",".toRegex(), ".")
                                .toDouble()
                        product!!.quantity = (quantity)
                        product = ProductDataHelper.calculateTotalPriceChanged(product)
                        val imm: InputMethodManager =
                            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(edtChangeQuantity.getWindowToken(), 0)
                        customChangeQuantityDialog.dismiss()

                        //notify the adapter of change
                        productDataChangeListener.onDataChange(product)
                    } catch (e: NumberFormatException) {
                        ZeemartBuyerApp.getToastRed(context.getString(R.string.txt_enter_valid_quantity))
                    }
                } else {
                    ZeemartBuyerApp.getToastRed(context.getString(R.string.txt_enter_quantity))
                }
            }
        })
        btnIncreaseQuantity.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                val quantity: Double = product!!.quantity + 1
                product!!.quantity = (quantity)
                if (UnitSizeModel.isDecimalAllowed(product!!.unitSize)) {
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
        })
        imgDecQuantity.setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                var quantity: Double = product!!.quantity - 1
                if (quantity < 0) {
                    quantity = 0.0
                }
                product!!.quantity = (quantity)
                if (UnitSizeModel.isDecimalAllowed(product!!.unitSize)) {
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
        })
        edtChangeQuantity.requestFocus()
        customChangeQuantityDialog.getWindow()!!
            .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
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
        if (productDetailData != null && (((supplierDetail.supplier != null) && (
                    supplierDetail.supplier.settings != null) && (
                    supplierDetail.supplier.settings!!.inventory != null) && (supplierDetail.supplier.settings!!.inventory?.status != null) && (supplierDetail.supplier.settings!!.inventory?.status == ServiceConstant.ACTIVE) && !supplierDetail.supplier.settings!!.inventory?.allowNegative!!))
        ) {
            priceListwithStocksMap = ProductDataHelper.getStocksAvailableUOM(productDetailData)
            if (priceListwithStocksMap!!.size == 0) {
                val message: String = context.getString(R.string.txt_bold_item_unavailable)
                if (product!!.quantity == 0.0) {
                    lytReduceQuantity.setVisibility(View.INVISIBLE)
                } else {
                    lytReduceQuantity.setVisibility(View.VISIBLE)
                }
                txtErrorMessage.setVisibility(View.VISIBLE)
                txtErrorMessage.setText(Html.fromHtml(message))
                edtQuantity.setBackgroundResource(R.drawable.pinky_rect_border)
                edtQuantity.setTextColor(context.getResources().getColor(R.color.pinky_red))
            } else {
                if (priceListwithStocksMap!!.containsKey(product!!.unitSize)) {
                    if (product!!.quantity == 0.0) {
                        //display the message that the quantity selected cannot be 0
                        val message: String =
                            context.getString(R.string.txt_bold_quantity_cannot_be_0)
                        val quantity: String = product!!.quantityDisplayValue
                        lytReduceQuantity.setVisibility(View.INVISIBLE)
                        txtErrorMessage.setVisibility(View.VISIBLE)
                        txtErrorMessage.setText(Html.fromHtml(message))
                        edtQuantity.setBackgroundResource(R.drawable.pinky_rect_border)
                        edtQuantity.setTextColor(context.getResources().getColor(R.color.pinky_red))
                    } else if (product!!.quantity < product!!.moq!!) {
                        val moqVal: String = product!!.moqDisplayValue
                        val txtStockMessageText: String = String.format(
                            context.getString(R.string.format_moq_is),
                            moqVal,
                            UnitSizeModel.returnShortNameValueUnitSize(
                                product!!.unitSize
                            )
                        )
                        lytReduceQuantity.setVisibility(View.VISIBLE)
                        txtErrorMessage.setVisibility(View.VISIBLE)
                        txtErrorMessage.setText(Html.fromHtml(txtStockMessageText))
                        edtQuantity.setBackgroundResource(R.drawable.pinky_rect_border)
                        edtQuantity.setTextColor(context.getResources().getColor(R.color.pinky_red))
                    } else if (product!!.quantity > priceListwithStocksMap!!.get(product!!.unitSize)!!.stocksAmountAvailable) {
                        //display message not enough stock
                        val stockAvailableAmount: String =
                            UnitSizeModel.getValueDecimalAllowedCheck(
                                product!!.unitSize,
                                priceListwithStocksMap!!.get(product!!.unitSize)!!.stocksAmountAvailable
                            )
                        val txtStockMessageText: String = String.format(
                            context.getString(R.string.format_not_enough_stock),
                            stockAvailableAmount,
                            UnitSizeModel.returnShortNameValueUnitSize(
                                product!!.unitSize
                            )
                        )
                        lytReduceQuantity.setVisibility(View.VISIBLE)
                        txtErrorMessage.setVisibility(View.VISIBLE)
                        txtErrorMessage.setText(Html.fromHtml(txtStockMessageText))
                        edtQuantity.setBackgroundResource(R.drawable.pinky_rect_border)
                        edtQuantity.setTextColor(context.getResources().getColor(R.color.pinky_red))
                    } else {
                        //do nothing
                        lytReduceQuantity.setVisibility(View.VISIBLE)
                        txtErrorMessage.setVisibility(View.VISIBLE)
                        txtErrorMessage.setText("")
                        edtQuantity.setBackgroundResource(R.color.faint_grey)
                        edtQuantity.setTextColor(context.getResources().getColor(R.color.black))
                    }
                } else {
                    //item unavailable in this particular UOM
                    //display message the product in current UOM is unavailable please select another UOM
                    val txtStockMessageText: String = String.format(
                        context.getString(R.string.format_item_unavailable),
                        UnitSizeModel.returnShortNameValueUnitSize(
                            product!!.unitSize
                        )
                    )
                    if (product!!.quantity == 0.0) {
                        lytReduceQuantity.setVisibility(View.INVISIBLE)
                    } else {
                        lytReduceQuantity.setVisibility(View.VISIBLE)
                    }
                    txtErrorMessage.setVisibility(View.VISIBLE)
                    txtErrorMessage.setText(Html.fromHtml(txtStockMessageText))
                    edtQuantity.setBackgroundResource(R.drawable.pinky_rect_border)
                    edtQuantity.setTextColor(context.getResources().getColor(R.color.pinky_red))
                }
            }
        } else {
            if (product!!.quantity == 0.0) {
                //display the message that the quantity selected cannot be 0
                val message: String = context.getString(R.string.txt_bold_quantity_cannot_be_0)
                lytReduceQuantity.setVisibility(View.INVISIBLE)
                txtErrorMessage.setVisibility(View.VISIBLE)
                txtErrorMessage.setText(Html.fromHtml(message))
                edtQuantity.setBackgroundResource(R.drawable.pinky_rect_border)
                edtQuantity.setTextColor(context.getResources().getColor(R.color.pinky_red))
            } else if (product!!.quantity < product!!.moq!!) {
                val moqVal: String = product!!.moqDisplayValue
                val txtStockMessageText: String = String.format(
                    context.getString(R.string.format_moq_is),
                    moqVal,
                    UnitSizeModel.returnShortNameValueUnitSize(
                        product!!.unitSize
                    )
                )
                lytReduceQuantity.setVisibility(View.VISIBLE)
                txtErrorMessage.setVisibility(View.VISIBLE)
                txtErrorMessage.setText(Html.fromHtml(txtStockMessageText))
                edtQuantity.setBackgroundResource(R.drawable.pinky_rect_border)
                edtQuantity.setTextColor(context.getResources().getColor(R.color.pinky_red))
            } else {
                lytReduceQuantity.setVisibility(View.VISIBLE)
                txtErrorMessage.setVisibility(View.VISIBLE)
                txtErrorMessage.setText("")
                edtQuantity.setBackgroundResource(R.color.faint_grey)
                edtQuantity.setTextColor(context.getResources().getColor(R.color.black))
            }
        }
    }
}