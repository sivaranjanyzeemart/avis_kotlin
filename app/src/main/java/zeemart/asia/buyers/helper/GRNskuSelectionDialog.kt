package zeemart.asia.buyers.helper

import android.content.Context
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastRed
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.DialogHelper.ShowUomFilterDialog
import zeemart.asia.buyers.helper.DialogHelper.onUomSelectListener
import zeemart.asia.buyers.helper.ProductDataHelper.calculateTotalPriceChanged
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.helperimportimport.CustomChangeQuantityDialog
import zeemart.asia.buyers.models.UnitSizeModel
import zeemart.asia.buyers.models.UnitSizeModel.Companion.returnShortNameValueUnitSizeForQuantity
import zeemart.asia.buyers.models.marketlist.Product
import java.text.DecimalFormatSymbols

/**
 * Created by RajPrudhviMarella on 14/Jun/2021.
 */
class GRNskuSelectionDialog(
    private val context: Context,
    private var product: Product?,
    private val productDataChangeListener: GRNskuSelectionDialogChangeListener
) {
    var tw: TextWatcher? = null
    var beforeTextChanged = ""
    var isChanged = false

    interface GRNskuSelectionDialogChangeListener {
        fun onDataChange(product: Product?, isStatusChanged: Boolean)
        fun requestForRemove(product: Product?)
    }

    /**
     * Create change product quantity dialog
     */
    fun createChangeQuantityDialog(isFromDashBoard: Boolean): CustomChangeQuantityDialog {
        val customChangeQuantityDialog: CustomChangeQuantityDialog =
            object : CustomChangeQuantityDialog(
                context, R.style.CustomDialogForTagsTheme
            ) {
                override fun onUnitSizeSelectedCalled(d: CustomChangeQuantityDialog) {
                    val edtQuantity = d.findViewById<EditText>(R.id.edtChangeQuantity)
                    val txtErrorMessage = d.findViewById<TextView>(R.id.txt_error_message)
                    val btnDone = d.findViewById<Button>(R.id.btnchange_quantity_done)
                    val lytReduceQuantity = d.findViewById<LinearLayout>(R.id.lyt_decrease_quantity)
                    edtQuantity.setText(product!!.quantity.toString() + "")
                    edtQuantity.setSelection(edtQuantity.text.length)
                    //display the particular message
                    setTheMessagesForChangeQuantityDialog(
                        txtErrorMessage,
                        edtQuantity,
                        lytReduceQuantity,
                        btnDone
                    )
                }
            }
        customChangeQuantityDialog.setContentView(R.layout.layout_grn_changes_quantity)
        customChangeQuantityDialog.window!!.setGravity(Gravity.BOTTOM)
        customChangeQuantityDialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        customChangeQuantityDialog.window!!
            .setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        customChangeQuantityDialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        val txtUOM = customChangeQuantityDialog.findViewById<TextView>(R.id.txt_uom_name)
        val txtSKUname = customChangeQuantityDialog.findViewById<TextView>(R.id.txt_sku_name_header)
        val txtCustomSKUname =
            customChangeQuantityDialog.findViewById<TextView>(R.id.txt_sku_name_header_custom)
        val img_arrow = customChangeQuantityDialog.findViewById<ImageView>(R.id.img_arrow)
        val lytUomOption =
            customChangeQuantityDialog.findViewById<LinearLayout>(R.id.lyt_uom_options)
        val imgDecQuantity = customChangeQuantityDialog.findViewById<ImageView>(R.id.img_dec_quant)
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
        val btnRemove = customChangeQuantityDialog.findViewById<TextView>(R.id.txt_remove)
        if (!isFromDashBoard) {
            btnRemove.visibility = View.GONE
            btnRemove.isClickable = false
        } else {
            btnRemove.visibility = View.VISIBLE
            btnRemove.isClickable = true
        }
        val dismissBG = customChangeQuantityDialog.findViewById<RelativeLayout>(R.id.dismiss_bg)
        lytUomOption.setOnClickListener(View.OnClickListener {
            if (product!!.unitSizes != null && product!!.unitSizes!!.size > 0) {
                val unitSizeModels: MutableList<UnitSizeModel?> = ArrayList()
                for (i in product!!.unitSizes!!.indices) {
                    val unitSizeModel = UnitSizeModel()
                    unitSizeModel.unitSize = product!!.unitSizes!![i].unitSize
                    if ((product!!.unitSizes!![i].unitSize == product!!.unitSize)) {
                        unitSizeModel.isSelected = true
                    } else {
                        unitSizeModel.isSelected = false
                    }
                    unitSizeModels.add(unitSizeModel)
                }
                ShowUomFilterDialog(context, unitSizeModels as List<UnitSizeModel>, object : onUomSelectListener {
                    override fun onUomSelect(unitSize: String?) {
                        if (!StringHelper.isStringNullOrEmpty(unitSize)) {
                            product!!.unitSize = (unitSize)!!
                            product = calculateTotalPriceChanged(product)
                            txtUOM.text = returnShortNameValueUnitSizeForQuantity(
                                product!!.unitSize
                            )
                        }
                    }
                })
            }
        })
        if (product!!.unitSizes != null && product!!.unitSizes!!.size > 0) {
            if (product!!.unitSizes!!.size > 1) {
                lytUomOption.isClickable = true
                img_arrow.visibility = View.VISIBLE
                txtUOM.setTextColor(context.resources.getColor(R.color.color_azul_two))
            } else {
                lytUomOption.isClickable = false
                img_arrow.visibility = View.GONE
                txtUOM.setTextColor(context.resources.getColor(R.color.dark_grey))
            }
        } else {
            lytUomOption.isClickable = false
            img_arrow.visibility = View.GONE
            txtUOM.setTextColor(context.resources.getColor(R.color.dark_grey))
        }
        if (SharedPref.readBool(
                SharedPref.USER_INVENTORY_SETTING_STATUS,
                false
            )!! && product!!.customName != null
        ) {
            txtSKUname.text = product!!.customName
            txtCustomSKUname.text = product!!.productName
        } else {
            txtSKUname.text = product!!.productName
            txtCustomSKUname.visibility = View.GONE
        }
        txtUOM.text = returnShortNameValueUnitSizeForQuantity(product!!.unitSize)

        //set the font styling for change quantity dialog
        setTypefaceView(btnDone, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(edtChangeQuantity, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtErrorMessage, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(
            txtChangeQuantityHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtSKUname, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtCustomSKUname, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_ITALICS)
        setTypefaceView(btnRemove, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtUOM, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(btnRemove, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        val quantitiy = product!!.quantity
        if (product != null) {
            edtChangeQuantity.setText(product!!.quantity.toString() + "")
            edtChangeQuantity.setSelection(edtChangeQuantity.text.length)
        }
        dismissBG.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                product!!.quantity = (quantitiy)
                val imm =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(edtChangeQuantity.windowToken, 0)
                customChangeQuantityDialog.dismiss()
            }
        })
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
                    } else {
                        val `val` = s.toString().replace(",".toRegex(), ".").toDouble()
                        val splitter =
                            `val`.toString().split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                        if (`val` < 0 || (splitter.size == 2 && splitter[1].length > 3)) {
                            throw NumberFormatException()
                        }
                        product!!.quantity = (`val`)
                    }
                    edtChangeQuantity.setText(
                        edtChangeQuantity.text.toString().replace(".", "" + sep)
                    )
                    edtChangeQuantity.addTextChangedListener(tw)
                    edtChangeQuantity.setSelection(start + count)
                    setTheMessagesForChangeQuantityDialog(
                        txtErrorMessage,
                        edtChangeQuantity,
                        lytReduceQuantity,
                        btnDone
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
        btnRemove.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                product = calculateTotalPriceChanged(product)
                val imm =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(edtChangeQuantity.windowToken, 0)
                customChangeQuantityDialog.dismiss()
                productDataChangeListener.requestForRemove(product)
            }
        })
        btnDone.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (!StringHelper.isStringNullOrEmpty(edtChangeQuantity.text.toString())) {
                    try {
                        val quantity =
                            edtChangeQuantity.text.toString().replace(",".toRegex(), ".").toDouble()
                        product!!.quantity = (quantity)
                        if (!product!!.isSelected) {
                            product!!.isSelected = true
                            isChanged = true
                        }
                        product!!.isCustom = false
                        product = calculateTotalPriceChanged(product)
                        val imm =
                            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(edtChangeQuantity.windowToken, 0)
                        customChangeQuantityDialog.dismiss()
                        //notify the adapter of change
                        productDataChangeListener.onDataChange(product, isChanged)
                    } catch (e: NumberFormatException) {
                        getToastRed(context.getString(R.string.txt_enter_valid_quantity))
                    }
                } else {
                    getToastRed(context.getString(R.string.txt_enter_quantity))
                }
            }
        })
        btnIncreaseQuantity.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val quantity = product!!.quantity + 1
                product!!.quantity = (quantity)
                edtChangeQuantity.setText(product!!.quantity.toString() + "")
                setTheMessagesForChangeQuantityDialog(
                    txtErrorMessage,
                    edtChangeQuantity,
                    lytReduceQuantity,
                    btnDone
                )
            }
        })
        imgDecQuantity.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                var quantity = product!!.quantity - 1
                if (quantity < 0) {
                    quantity = 0.0
                }
                product!!.quantity = (quantity)
                edtChangeQuantity.setText(product!!.quantity.toString() + "")
                setTheMessagesForChangeQuantityDialog(
                    txtErrorMessage,
                    edtChangeQuantity,
                    lytReduceQuantity,
                    btnDone
                )
            }
        })
        setTheMessagesForChangeQuantityDialog(
            txtErrorMessage,
            edtChangeQuantity,
            lytReduceQuantity,
            btnDone
        )
        customChangeQuantityDialog.show()
        return customChangeQuantityDialog
    }

    /**
     * Create change product quantity dialog
     */
    fun createCustomeSKUChangeQuantityDialog(isFromDashBoard: Boolean): CustomChangeQuantityDialog {
        val customChangeQuantityDialog: CustomChangeQuantityDialog =
            object : CustomChangeQuantityDialog(
                context, R.style.CustomDialogForTagsTheme
            ) {
                override fun onUnitSizeSelectedCalled(d: CustomChangeQuantityDialog) {
                    val edtQuantity = d.findViewById<EditText>(R.id.edtChangeQuantity)
                    val txtErrorMessage = d.findViewById<TextView>(R.id.txt_error_message)
                    val btnDone = d.findViewById<Button>(R.id.btnchange_quantity_done)
                    val lytReduceQuantity = d.findViewById<LinearLayout>(R.id.lyt_decrease_quantity)
                    edtQuantity.setText(product!!.quantity.toString() + "")
                    edtQuantity.setSelection(edtQuantity.text.length)
                    //display the particular message
                    setTheMessagesForChangeQuantityDialog(
                        txtErrorMessage,
                        edtQuantity,
                        lytReduceQuantity,
                        btnDone
                    )
                }
            }
        val unitSizeModels = ZeemartBuyerApp.gsonExposeExclusive.fromJson<List<UnitSizeModel?>>(
            SharedPref.read(
                SharedPref.UNIT_SIZE_LIST,
                ""
            ), object : TypeToken<List<UnitSizeModel?>?>() {}.type
        )
        if (StringHelper.isStringNullOrEmpty(product!!.unitSize)) {
            product!!.unitSize = "Unit"
            product!!.quantity = (1.0)
        }
        customChangeQuantityDialog.setContentView(R.layout.layout_grn_custom_sku_changes_quantity)
        customChangeQuantityDialog.window!!.setGravity(Gravity.BOTTOM)
        customChangeQuantityDialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        customChangeQuantityDialog.window!!
            .setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        customChangeQuantityDialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        val txtUOM = customChangeQuantityDialog.findViewById<TextView>(R.id.txt_uom_name)
        val txtSKUname = customChangeQuantityDialog.findViewById<TextView>(R.id.txt_sku_name_header)
        val lytUomOption =
            customChangeQuantityDialog.findViewById<LinearLayout>(R.id.lyt_uom_options)
        val imgDecQuantity = customChangeQuantityDialog.findViewById<ImageView>(R.id.img_dec_quant)
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
        val btnRemove = customChangeQuantityDialog.findViewById<TextView>(R.id.txt_remove)
        if (!isFromDashBoard) {
            btnRemove.visibility = View.GONE
            btnRemove.isClickable = false
        } else {
            btnRemove.visibility = View.VISIBLE
            btnRemove.isClickable = true
        }
        val dismissBG = customChangeQuantityDialog.findViewById<RelativeLayout>(R.id.dismiss_bg)
        val txtSkuNameHeader = customChangeQuantityDialog.findViewById<TextView>(R.id.txt_sku_name)
        val txtCharactersRemaining =
            customChangeQuantityDialog.findViewById<TextView>(R.id.txt_characters_remaining)
        val edtSKUName = customChangeQuantityDialog.findViewById<EditText>(R.id.edt_sku_name)
        val txtQuantityHeader =
            customChangeQuantityDialog.findViewById<TextView>(R.id.txt_quanity_name)
        val quantitiy = product!!.quantity
        if (product != null) {
            edtChangeQuantity.setText(product!!.quantity.toString() + "")
            edtChangeQuantity.setSelection(edtChangeQuantity.text.length)
            if (!StringHelper.isStringNullOrEmpty(product!!.productName)) {
                edtSKUName.setText(product!!.productName)
                txtCharactersRemaining.text = String.format(
                    context.getString(R.string.txt_characters_remaining),
                    100 - product!!.productName!!.length
                )
            } else {
                txtCharactersRemaining.text =
                    String.format(context.getString(R.string.txt_characters_remaining), 100)
            }
            if (!StringHelper.isStringNullOrEmpty(product!!.unitSize)) txtUOM.text =
                returnShortNameValueUnitSizeForQuantity(
                    product!!.unitSize
                )
        }
        lytUomOption.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                val imm =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(edtSKUName.windowToken, 0)
                imm.hideSoftInputFromWindow(edtChangeQuantity.windowToken, 0)
                for (i in unitSizeModels.indices) {
                    if (unitSizeModels[i]!!.unitSize.equals(
                            product!!.unitSize,
                            ignoreCase = true
                        )
                    ) {
                        unitSizeModels[i]!!.isSelected = true
                        break
                    }
                }
                ShowUomFilterDialog(context, unitSizeModels as List<UnitSizeModel>, object : onUomSelectListener {
                    override fun onUomSelect(unitSize: String?) {
                        if (!StringHelper.isStringNullOrEmpty(unitSize)) {
                            product!!.unitSize = (unitSize)!!
                            product = calculateTotalPriceChanged(product)
                            txtUOM.text = returnShortNameValueUnitSizeForQuantity(
                                product!!.unitSize
                            )
                        }
                        customChangeQuantityDialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
                    }
                })
            }
        })

        //set the font styling for change quantity dialog
        setTypefaceView(btnDone, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(edtChangeQuantity, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtErrorMessage, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(
            txtChangeQuantityHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtSKUname, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(btnRemove, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtUOM, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(btnRemove, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(edtSKUName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(
            txtCharactersRemaining,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(txtSkuNameHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtSkuNameHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtQuantityHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        dismissBG.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                product!!.quantity = (quantitiy)
                val imm =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(edtChangeQuantity.windowToken, 0)
                imm.hideSoftInputFromWindow(edtSKUName.windowToken, 0)
                customChangeQuantityDialog.dismiss()
            }
        })
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
                    } else {
                        val `val` = s.toString().replace(",".toRegex(), ".").toDouble()
                        val splitter =
                            `val`.toString().split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                        if (`val` < 0 || (splitter.size == 2 && splitter[1].length > 3)) {
                            throw NumberFormatException()
                        }
                        product!!.quantity = (`val`)
                    }
                    edtChangeQuantity.setText(
                        edtChangeQuantity.text.toString().replace(".", "" + sep)
                    )
                    edtChangeQuantity.addTextChangedListener(tw)
                    edtChangeQuantity.setSelection(start + count)
                    setTheMessagesForChangeQuantityDialog(
                        txtErrorMessage,
                        edtChangeQuantity,
                        lytReduceQuantity,
                        btnDone
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
        edtSKUName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (!StringHelper.isStringNullOrEmpty(edtSKUName.text.toString()) && edtSKUName.text.length > 0) {
                    txtCharactersRemaining.text = String.format(
                        context.getString(R.string.txt_characters_remaining),
                        100 - edtSKUName.text.length
                    )
                } else {
                    txtCharactersRemaining.text =
                        String.format(context.getString(R.string.txt_characters_remaining), 100)
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        btnRemove.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                product = calculateTotalPriceChanged(product)
                val imm =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(edtChangeQuantity.windowToken, 0)
                imm.hideSoftInputFromWindow(edtSKUName.windowToken, 0)
                customChangeQuantityDialog.dismiss()
                productDataChangeListener.requestForRemove(product)
            }
        })
        btnDone.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (!StringHelper.isStringNullOrEmpty(edtChangeQuantity.text.toString()) && !StringHelper.isStringNullOrEmpty(
                        edtSKUName.text.toString()
                    )
                ) {
                    try {
                        val quantity =
                            edtChangeQuantity.text.toString().replace(",".toRegex(), ".").toDouble()
                        product!!.quantity = (quantity)
                        if (!product!!.isSelected) {
                            product!!.isSelected = true
                            isChanged = true
                        }
                        product!!.productName = edtSKUName.text.toString()
                        product!!.isCustom = true
                        product = calculateTotalPriceChanged(product)
                        val imm =
                            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(edtChangeQuantity.windowToken, 0)
                        imm.hideSoftInputFromWindow(edtSKUName.windowToken, 0)
                        customChangeQuantityDialog.dismiss()
                        //notify the adapter of change
                        productDataChangeListener.onDataChange(product, isChanged)
                    } catch (e: NumberFormatException) {
                        getToastRed(context.getString(R.string.txt_enter_valid_quantity))
                    }
                } else {
                    if (StringHelper.isStringNullOrEmpty(edtChangeQuantity.text.toString())) getToastRed(
                        context.getString(R.string.txt_enter_quantity)
                    )
                    if (StringHelper.isStringNullOrEmpty(edtSKUName.text.toString())) getToastRed(
                        context.getString(R.string.txt_enter_sku_name)
                    )
                }
            }
        })
        btnIncreaseQuantity.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val quantity = product!!.quantity + 1
                product!!.quantity = (quantity)
                edtChangeQuantity.setText(product!!.quantity.toString() + "")
                setTheMessagesForChangeQuantityDialog(
                    txtErrorMessage,
                    edtChangeQuantity,
                    lytReduceQuantity,
                    btnDone
                )
            }
        })
        imgDecQuantity.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                var quantity = product!!.quantity - 1
                if (quantity < 0) {
                    quantity = 0.0
                }
                product!!.quantity = (quantity)
                edtChangeQuantity.setText(product!!.quantity.toString() + "")
                setTheMessagesForChangeQuantityDialog(
                    txtErrorMessage,
                    edtChangeQuantity,
                    lytReduceQuantity,
                    btnDone
                )
            }
        })
        setTheMessagesForChangeQuantityDialog(
            txtErrorMessage,
            edtChangeQuantity,
            lytReduceQuantity,
            btnDone
        )
        customChangeQuantityDialog.show()
        return customChangeQuantityDialog
    }

    /**
     * sets the appropriate messages for the change quantity dialog
     *
     * @param txtErrorMessage
     * @param edtQuantity
     * @param lytReduceQuantity
     * @param btnDone
     */
    private fun setTheMessagesForChangeQuantityDialog(
        txtErrorMessage: TextView,
        edtQuantity: EditText,
        lytReduceQuantity: LinearLayout,
        btnDone: Button
    ) {
        if (product!!.quantity == 0.0) {
            //display the message that the quantity selected cannot be 0
            val message = context.getString(R.string.txt_bold_quantity_cannot_be_0)
            lytReduceQuantity.visibility = View.INVISIBLE
            txtErrorMessage.visibility = View.VISIBLE
            txtErrorMessage.text = Html.fromHtml(message)
            edtQuantity.setBackgroundResource(R.drawable.pinky_rect_border)
            edtQuantity.setTextColor(context.resources.getColor(R.color.pinky_red))
            btnDone.isClickable = false
            btnDone.background = context.resources.getDrawable(R.drawable.btn_rounded_dark_grey)
        } else if (product!!.quantity < product!!.moq!!) {
            val moqVal = product!!.moqDisplayValue
            val txtStockMessageText = String.format(
                context.getString(R.string.format_moq_is),
                moqVal,
                returnShortNameValueUnitSizeForQuantity(
                    product!!.unitSize
                )
            )
            lytReduceQuantity.visibility = View.VISIBLE
            txtErrorMessage.visibility = View.VISIBLE
            txtErrorMessage.text = Html.fromHtml(txtStockMessageText)
            edtQuantity.setBackgroundResource(R.drawable.pinky_rect_border)
            edtQuantity.setTextColor(context.resources.getColor(R.color.pinky_red))
            btnDone.isClickable = false
            btnDone.background = context.resources.getDrawable(R.drawable.btn_rounded_dark_grey)
        } else {
            lytReduceQuantity.visibility = View.VISIBLE
            txtErrorMessage.visibility = View.GONE
            edtQuantity.setBackgroundResource(R.color.faint_grey)
            edtQuantity.setTextColor(context.resources.getColor(R.color.black))
            btnDone.isClickable = true
            btnDone.background = context.resources.getDrawable(R.drawable.btn_rounded_blue)
        }
    }
}