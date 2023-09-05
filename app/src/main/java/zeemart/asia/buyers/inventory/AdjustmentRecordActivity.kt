package zeemart.asia.buyers.inventory

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.volley.VolleyError
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.inventory.InventoryDataManager.generateDeleteStockRequestJson
import zeemart.asia.buyers.models.UnitSizeModel.Companion.returnShortNameValueUnitSizeForQuantity
import zeemart.asia.buyers.models.inventory.AdjustmentReasons
import zeemart.asia.buyers.models.inventory.AdjustmentReasons.Companion.getReasonResId
import zeemart.asia.buyers.models.inventory.AdjustmentReasons.Companion.isNegativeAdjustmentReason
import zeemart.asia.buyers.models.inventory.InventoryResponse
import zeemart.asia.buyers.models.inventory.StockCountEntries
import zeemart.asia.buyers.network.OutletInventoryApi.StockageEntryResponseListener
import zeemart.asia.buyers.network.OutletInventoryApi.deleteStockCount
import zeemart.asia.buyers.network.OutletInventoryApi.retrieveInventoryStockageEntryById

/**
 * Created by RajPrudhviMarella on 02/Nov/2021.
 */
class AdjustmentRecordActivity : AppCompatActivity() {
    private lateinit var btnBack: ImageView
    private var txtProductName: TextView? = null
    private var txtSupplierName: TextView? = null
    private var txtAdjustmentQuantityValue: TextView? = null
    private var txtAdjustmentReason: TextView? = null
    private var txtInventoryList: TextView? = null
    private var txtNotesHeading: TextView? = null
    private var txtNotes: TextView? = null
    private var stockageId: String? = null
    private var lytAdjustmentDetails: ConstraintLayout? = null
    private lateinit var txtAdjustmentDeleted: TextView
    private var txtAdjustmentRecordScreenHeading: TextView? = null
    private var txtQuantityTitle: TextView? = null
    private var txtReasonTitle: TextView? = null
    private var txtInventoryListTitle: TextView? = null
    private lateinit var lytDelete: LinearLayout
    private var txtDeleteRecord: TextView? = null
    private var txtUpdatedByTitle: TextView? = null
    private var txtUpdatedByValue: TextView? = null
    private var lytTransfer: RelativeLayout? = null
    private var txtTransferHeader: TextView? = null
    private var txtTransferValue: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_adjustment_record)
        btnBack = findViewById(R.id.adjustment_record_back_btn)
        btnBack.setOnClickListener(View.OnClickListener { finish() })
        txtProductName = findViewById(R.id.txt_adjustment_record_product_name)
        txtSupplierName = findViewById(R.id.txt_adjustment_record_supplier_name)
        txtAdjustmentQuantityValue = findViewById(R.id.txt_adjustment_qty_value)
        txtAdjustmentReason = findViewById(R.id.txt_adjustment_reason_value)
        txtInventoryList = findViewById(R.id.txt_adjustment_inventory_list_value)
        txtNotes = findViewById(R.id.txt_adjustment_record_notes_values)
        txtAdjustmentRecordScreenHeading = findViewById(R.id.txt_adjustment_record)
        txtAdjustmentDeleted = findViewById(R.id.txt_adjustment_record_deleted)
        txtAdjustmentDeleted.setVisibility(View.GONE)
        txtNotesHeading = findViewById(R.id.txt_adjustment_record_notes_heading)
        lytAdjustmentDetails = findViewById(R.id.lyt_adjustment_record_details)
        txtQuantityTitle = findViewById(R.id.txt_adjustment_qty_title)
        txtReasonTitle = findViewById(R.id.txt_adjustment_reason_title)
        txtInventoryListTitle = findViewById(R.id.txt_adjustment_inventory_list_title)
        lytDelete = findViewById(R.id.lyt_delete_Stock)
        txtDeleteRecord = findViewById(R.id.txt_adjustment_delete_record)
        lytTransfer = findViewById(R.id.lyt_tranfer_to)
        txtTransferHeader = findViewById(R.id.txt_transfer_to_tag)
        txtTransferValue = findViewById(R.id.txt_transfer_to_Value)
        txtUpdatedByTitle = findViewById(R.id.txt_adjustment_updated_by_title)
        txtUpdatedByValue = findViewById(R.id.txt_adjustment_updated_by_value)
        lytDelete.setOnClickListener(View.OnClickListener { showDeleteAlert() })
        lytDelete.setVisibility(View.GONE)
        val bundle = intent.extras
        stockageId = bundle!!.getString(InventoryDataManager.INTENT_STOCKAGE_ID, "")
        adjustmentDetails
        setFont()
    }

    private fun showDeleteAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setPositiveButton(getString(R.string.txt_yes_delete)) { dialog, which -> //remove the row from the adapter and notify the adapter call the API also to remove from the backend
            btnDeleteClicked()
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.txt_cancel)) { dialog, which -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.setOnDismissListener { }
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setTitle(getString(R.string.txt_delete_dialog_title))
        dialog.show()
        val deleteBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
        deleteBtn.setTextColor(resources.getColor(R.color.chart_red))
        deleteBtn.isAllCaps = false
        val cancelBtn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        cancelBtn.isAllCaps = false
    }

    private fun setFont() {
        setTypefaceView(
            txtAdjustmentRecordScreenHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtProductName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtNotes, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtNotesHeading, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
        setTypefaceView(txtAdjustmentReason, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(
            txtAdjustmentQuantityValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtInventoryList, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtSupplierName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtQuantityTitle, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtInventoryListTitle, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtReasonTitle, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(
            txtAdjustmentDeleted,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        setTypefaceView(txtDeleteRecord, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtUpdatedByValue, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtUpdatedByTitle, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtTransferValue, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(txtTransferHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
    }

    private val adjustmentDetails: Unit
        private get() {
            retrieveInventoryStockageEntryById(
                this,
                stockageId,
                object : StockageEntryResponseListener {
                    override fun onSuccessResponse(data: InventoryResponse.StockCountEntryByOutlet?) {
                        if (data != null && data.data != null && data.data!!
                                .products != null && data.data!!.products?.size!! > 0
                        ) {
                            if (data.data!!.status
                                    .equals(StockCountEntries.InventoryActivityStatus.ACTIVE.value)
                            ) {
                                txtAdjustmentDeleted!!.visibility = View.GONE
                                lytDelete!!.visibility = View.VISIBLE
                            } else {
                                txtAdjustmentDeleted!!.visibility = View.VISIBLE
                                lytAdjustmentDetails!!.alpha = 0.5f
                                lytDelete!!.visibility = View.GONE
                            }
                            setUIDataForAdjustmentRecord(data.data!!)
                        }
                    }

                    override fun onErrorResponse(error: VolleyError?) {}
                })
        }

    private fun btnDeleteClicked() {
        val request = generateDeleteStockRequestJson(stockageId!!, " ")
        deleteStockCount(this, request, object : StockageEntryResponseListener {
            override fun onSuccessResponse(data: InventoryResponse.StockCountEntryByOutlet?) {
                if (data != null && data.data != null && data.data!!
                        .products != null && data.data!!.products?.size!! > 0
                ) {
                    if (data.data!!.status
                            .equals(StockCountEntries.InventoryActivityStatus.ACTIVE.value)
                    ) {
                        txtAdjustmentDeleted!!.visibility = View.GONE
                        lytDelete!!.visibility = View.VISIBLE
                        showErrorAlert()
                    } else {
                        txtAdjustmentDeleted!!.visibility = View.VISIBLE
                        lytAdjustmentDetails!!.alpha = 0.5f
                        lytDelete!!.visibility = View.GONE
                    }
                    setUIDataForAdjustmentRecord(data.data!!)
                } else {
                    showErrorAlert()
                }
            }

            override fun onErrorResponse(error: VolleyError?) {
                showErrorAlert()
            }
        })
    }

    private fun setUIDataForAdjustmentRecord(data: StockCountEntries) {
        if (data.status == StockCountEntries.InventoryActivityStatus.ACTIVE.value) {
            if (isNegativeAdjustmentReason(data.stockageReason!!)) {
                txtAdjustmentQuantityValue!!.setTextColor(resources.getColor(R.color.pinky_red))
            } else {
                txtAdjustmentQuantityValue!!.setTextColor(resources.getColor(R.color.black))
            }
        }
        if (SharedPref.readBool(
                SharedPref.USER_INVENTORY_SETTING_STATUS,
                false
            ) == true && data.products!![0].customName != null && !StringHelper.isStringNullOrEmpty(
                data.products!![0].customName
            )
        ) {
            txtProductName!!.text = data.products!![0].customName
        } else {
            txtProductName!!.text = data.products!![0].productName
        }
        txtSupplierName!!.text = data.products!![0].supplier!!.supplierName
        var name: String? = ""
        if (!StringHelper.isStringNullOrEmpty(data.updatedBy!!.firstName)) {
            name = data.updatedBy!!.firstName
        } else if (!StringHelper.isStringNullOrEmpty(data.updatedBy!!.lastName)) {
            name = name + data.updatedBy!!.lastName
        }
        txtUpdatedByValue!!.text = name
        txtAdjustmentQuantityValue!!.text =
            data.products!![0].stockQuantityDisplayValue + " " + returnShortNameValueUnitSizeForQuantity(
                data.products!![0].unitSize
            )
        if (getReasonResId(data.stockageReason!!) != null) txtAdjustmentReason!!.text =
            resources.getString(
                getReasonResId(data.stockageReason!!)!!
            )
        txtInventoryList!!.text = data.shelve!!.shelveName
        if (StringHelper.isStringNullOrEmpty(data.notes)) {
            txtNotes!!.visibility = View.GONE
            txtNotesHeading!!.visibility = View.GONE
        } else {
            txtNotesHeading!!.visibility = View.VISIBLE
            txtNotes!!.visibility = View.VISIBLE
            txtNotes!!.text = data.notes
        }
        if (data.selectedOutlet != null) {
            lytTransfer!!.visibility = View.VISIBLE
            if (data.stockageReason == AdjustmentReasons.Transfer_In.apiValue) {
                txtTransferHeader!!.text = resources.getString(R.string.txt_from)
            } else {
                txtTransferHeader!!.text = resources.getString(R.string.txt_to)
            }
            txtTransferValue!!.text = data.selectedOutlet!!.outletName
        } else {
            lytTransfer!!.visibility = View.GONE
        }
    }

    private fun showErrorAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setPositiveButton(getString(R.string.dialog_ok_button_text)) { dialog, which -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setTitle(getString(R.string.txt_cant_delete_adjustment))
        dialog.setMessage(getString(R.string.txt_cannot_delete))
        dialog.show()
    }
}