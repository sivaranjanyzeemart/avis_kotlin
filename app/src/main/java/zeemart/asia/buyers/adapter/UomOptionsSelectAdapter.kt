package zeemart.asia.buyers.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.inventory.UomOptionsSelectActivity.UomItemCountListener
import zeemart.asia.buyers.models.UnitSizeModel
import zeemart.asia.buyers.models.inventory.InventoryProduct.OrderByUnitConversion

class UomOptionsSelectAdapter(
    private val context: Context,
    orderByUnitConversions: List<OrderByUnitConversion>,
    listener: UomItemCountListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var orderByUnitConversions: List<OrderByUnitConversion> = ArrayList()
    private val listener: UomItemCountListener

    init {
        this.orderByUnitConversions = orderByUnitConversions
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_uom_options_row, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        val holderItem = holder as ViewHolder
        holderItem.txtOrderByUOM.text = UnitSizeModel.returnShortNameValueUnitSizeForQuantity(
            orderByUnitConversions[position].orderByUnit
        )
        val orderByUom = orderByUnitConversions[position].orderByUnit
        val stockUnit = orderByUnitConversions[position].stockUnit
        if (orderByUnitConversions[position].orderByUnit != orderByUnitConversions[position].stockUnit) {
            val value =
                ZeemartBuyerApp.getDoubleToString(orderByUnitConversions[position].orderByUnitQtyConversionValue)
            holderItem.txtOtherUOM.text =
                " ($value " + UnitSizeModel.returnShortNameValueUnitSizeForQuantity(
                    orderByUnitConversions[position].stockUnit
                ) + ")"
        }
        holderItem.txtUnitSize.text = UnitSizeModel.returnShortNameValueUnitSizeForQuantity(
            orderByUnitConversions[position].orderByUnit
        )
        if (orderByUnitConversions[position].selectedQuantity == null) {
            holderItem.btnDecrease.visibility = View.GONE
            holderItem.edtQuantity.setText("")
        } else {
            holderItem.btnDecrease.visibility = View.VISIBLE
            holderItem.edtQuantity.setText(orderByUnitConversions[position].displaySelectedQuantityValue())
        }
        holderItem.btnDecrease.setOnClickListener {
            var value = 0.0
            value = orderByUnitConversions[position].selectedQuantity!!
            if (value > 0) {
                value = value - 1
                orderByUnitConversions[position].selectedQuantity = value
                holderItem.edtQuantity.setText(orderByUnitConversions[position].displaySelectedQuantityValue())
                holderItem.edtQuantity.setSelection(holderItem.edtQuantity.text.length)
                listener.onInventoryItemCounted(orderByUnitConversions)
            }
        }
        holderItem.btnIncrease.setOnClickListener {
            holderItem.btnDecrease.visibility = View.VISIBLE
            if (orderByUnitConversions[position].selectedQuantity == null) {
                orderByUnitConversions[position].selectedQuantity = 1.0
                holderItem.edtQuantity.setText(orderByUnitConversions[position].displaySelectedQuantityValue())
                holderItem.edtQuantity.setSelection(holderItem.edtQuantity.text.length)
            } else {
                var value = orderByUnitConversions[position].selectedQuantity
                value = value?.plus(1)
                orderByUnitConversions[position].selectedQuantity = value
                holderItem.edtQuantity.setText(orderByUnitConversions[position].displaySelectedQuantityValue())
                holderItem.edtQuantity.setSelection(holderItem.edtQuantity.text.length)
            }
            listener.onInventoryItemCounted(orderByUnitConversions)
        }
        holderItem.edtQuantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val value = holderItem.edtQuantity.text.toString()
                if (value != "") {
                    holderItem.btnDecrease.visibility = View.VISIBLE
                    try {
                        val `val` = value.toDouble()
                        orderByUnitConversions[holderItem.adapterPosition].selectedQuantity = `val`
                        listener.onInventoryItemCounted(orderByUnitConversions)
                    } catch (e: NumberFormatException) {
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        holderItem.edtQuantity.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            listener.onInventoryItemCounted(orderByUnitConversions)
        }
    }

    fun setButtonTitle(viewHolder: ViewHolder?) {
        var value = 0.0
        var i = 0
        while (i > orderByUnitConversions.size) {
            if (orderByUnitConversions[i].selectedQuantity != null && orderByUnitConversions[i].selectedQuantity != 0.0) {
                value =
                    value + (orderByUnitConversions[i].orderByUnitQty?.times(orderByUnitConversions[i].selectedQuantity!!)!!)
            }
            i++
        }
    }

    override fun getItemCount(): Int {
        return orderByUnitConversions.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var btnIncrease: ImageButton
        var btnDecrease: ImageButton
        var edtQuantity: EditText
        var txtUnitSize: TextView
        var txtOtherUOM: TextView
        var txtOrderByUOM: TextView

        init {
            btnIncrease = itemView.findViewById(R.id.btn_inc_quantity)
            btnDecrease = itemView.findViewById(R.id.btn_reduce_quantity)
            edtQuantity = itemView.findViewById(R.id.edt_stock_count_value)
            txtOtherUOM = itemView.findViewById(R.id.txt_other_uom)
            txtOrderByUOM = itemView.findViewById(R.id.txt_order_by_uom)
            checkFocusableViewOnNextAndDonePress()
            txtUnitSize = itemView.findViewById(R.id.txt_product_unit_size)
            ZeemartBuyerApp.setTypefaceView(
                txtOtherUOM,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtOrderByUOM,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                edtQuantity,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtUnitSize,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }

        fun checkFocusableViewOnNextAndDonePress() {
            edtQuantity.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                    val v1 = v.focusSearch(View.FOCUS_DOWN)
                    if (v1 != null) {
                        if (!v.requestFocus(View.FOCUS_FORWARD)) {
                            return@OnEditorActionListener true
                        }
                    }
                    false
                } else false
            })
        }
    }
}