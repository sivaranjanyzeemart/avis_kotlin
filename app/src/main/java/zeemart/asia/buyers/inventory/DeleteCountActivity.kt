package zeemart.asia.buyers.inventory

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.VolleyError
import zeemart.asia.buyers.R
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.inventory.InventoryResponse
import zeemart.asia.buyers.network.OutletInventoryApi

class DeleteCountActivity : AppCompatActivity() {
    private lateinit var btnCross: ImageButton
    private var btnDelete: Button? = null
    private lateinit var edtReason: EditText
    private var stockageId: String? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_count)
        val bundle: Bundle? = getIntent().getExtras()
        if (bundle != null) {
            stockageId = bundle.getString(InventoryDataManager.INTENT_STOCKAGE_ID, "")
        }
        btnDelete = findViewById<Button>(R.id.btn_delete_stock_count)
        btnDelete!!.setOnClickListener { btnDeleteClicked() }
        btnCross = findViewById<ImageButton>(R.id.btn_close)
        btnCross.setOnClickListener(View.OnClickListener { finish() })
        edtReason = findViewById<EditText>(R.id.edt_reason)
        edtReason.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setDeleteButtonBackground()
            }

            override fun afterTextChanged(s: Editable) {}
        })
        setDeleteButtonBackground()
    }

    private fun btnDeleteClicked() {
        val request = InventoryDataManager.generateDeleteStockRequestJson(
            stockageId!!,
            edtReason.getText().toString()
        )
        OutletInventoryApi.deleteStockCount(this, request, object :
            OutletInventoryApi.StockageEntryResponseListener {
            override fun onSuccessResponse(data: InventoryResponse.StockCountEntryByOutlet?) {
                finish()
            }

            override fun onErrorResponse(error: VolleyError?) {}
        })
    }

    private fun setDeleteButtonBackground() {
        if (StringHelper.isStringNullOrEmpty(edtReason.getText().toString())) {
            btnDelete!!.isClickable = false
            btnDelete!!.setBackgroundResource(R.drawable.dark_grey_rounded_corner)
        } else {
            btnDelete!!.isClickable = true
            btnDelete!!.setBackgroundResource(R.drawable.pink_rounded_corner)
        }
    }
}