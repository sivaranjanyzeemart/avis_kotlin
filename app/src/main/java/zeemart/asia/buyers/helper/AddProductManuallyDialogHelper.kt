package zeemart.asia.buyers.helper

import android.app.Dialog
import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View.OnFocusChangeListener
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import zeemart.asia.buyers.R
import zeemart.asia.buyers.helper.StringHelper

/**
 * Created by ParulBhandari on 9/12/2018.
 */
object AddProductManuallyDialogHelper {
    fun createManuallyAddItemDialog(context: Context, listener: ManuallyAddedProductDataListener) {
        val d = Dialog(context)
        d.setContentView(R.layout.layout_input_item_manually)
        val inputLayoutItemName = d.findViewById<TextInputLayout>(R.id.input_layout_txt_item_name)
        val inputLayoutItemQuantity =
            d.findViewById<TextInputLayout>(R.id.input_layout_txt_item_quantity)
        val edtItemName = d.findViewById<TextInputEditText>(R.id.txt_item_name)
        val edtQuantity = d.findViewById<TextInputEditText>(R.id.txt_item_quantity)
        edtQuantity.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        val txtDone = d.findViewById<TextView>(R.id.txt_done_button)
        isAddItemManuallyButtonActive(edtItemName, edtQuantity, txtDone)
        txtDone.setOnClickListener {
            d.dismiss()
            listener.onProductDataReceived(edtItemName.text.toString(), edtQuantity.text.toString())
        }
        edtItemName.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (StringHelper.isStringNullOrEmpty(edtItemName.text.toString())) {
                    inputLayoutItemName.error = context.getString(R.string.txt_item_name_required)
                    edtItemName.setBackgroundResource(R.drawable.grey_rounded_corner_pink_border)
                    isAddItemManuallyButtonActive(edtItemName, edtQuantity, txtDone)
                } else {
                    inputLayoutItemName.error = ""
                    edtItemName.setBackgroundResource(R.drawable.grey_rounded_corner)
                    isAddItemManuallyButtonActive(edtItemName, edtQuantity, txtDone)
                }
            } else {
                inputLayoutItemName.error = ""
                edtItemName.setBackgroundResource(R.drawable.grey_rounded_corner)
                isAddItemManuallyButtonActive(edtItemName, edtQuantity, txtDone)
            }
        }
        edtQuantity.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (StringHelper.isStringNullOrEmpty(edtQuantity.text.toString())) {
                    inputLayoutItemQuantity.error =
                        context.getString(R.string.txt_quantity_required)
                    edtQuantity.setBackgroundResource(R.drawable.grey_rounded_corner_pink_border)
                    isAddItemManuallyButtonActive(edtItemName, edtQuantity, txtDone)
                } else {
                    inputLayoutItemQuantity.error = ""
                    edtQuantity.setBackgroundResource(R.drawable.grey_rounded_corner)
                    isAddItemManuallyButtonActive(edtItemName, edtQuantity, txtDone)
                }
            } else {
                inputLayoutItemQuantity.error = ""
                edtQuantity.setBackgroundResource(R.drawable.grey_rounded_corner)
                isAddItemManuallyButtonActive(edtItemName, edtQuantity, txtDone)
            }
        }
        edtQuantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                isAddItemManuallyButtonActive(edtItemName, edtQuantity, txtDone)
            }
        })
        edtItemName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                isAddItemManuallyButtonActive(edtItemName, edtQuantity, txtDone)
            }
        })
        d.show()
    }

    private fun isAddItemManuallyButtonActive(
        edtItemName: TextInputEditText,
        edtQuantity: TextInputEditText,
        txtDoneButton: TextView
    ): Boolean {
        val canAddItem: Boolean
        if (StringHelper.isStringNullOrEmpty(edtItemName.text.toString()) || StringHelper.isStringNullOrEmpty(
                edtQuantity.text.toString()
            )
        ) {
            canAddItem = false
            txtDoneButton.isClickable = false
            txtDoneButton.isEnabled = false
            txtDoneButton.setBackgroundResource(R.drawable.dark_grey_rounded_corner)
        } else {
            canAddItem = true
            txtDoneButton.isClickable = true
            txtDoneButton.isEnabled = true
            txtDoneButton.isFocusable = true
            txtDoneButton.setBackgroundResource(R.drawable.blue_rounded_corner)
        }
        return canAddItem
    }

    interface ManuallyAddedProductDataListener {
        fun onProductDataReceived(productName: String?, quantity: String?)
    }
}