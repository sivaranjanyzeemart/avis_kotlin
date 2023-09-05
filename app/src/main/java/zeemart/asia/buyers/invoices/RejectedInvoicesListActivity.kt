package zeemart.asia.buyers.invoices

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewBlue
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.EditInvoiceListener
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.invoice.Invoice
import zeemart.asia.buyers.models.invoice.InvoiceMgr
import zeemart.asia.buyers.models.invoice.InvoiceUploadsListDataMgr
import zeemart.asia.buyers.network.InvoiceHelper
import zeemart.asia.buyers.network.VolleyErrorHelper
import java.io.File
import java.util.*

/**
 * Created by RajPrudhviMarella on 20/Dec/2021.
 */
class RejectedInvoicesListActivity : AppCompatActivity() {
    private var txtHeader: TextView? = null
    private var backButton: ImageView? = null
    private var lstRejectedInvoices: RecyclerView? = null
    private val invoiceRejectedList: MutableList<InvoiceUploadsListDataMgr> =
        Collections.synchronizedList<InvoiceUploadsListDataMgr>(
            ArrayList<InvoiceUploadsListDataMgr>()
        )
    private var invoiceDataManager: InvoiceDataManager? = null
    private var threeDotLoaderBlue: CustomLoadingViewBlue? = null
    protected override fun onResume() {
        super.onResume()
        callRejectedInvoicesApi()
    }

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rejected_invoices_list)
        threeDotLoaderBlue = findViewById<CustomLoadingViewBlue>(R.id.spin_kit_loader_invoice_blue)
        txtHeader = findViewById<TextView>(R.id.txt_adjustment_record)
        ZeemartBuyerApp.setTypefaceView(
            txtHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        invoiceDataManager = InvoiceDataManager(this)
        backButton = findViewById<ImageView>(R.id.adjustment_record_back_btn)
        backButton!!.setOnClickListener {
            val intent = Intent()
            setResult(Activity.RESULT_CANCELED, intent)
            finish()
        }
        lstRejectedInvoices = findViewById<RecyclerView>(R.id.lst_rejected_invoices)
        lstRejectedInvoices!!.setLayoutManager(LinearLayoutManager(this))
    }

    override fun onBackPressed() {
        val intent = Intent()
        setResult(Activity.RESULT_CANCELED, intent)
        finish()
    }

    private fun initUploadInvoiceSwipe() {
        val uploadedInvoiceSwipeHelper: ItemTouchHelper.SimpleCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                private val background: ColorDrawable = ColorDrawable()
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    if (lstRejectedInvoices?.getAdapter() != null && lstRejectedInvoices?.getAdapter() is InvoicesRejectedListAdapter) {
                        val mAdapter =
                            lstRejectedInvoices?.getAdapter() as InvoicesRejectedListAdapter
                        if (mAdapter != null) {
                            val deleteItemPosition: Int = viewHolder.getAdapterPosition()
                            if (invoiceRejectedList[deleteItemPosition].inQueueForUploadInvoice != null) {
                                //call the task to remove the invoice details from the internal memory
                                invoiceDataManager!!.deleteDirectory(File(invoiceRejectedList[deleteItemPosition].inQueueForUploadInvoice?.imageDirectoryPath))
                                invoiceRejectedList.removeAt(deleteItemPosition)
                                //notify the adapter of deletion
                                mAdapter.notifyDataChanged(
                                    invoiceRejectedList,
                                    true,
                                    false,
                                    false,
                                    deleteItemPosition
                                )
                            } else if (invoiceRejectedList[deleteItemPosition].uploadedInvoice != null) {
                                createDeleteUploadedInvoiceDialog(mAdapter, deleteItemPosition)
                            }
                        }
                    }
                }

                override fun getSwipeDirs(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ): Int {
                    return if (lstRejectedInvoices?.getAdapter() != null && lstRejectedInvoices?.getAdapter() is InvoicesRejectedListAdapter) {
                        val mAdapter =
                            lstRejectedInvoices?.getAdapter() as InvoicesRejectedListAdapter
                        if (mAdapter != null) {
                            val currentItemPosition: Int = viewHolder.getAdapterPosition()
                            if (invoiceRejectedList[currentItemPosition].inQueueForUploadInvoice != null) {
                                val invoiceStatus: InQueueForUploadDataModel.InvoiceStatus? =
                                    invoiceRejectedList[currentItemPosition].inQueueForUploadInvoice?.status
                                if (invoiceStatus == InQueueForUploadDataModel.InvoiceStatus.FAILED || invoiceStatus == InQueueForUploadDataModel.InvoiceStatus.IN_QUEUE) {
                                    ItemTouchHelper.LEFT
                                } else {
                                    0
                                }
                            } else if (invoiceRejectedList[currentItemPosition].uploadedInvoice != null) {
                                val uploadedInvoice: InvoiceMgr? =
                                    invoiceRejectedList[currentItemPosition].uploadedInvoice
                                //PNF-340 created by user check, removed as permissed to view invoice added.
                                //UserDetails createdBy = ((InvoiceMgr) inQueueAndUploadedList.get(currentItemPosition)).getCreatedBy();
                                if (uploadedInvoice?.isStatus(Invoice.Status.PENDING) == true || uploadedInvoice?.isStatus(
                                        Invoice.Status.REJECTED
                                    ) == true
                                ) {
                                    ItemTouchHelper.LEFT
                                } else {
                                    0
                                }
                            } else {
                                0
                            }
                        } else {
                            0
                        }
                    } else {
                        0
                    }
                }

                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    if (lstRejectedInvoices?.getAdapter() != null && lstRejectedInvoices?.getAdapter() is InvoicesRejectedListAdapter) {
                        val text: String = getString(R.string.txt_delete)
                        //InvoiceUploadsAdapter.ViewHolderItem mViewHolder = (InvoiceUploadsAdapter.ViewHolderItem) viewHolder;
                        val color: Int = getResources().getColor(R.color.pinky_red)
                        background.setColor(color)
                        background.setBounds(
                            viewHolder.itemView.getRight() + dX.toInt(),
                            viewHolder.itemView.getTop(),
                            viewHolder.itemView.getRight(),
                            viewHolder.itemView.getBottom()
                        )
                        background.draw(c)
                        val p = Paint()
                        val buttonWidth: Int = viewHolder.itemView.getRight() - SWIPE_BUTTON_WIDTH
                        val rightButton = RectF(
                            buttonWidth.toFloat(),
                            viewHolder.itemView.getTop().toFloat(),
                            viewHolder.itemView.getRight().toFloat(),
                            viewHolder.itemView.getBottom().toFloat()
                        )
                        p.color = color
                        c.drawRect(rightButton, p)
                        p.color = Color.WHITE
                        p.isAntiAlias = true
                        p.textSize = SWIPE_BUTTON_TEXT_SIZE.toFloat()
                        val textWidth = p.measureText(text)
                        val bmp: Bitmap = BitmapFactory.decodeResource(
                            getResources(),
                            R.drawable.deletedraft
                        )
                        val bounds = Rect()
                        p.getTextBounds(text, 0, text.length, bounds)
                        val combinedHeight: Float =
                            (bmp.getHeight() + SWIPE_BUTTON_PADDING_BETWEEN_TEXT_IMAGE + bounds.height()).toFloat()
                        c.drawBitmap(
                            bmp,
                            rightButton.centerX() - bmp.getWidth() / 2,
                            rightButton.centerY() - combinedHeight / 2,
                            null
                        )
                        c.drawText(
                            text,
                            rightButton.centerX() - textWidth / 2,
                            rightButton.centerY() + combinedHeight / 2,
                            p
                        )
                        super.onChildDraw(
                            c,
                            recyclerView,
                            viewHolder,
                            dX,
                            dY,
                            actionState,
                            isCurrentlyActive
                        )
                    }
                }
            }
        val helperUploadInvoice = ItemTouchHelper(uploadedInvoiceSwipeHelper)
        helperUploadInvoice.attachToRecyclerView(lstRejectedInvoices)
    }

    fun createDeleteUploadedInvoiceDialog(
        mAdapter: InvoicesRejectedListAdapter, deleteItemPosition: Int
    ) {
        val builder = AlertDialog.Builder(this@RejectedInvoicesListActivity)
        builder.setPositiveButton(
            getString(R.string.txt_yes_delete),
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    //remove the row from the adapter and notify the adapter call the API also to remove from the backend
                    val json: String = InvoiceHelper.createDeleteJsonForInvoice(
                        invoiceRejectedList[deleteItemPosition].uploadedInvoice?.invoiceId
                    )
                    val outlet: MutableList<Outlet> = ArrayList<Outlet>()
                    outlet.add(SharedPref.defaultOutlet!!)
                    InvoiceHelper.deleteUnprocessedInvoice(
                        this@RejectedInvoicesListActivity,
                        outlet,
                        object : GetRequestStatusResponseListener {
                            override fun onSuccessResponse(status: String?) {
                                callRejectedInvoicesApi()
                            }

                            override fun onErrorResponse(error: VolleyErrorHelper?) {
                                ZeemartBuyerApp.getToastRed(getString(R.string.txt_delete_draft_fail))
                            }
                        },
                        json
                    )

                    //Remove from the list and update the adapter
                    invoiceRejectedList.removeAt(deleteItemPosition)
                    mAdapter.notifyDataChanged(
                        invoiceRejectedList,
                        true,
                        false,
                        false,
                        deleteItemPosition
                    )
                    dialog.dismiss()
                }
            })
        builder.setNegativeButton(
            getString(R.string.txt_cancel),
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    dialog.dismiss()
                }
            })
        val dialog = builder.create()
        dialog.setOnDismissListener(object : DialogInterface.OnDismissListener {
            override fun onDismiss(dialog: DialogInterface) {
                mAdapter.notifyDataChanged(
                    invoiceRejectedList,
                    false,
                    false,
                    true,
                    deleteItemPosition
                )
            }
        })
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setTitle(getString(R.string.txt_delete_invoice))
        dialog.setMessage(getString(R.string.txt_want_to_delete_the_invoice))
        dialog.show()
        val deleteBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
        deleteBtn.setTextColor(getResources().getColor(R.color.chart_red))
        deleteBtn.isAllCaps = false
        val cancelBtn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        cancelBtn.isAllCaps = false
    }

    private fun callRejectedInvoicesApi() {
        threeDotLoaderBlue?.setVisibility(View.VISIBLE)
        val outlet: MutableList<Outlet> = ArrayList<Outlet>()
        outlet.add(SharedPref.defaultOutlet!!)
        InvoiceHelper.GetAllInvoicesForParticularOrder(this, null, true, object :
            InvoiceHelper.GetInvoices {
            override fun result(invoices: List<Invoice>?) {
                threeDotLoaderBlue?.setVisibility(View.GONE)
                invoiceRejectedList.clear()
                if (invoices != null) {
                    Log.e("GetAllPendingInvoices", "invoices.size()" + invoices.size)
                    val invoicePendingList: ArrayList<InvoiceMgr> =
                        ZeemartBuyerApp.gsonExposeExclusive.fromJson<ArrayList<InvoiceMgr>>(
                            ZeemartBuyerApp.gsonExposeExclusive.toJson(invoices).toString(),
                            object : TypeToken<ArrayList<InvoiceMgr?>?>() {}.type
                        )
                    Collections.sort<InvoiceMgr>(invoicePendingList, Invoice.TimeCreatedCompare())
                    invoiceRejectedList.addAll(
                        InvoiceUploadsListDataMgr.deserializeInvoiceMgrToInvoiceUploads(
                            invoicePendingList
                        )
                    )
                    if (invoiceRejectedList.size > 0) {
                        invoiceRejectedList.add(
                            0,
                            InvoiceUploadsListDataMgr.getInstanceWithHeaderData(
                                InvoiceUploadsListDataMgr.UploadListHeader.Uploaded
                            )
                        )
                    }
                    val adapter = InvoicesRejectedListAdapter(
                        invoiceRejectedList,
                        false,
                        this@RejectedInvoicesListActivity,
                        object : EditInvoiceListener {
                            override fun retryUploadOfFailedInvoices(
                                failedInvoice: List<InQueueForUploadDataModel?>?,
                                retryInvoicePosition: Int
                            ) {
                            }

                            override fun getSelectedInvoicesDeleteOrMerge(selectedInvoices: List<InvoiceUploadsListDataMgr?>?) {}
                            override fun invoicesCountByStatus() {}
                            override fun onEditButtonClicked() {}
                            override fun onListItemLongPressed() {}
                        })
                    if (lstRejectedInvoices?.getAdapter() != null) {
                        lstRejectedInvoices!!.getAdapter()?.notifyDataSetChanged()
                    } else {
                        lstRejectedInvoices?.setAdapter(adapter)
                        initUploadInvoiceSwipe()
                    }
                    lstRejectedInvoices?.setVisibility(View.VISIBLE)
                } else {
                    lstRejectedInvoices?.setVisibility(View.GONE)
                }
            }
        })
    }

    protected override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == ZeemartAppConstants.ResultCode.RESULT_IMAGES) {
            removeAt()
        }
    }

    fun removeAt() {
        val invoiceId: String? = SharedPref.read(SharedPref.INVOICE_ID_DELETE, "")
        if (!StringHelper.isStringNullOrEmpty(invoiceId)) {
            val json: String = InvoiceHelper.createDeleteJsonForInvoice(invoiceId)
            val outlet: MutableList<Outlet> = ArrayList<Outlet>()
            outlet.add(SharedPref.defaultOutlet!!)
            InvoiceHelper.deleteUnprocessedInvoice(
                this@RejectedInvoicesListActivity,
                outlet,
                object : GetRequestStatusResponseListener {
                    override fun onSuccessResponse(status: String?) {
                        callRejectedInvoicesApi()
                    }

                    override fun onErrorResponse(error: VolleyErrorHelper?) {
                        ZeemartBuyerApp.getToastRed(getString(R.string.txt_delete_draft_fail))
                    }
                },
                json
            )
        }
    }

    companion object {
        private val SWIPE_BUTTON_WIDTH: Int = CommonMethods.dpToPx(70)
        private val SWIPE_BUTTON_TEXT_SIZE: Int = CommonMethods.dpToPx(14)
        private val SWIPE_BUTTON_PADDING_BETWEEN_TEXT_IMAGE: Int = CommonMethods.dpToPx(11)
    }
}