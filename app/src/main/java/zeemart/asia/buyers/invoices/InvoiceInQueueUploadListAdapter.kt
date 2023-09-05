package zeemart.asia.buyers.invoices

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.squareup.picasso.Picasso
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastRed
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.adapter.InvoiceImagesSliderAdapter
import zeemart.asia.buyers.adapter.InvoiceImagesSliderAdapter.InvoiceImageListener
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.*
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.EditInvoiceListener
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.models.ImagesModel
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.invoice.Invoice
import zeemart.asia.buyers.models.invoice.InvoiceMgr
import zeemart.asia.buyers.models.invoice.InvoiceUploadsListDataMgr
import zeemart.asia.buyers.network.InvoiceHelper.createDeleteJsonForInvoice
import zeemart.asia.buyers.network.InvoiceHelper.deleteUnprocessedInvoice
import zeemart.asia.buyers.network.VolleyErrorHelper
import java.io.File

/**
 * Created by ParulBhandari on 7/2/2018.
 */
class InvoiceInQueueUploadListAdapter(
    private val context: Context,
    private var lstInQueueAndUploadedList: MutableList<InvoiceUploadsListDataMgr>,
    private val listener: EditInvoiceListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    private var isDeleteOrMerge = false
    private val invoicesSelectedForMergeDelete: MutableList<InvoiceUploadsListDataMgr> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_HEADING) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_invoice_upload_tab_list_header, parent, false)
            return ViewHolderHeading(v)
        } else if (viewType == VIEW_TYPE_IN_QUEUE) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_invoice_in_queue, parent, false)
            return ViewHolderInQueue(v)
        } else if (viewType == VIEW_TYPE_UPLOADED) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_invoice_uploads, parent, false)
            return ViewHolderUploaded(v)
        } else {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_invoice_uploads, parent, false)
            return ViewHolderUploaded(v)        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        if (lstInQueueAndUploadedList[position].header != null) {
            val headerHolder = holder as ViewHolderHeading?
            headerHolder!!.txtHeading.text =
                context.getString(lstInQueueAndUploadedList.get(position).header!!.headerResId)
            if (lstInQueueAndUploadedList[position].header!!.headerType == 1) {
                if (isDeleteOrMerge) {
                    headerHolder.btnMergeOrDelete.visibility = View.GONE
                } else {
                    headerHolder.btnMergeOrDelete.visibility = View.VISIBLE
                }
            } else {
                headerHolder.btnMergeOrDelete.visibility = View.GONE
            }
            headerHolder.btnMergeOrDelete.setOnClickListener(View.OnClickListener {
                setDeleteMergeActiveInactive(true)
                listener.onEditButtonClicked()
            })
        } else if (lstInQueueAndUploadedList[position].inQueueForUploadMergedInvoice != null) {
            val item =
                lstInQueueAndUploadedList[position].inQueueForUploadMergedInvoice!![lstInQueueAndUploadedList[position].inQueueForUploadMergedInvoice!!.size - 1]
            val viewHolderItem = holder as ViewHolderInQueue?
            if ((item.status == InQueueForUploadDataModel.InvoiceStatus.INVOICE_UPLOADING)) {
                viewHolderItem!!.inQueueRow.setBackgroundColor(context.resources.getColor(R.color.opacity50))
                viewHolderItem.imgInvoice.alpha = 0.5f
                viewHolderItem.txtInvoiceName.alpha = 0.5f
                viewHolderItem.txtInvoiceStatus.alpha = 0.5f
            } else {
                viewHolderItem!!.inQueueRow.setBackgroundColor(context.resources.getColor(R.color.white))
                viewHolderItem.imgInvoice.alpha = 1f
                viewHolderItem.txtInvoiceName.alpha = 1f
                viewHolderItem.txtInvoiceStatus.alpha = 1f
            }
            if ((item.status == InQueueForUploadDataModel.InvoiceStatus.IN_QUEUE) || (item.status == InQueueForUploadDataModel.InvoiceStatus.FAILED)) {
                if (isDeleteOrMerge) {
                    viewHolderItem.chkSelectInvoice.visibility = View.VISIBLE
                } else {
                    viewHolderItem.chkSelectInvoice.visibility = View.GONE
                }
                viewHolderItem.imgSwipeIndicator.visibility = View.VISIBLE
            } else {
                viewHolderItem.chkSelectInvoice.visibility = View.GONE
                viewHolderItem.imgSwipeIndicator.visibility = View.INVISIBLE
            }
            if ((item.status == InQueueForUploadDataModel.InvoiceStatus.IN_QUEUE)) {
                viewHolderItem.txtInvoiceStatus.setText(R.string.txt_waiting_to_upload)
                viewHolderItem.btnRetry.visibility = View.GONE
                viewHolderItem.txtUploadFailedIndicator.visibility = View.GONE
                viewHolderItem.txtInvoiceStatus.setTextColor(context.resources.getColor(R.color.grey_medium))
                viewHolderItem.progressbarInvoiceUploading.visibility = View.GONE
            } else if ((item.status == InQueueForUploadDataModel.InvoiceStatus.FAILED)) {
                viewHolderItem.txtInvoiceStatus.setText(R.string.txt_failed_to_upload)
                viewHolderItem.btnRetry.visibility = View.VISIBLE
                viewHolderItem.txtUploadFailedIndicator.visibility = View.VISIBLE
                viewHolderItem.txtInvoiceStatus.setTextColor(context.resources.getColor(R.color.pinky_red))
                viewHolderItem.progressbarInvoiceUploading.visibility = View.GONE
            } else if ((item.status == InQueueForUploadDataModel.InvoiceStatus.INVOICE_UPLOADING)) {
                viewHolderItem.txtInvoiceStatus.setText(R.string.txt_uploading)
                viewHolderItem.btnRetry.visibility = View.GONE
                viewHolderItem.txtUploadFailedIndicator.visibility = View.GONE
                viewHolderItem.txtInvoiceStatus.setTextColor(context.resources.getColor(R.color.text_blue))
                viewHolderItem.progressbarInvoiceUploading.visibility = View.VISIBLE
            } else {
                viewHolderItem.btnRetry.visibility = View.GONE
                viewHolderItem.txtUploadFailedIndicator.visibility = View.GONE
                viewHolderItem.txtInvoiceStatus.text = ""
                viewHolderItem.progressbarInvoiceUploading.visibility = View.GONE
            }
            val imageFilePath =
                item.imageFilePath!!.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            val imageName = imageFilePath[imageFilePath.size - 1]
            viewHolderItem.txtInvoiceName.text = String.format(
                context.getString(R.string.format_value_files),
                "" + lstInQueueAndUploadedList.get(position).inQueueForUploadMergedInvoice!!.size
            )
            Picasso.get().load(File(item.imageFilePath)).resize(
                ViewHolderInQueue.PLACE_HOLDER_INVOICE_IMAGE_WIDTH,
                ViewHolderInQueue.PLACE_HOLDER_INVOICE_IMAGE_HEIGHT
            ).into(
                viewHolderItem.imgInvoice
            )
            //viewHolderItem.txtInvoiceName.setVisibility(View.GONE);
            viewHolderItem.txtTimeUploaded.visibility = View.GONE
            if (item.isInvoiceSelectedForDeleteOrMerge && isDeleteOrMerge) {
                viewHolderItem.chkSelectInvoice.isChecked = true
            } else {
                viewHolderItem.chkSelectInvoice.isChecked = false
            }
            //added for PNF-2501
            viewHolderItem.itemView.setOnLongClickListener(object : OnLongClickListener {
                override fun onLongClick(v: View): Boolean {
                    if (lstInQueueAndUploadedList[position].inQueueForUploadMergedInvoice != null) {
                        for (i in lstInQueueAndUploadedList[position].inQueueForUploadMergedInvoice!!.indices) {
                            lstInQueueAndUploadedList[position].inQueueForUploadMergedInvoice!![i].isInvoiceSelectedForDeleteOrMerge =
                                true
                        }
                        invoicesSelectedForMergeDelete.add(lstInQueueAndUploadedList[position])
                        viewHolderItem.chkSelectInvoice.isChecked = true
                    }
                    setDeleteMergeActiveInactive(true)
                    listener.onListItemLongPressed()
                    return true
                }
            })
            viewHolderItem.chkSelectInvoice.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    if (viewHolderItem.chkSelectInvoice.isChecked) {
                        if (invoicesSelectedForMergeDelete.size == 0) {
                            if (lstInQueueAndUploadedList[position].inQueueForUploadMergedInvoice != null) {
                                for (i in lstInQueueAndUploadedList[position].inQueueForUploadMergedInvoice!!.indices) {
                                    lstInQueueAndUploadedList[position].inQueueForUploadMergedInvoice!![i].isInvoiceSelectedForDeleteOrMerge =
                                        true
                                }
                                invoicesSelectedForMergeDelete.add(lstInQueueAndUploadedList[position])
                                viewHolderItem.chkSelectInvoice.isChecked = true
                            }
                        } else {
                            var hasInvoiceManagerData = false
                            for (i in invoicesSelectedForMergeDelete.indices) {
                                if (invoicesSelectedForMergeDelete[i].uploadedInvoice != null) {
                                    //((InvoiceMgr) lstInQueueAndUploadedList.get(position)).setInvoiceSelected(false);
                                    hasInvoiceManagerData = true
                                    break
                                }
                            }
                            if (hasInvoiceManagerData) {
                                invoicesSelectedForMergeDelete.clear()
                                for (j in lstInQueueAndUploadedList.indices) {
                                    if (lstInQueueAndUploadedList[j].uploadedInvoice != null) {
                                        lstInQueueAndUploadedList[j].uploadedInvoice!!.isInvoiceSelected =
                                            false
                                    }
                                }
                            }
                            if (lstInQueueAndUploadedList[position].inQueueForUploadMergedInvoice != null) {
                                for (i in lstInQueueAndUploadedList[position].inQueueForUploadMergedInvoice!!.indices) {
                                    lstInQueueAndUploadedList[position].inQueueForUploadMergedInvoice!![i].isInvoiceSelectedForDeleteOrMerge =
                                        true
                                }
                                invoicesSelectedForMergeDelete.add(lstInQueueAndUploadedList[position])
                                viewHolderItem.chkSelectInvoice.isChecked = true
                            }
                        }
                    } else {
                        val deSelectedInvoice: Any = lstInQueueAndUploadedList[position]
                        for (i in invoicesSelectedForMergeDelete.indices) {
                            if (invoicesSelectedForMergeDelete[i].inQueueForUploadMergedInvoice != null && (deSelectedInvoice as InvoiceUploadsListDataMgr).inQueueForUploadMergedInvoice != null) {
                                if ((invoicesSelectedForMergeDelete[i].inQueueForUploadMergedInvoice!![0].imageFilePath == deSelectedInvoice.inQueueForUploadMergedInvoice!![0].imageFilePath)) {
                                    for (j in lstInQueueAndUploadedList[position].inQueueForUploadMergedInvoice!!.indices) {
                                        lstInQueueAndUploadedList[position].inQueueForUploadMergedInvoice!![j].isInvoiceSelectedForDeleteOrMerge =
                                            false
                                    }
                                    invoicesSelectedForMergeDelete.removeAt(i)
                                }
                            }
                        }
                        viewHolderItem.chkSelectInvoice.isChecked = false
                    }
                    listener.getSelectedInvoicesDeleteOrMerge(invoicesSelectedForMergeDelete)
                    notifyDataSetChanged()
                }
            })
            viewHolderItem.btnRetry.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    //Add to the request queue
                    listener.retryUploadOfFailedInvoices(
                        lstInQueueAndUploadedList[position].inQueueForUploadMergedInvoice,
                        position
                    )
                    /*lstInQueueAndUploadedList.remove(position);
                    notifyDataChanged(lstInQueueAndUploadedList,true,false,false,position);*/
                }
            })
        } else if (lstInQueueAndUploadedList[position].inQueueForUploadInvoice != null) {
            val item = lstInQueueAndUploadedList[position].inQueueForUploadInvoice
            val viewHolderItem = holder as ViewHolderInQueue?
            if ((item!!.status == InQueueForUploadDataModel.InvoiceStatus.INVOICE_UPLOADING)) {
                viewHolderItem!!.inQueueRow.setBackgroundColor(context.resources.getColor(R.color.opacity50))
                viewHolderItem.imgInvoice.alpha = 0.5f
                viewHolderItem.txtInvoiceName.alpha = 0.5f
                viewHolderItem.txtInvoiceStatus.alpha = 0.5f
            } else {
                viewHolderItem!!.inQueueRow.setBackgroundColor(context.resources.getColor(R.color.white))
                viewHolderItem.imgInvoice.alpha = 1f
                viewHolderItem.txtInvoiceName.alpha = 1f
                viewHolderItem.txtInvoiceStatus.alpha = 1f
            }
            if ((item.status == InQueueForUploadDataModel.InvoiceStatus.IN_QUEUE) || (item.status == InQueueForUploadDataModel.InvoiceStatus.FAILED)) {
                if (isDeleteOrMerge) {
                    viewHolderItem.chkSelectInvoice.visibility = View.VISIBLE
                } else {
                    viewHolderItem.chkSelectInvoice.visibility = View.GONE
                }
                viewHolderItem.imgSwipeIndicator.visibility = View.VISIBLE
            } else {
                viewHolderItem.chkSelectInvoice.visibility = View.GONE
                viewHolderItem.imgSwipeIndicator.visibility = View.INVISIBLE
            }
            if ((item.status == InQueueForUploadDataModel.InvoiceStatus.IN_QUEUE)) {
                viewHolderItem.txtInvoiceStatus.setText(R.string.txt_waiting_to_upload)
                viewHolderItem.btnRetry.visibility = View.GONE
                viewHolderItem.txtUploadFailedIndicator.visibility = View.GONE
                viewHolderItem.txtInvoiceStatus.setTextColor(context.resources.getColor(R.color.grey_medium))
                viewHolderItem.progressbarInvoiceUploading.visibility = View.GONE
            } else if ((item.status == InQueueForUploadDataModel.InvoiceStatus.FAILED)) {
                viewHolderItem.txtInvoiceStatus.setText(R.string.txt_failed_to_upload)
                viewHolderItem.btnRetry.visibility = View.VISIBLE
                viewHolderItem.txtUploadFailedIndicator.visibility = View.VISIBLE
                viewHolderItem.txtInvoiceStatus.setTextColor(context.resources.getColor(R.color.pinky_red))
                viewHolderItem.progressbarInvoiceUploading.visibility = View.GONE
            } else if ((item.status == InQueueForUploadDataModel.InvoiceStatus.INVOICE_UPLOADING)) {
                viewHolderItem.txtInvoiceStatus.setText(R.string.txt_uploading)
                viewHolderItem.btnRetry.visibility = View.GONE
                viewHolderItem.txtUploadFailedIndicator.visibility = View.GONE
                viewHolderItem.txtInvoiceStatus.setTextColor(context.resources.getColor(R.color.text_blue))
                viewHolderItem.progressbarInvoiceUploading.visibility = View.VISIBLE
            } else {
                viewHolderItem.btnRetry.visibility = View.GONE
                viewHolderItem.txtUploadFailedIndicator.visibility = View.GONE
                viewHolderItem.txtInvoiceStatus.text = ""
                viewHolderItem.progressbarInvoiceUploading.visibility = View.GONE
            }
            val imageFilePath =
                item.imageFilePath!!.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            val imageName = imageFilePath[imageFilePath.size - 1]
            viewHolderItem.txtInvoiceName.text = imageName
            Picasso.get().load(File(item.imageFilePath)).resize(
                ViewHolderInQueue.PLACE_HOLDER_INVOICE_IMAGE_WIDTH,
                ViewHolderInQueue.PLACE_HOLDER_INVOICE_IMAGE_HEIGHT
            ).into(
                viewHolderItem.imgInvoice
            )
            //viewHolderItem.txtInvoiceName.setVisibility(View.GONE);
            viewHolderItem.txtTimeUploaded.visibility = View.GONE
            if (item.isInvoiceSelectedForDeleteOrMerge && isDeleteOrMerge) {
                viewHolderItem.chkSelectInvoice.isChecked = true
            } else {
                viewHolderItem.chkSelectInvoice.isChecked = false
            }
            //added for PNF-2501
            viewHolderItem.itemView.setOnLongClickListener(object : OnLongClickListener {
                override fun onLongClick(v: View): Boolean {
                    if (lstInQueueAndUploadedList[position].inQueueForUploadInvoice != null) {
                        lstInQueueAndUploadedList[position].inQueueForUploadInvoice!!.isInvoiceSelectedForDeleteOrMerge =
                            true
                        invoicesSelectedForMergeDelete.add(lstInQueueAndUploadedList[position])
                        viewHolderItem.chkSelectInvoice.isChecked = true
                    }
                    setDeleteMergeActiveInactive(true)
                    listener.onListItemLongPressed()
                    return true
                }
            })
            viewHolderItem.chkSelectInvoice.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    if (viewHolderItem.chkSelectInvoice.isChecked) {
                        if (invoicesSelectedForMergeDelete.size == 0) {
                            if (lstInQueueAndUploadedList[position].inQueueForUploadInvoice != null) {
                                lstInQueueAndUploadedList[position].inQueueForUploadInvoice!!.isInvoiceSelectedForDeleteOrMerge =
                                    true
                                invoicesSelectedForMergeDelete.add(lstInQueueAndUploadedList[position])
                                viewHolderItem.chkSelectInvoice.isChecked = true
                            }
                        } else {
                            var hasInvoiceManagerData = false
                            for (i in invoicesSelectedForMergeDelete.indices) {
                                if (invoicesSelectedForMergeDelete[i].uploadedInvoice != null) {
                                    //((InvoiceMgr) lstInQueueAndUploadedList.get(position)).setInvoiceSelected(false);
                                    hasInvoiceManagerData = true
                                    break
                                }
                            }
                            if (hasInvoiceManagerData) {
                                invoicesSelectedForMergeDelete.clear()
                                for (j in lstInQueueAndUploadedList.indices) {
                                    if (lstInQueueAndUploadedList[j].uploadedInvoice != null) {
                                        lstInQueueAndUploadedList[j].uploadedInvoice!!.isInvoiceSelected =
                                            false
                                    }
                                }
                            }
                            if (lstInQueueAndUploadedList[position].inQueueForUploadInvoice != null) {
                                lstInQueueAndUploadedList[position].inQueueForUploadInvoice!!.isInvoiceSelectedForDeleteOrMerge =
                                    true
                                invoicesSelectedForMergeDelete.add(lstInQueueAndUploadedList[position])
                                viewHolderItem.chkSelectInvoice.isChecked = true
                            }
                        }
                    } else {
                        val deSelectedInvoice: Any = lstInQueueAndUploadedList[position]
                        for (i in invoicesSelectedForMergeDelete.indices) {
                            if (invoicesSelectedForMergeDelete[i].inQueueForUploadInvoice != null && (deSelectedInvoice as InvoiceUploadsListDataMgr).inQueueForUploadInvoice != null) {
                                if ((invoicesSelectedForMergeDelete[i].inQueueForUploadInvoice!!.imageFilePath == deSelectedInvoice.inQueueForUploadInvoice!!.imageFilePath)) {
                                    lstInQueueAndUploadedList[position].inQueueForUploadInvoice!!.isInvoiceSelectedForDeleteOrMerge =
                                        false
                                    invoicesSelectedForMergeDelete.removeAt(i)
                                }
                            }
                        }
                        viewHolderItem.chkSelectInvoice.isChecked = false
                    }
                    listener.getSelectedInvoicesDeleteOrMerge(invoicesSelectedForMergeDelete)
                    notifyDataSetChanged()
                }
            })
            viewHolderItem.btnRetry.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    val lstInQueue: MutableList<InQueueForUploadDataModel?> = ArrayList()
                    lstInQueue.add(lstInQueueAndUploadedList[position].inQueueForUploadInvoice)
                    //Add to the request queue
                    listener.retryUploadOfFailedInvoices(lstInQueue, position)
                    /*lstInQueueAndUploadedList.remove(position);
                    notifyDataChanged(lstInQueueAndUploadedList,true,false,false,position);*/
                }
            })
        } else if (lstInQueueAndUploadedList[position].uploadedInvoice != null) {
            val item = lstInQueueAndUploadedList[position].uploadedInvoice
            val viewHolderItem = holder as ViewHolderUploaded?
            // time created
//          Date dateCreatedDate = new Date(item.getTimeCreated() * 1000);
            if (item!!.daysElapsed != null) {
                if (item.daysElapsed == 0) {
                    viewHolderItem!!.txtTimeUploaded.text = DateHelper.getDateInDateMonthFormat(
                        item.timeCreated
                    ) + " (" + context.getString(R.string.txt_today) + ") "
                } else if (item.daysElapsed == 1) {
                    viewHolderItem!!.txtTimeUploaded.text = DateHelper.getDateInDateMonthFormat(
                        item.timeCreated
                    ) + " (" + context.getString(R.string.txt_yesterday) + ") "
                } else {
                    viewHolderItem!!.txtTimeUploaded.text = DateHelper.getDateInDateMonthFormat(
                        item.timeCreated
                    ) + " (" + item.daysElapsed.toString() + " " + context.getString(R.string.txt_working_days_ago) + ") "
                }
            } else {
                viewHolderItem!!.txtTimeUploaded.text =
                    DateHelper.getDateInDateMonthFormat(item.timeCreated)
            }

//            Date todaysDate = new Date();
//            Map<TimeUnit, Long> differenceMap = DateHelper.computeDateDiff(dateCreatedDate, todaysDate);
//            if (differenceMap.get(TimeUnit.DAYS) == 0) {
//                if (differenceMap.get(TimeUnit.HOURS) == 0) {
//                    String updatedTimeText = differenceMap.get(TimeUnit.MINUTES) + " " + context.getResources().getString(R.string.txt_minutes_ago);
//                    viewHolderItem.txtTimeUploaded.setText(updatedTimeText);
//                } else {
//                    String updatedTimeText = differenceMap.get(TimeUnit.HOURS) + " " + context.getResources().getString(R.string.txt_hours_ago);
//                    viewHolderItem.txtTimeUploaded.setText(updatedTimeText);
//                }
//            } else {
//                String updatedTimeText = differenceMap.get(TimeUnit.DAYS) + " " + context.getResources().getString(R.string.txt_days_ago);
//                viewHolderItem.txtTimeUploaded.setText(updatedTimeText);
//            }
            if (isDeleteOrMerge) {
                ////PNF-340 created by user check
                if (item.isStatus(Invoice.Status.PENDING) || (item.isStatus(Invoice.Status.REJECTED))) {
                    viewHolderItem.chkSelectInvoice.visibility = View.VISIBLE
                } else {
                    viewHolderItem.chkSelectInvoice.visibility = View.GONE
                }
            } else {
                viewHolderItem.chkSelectInvoice.visibility = View.GONE
            }
            if (item.isStatus(Invoice.Status.PENDING)) {
                viewHolderItem.imgSwipeIndicator.visibility = View.VISIBLE
            } else {
                viewHolderItem.imgSwipeIndicator.visibility = View.INVISIBLE
            }
            if (item.isStatus(Invoice.Status.PROCESSING)) {
                viewHolderItem.txtInvoiceStatus.text =
                    context.getString(R.string.txt_processing_lower_case)
                viewHolderItem.txtUploadFailedIndicator.visibility = View.GONE
                viewHolderItem.txtInvoiceStatus.setTextColor(context.resources.getColor(R.color.green))
            } else if (item.isStatus(Invoice.Status.REJECTED)) {
                viewHolderItem.txtInvoiceStatus.text =
                    context.getString(R.string.txt_invoice_rejected)
                viewHolderItem.txtUploadFailedIndicator.visibility = View.VISIBLE
                viewHolderItem.txtInvoiceStatus.setTextColor(context.resources.getColor(R.color.pinky_red))
            } else {
                viewHolderItem.txtInvoiceStatus.text = ""
                viewHolderItem.txtUploadFailedIndicator.visibility = View.GONE
            }
            var count = 0
            for (imagesModel: ImagesModel in item.images!!) {
                if (imagesModel.imageFileNames != null && imagesModel.imageFileNames.size > 0) count += imagesModel.imageFileNames.size
            }
            if (count > 1) {
                viewHolderItem.txtInvoiceMergedAtTime.visibility = View.GONE
                //calculate the merged time
                val mergedTime = DateHelper.getDateInDateMonthHourMinuteFormat((item.timeCreated)!!)
                viewHolderItem.txtInvoiceMergedAtTime.text =
                    "(" + context.getString(R.string.txt_merged_on) + " " + mergedTime + ")"
            } else {
                viewHolderItem.txtInvoiceMergedAtTime.visibility = View.GONE
            }
            viewHolderItem.imgInvoice.setImageResource(R.drawable.placeholder_all)
            viewHolderItem.imgInvoice.setImageResource(R.drawable.placeholder_all)
            if (item.supplier != null && !StringHelper.isStringNullOrEmpty(item.supplier!!.supplierName)) {
                viewHolderItem.txtInvoiceName.text = item.supplier!!.supplierName
            } else {
                if (count > 1) {
                    val files =
                        String.format(context.getString(R.string.format_value_files), "" + count)
                    viewHolderItem.txtInvoiceName.text =
                        context.resources.getString(R.string.txt_unknown_supplier) + " (" + files + ")"
                } else {
                    viewHolderItem.txtInvoiceName.text =
                        context.resources.getString(R.string.txt_unknown_supplier)
                }
            }
            if (count > 0) {
                for (i in item.images!!.indices) {
                    try {
                        if (!item.images!![i].imageURL!!.isEmpty() && (item.images!![i].imageFileNames != null) && (item.images!![i].imageFileNames.size > 0)) {
                            if (count == 1) {
                                viewHolderItem.txtInvoiceMergedAtTime.visibility = View.GONE
                            }
                            val url = item.images!![i].imageURL + item.images!![i].imageFileNames[0]
                            if (!StringHelper.isStringNullOrEmpty(url)) Picasso.get().load(url)
                                .placeholder(
                                    R.drawable.placeholder_all
                                ).resize(
                                    ViewHolderUploaded.PLACE_HOLDER_INVOICE_IMAGE_WIDTH,
                                    ViewHolderUploaded.PLACE_HOLDER_INVOICE_IMAGE_HEIGHT
                                ).into(
                                    viewHolderItem.imgInvoice
                                )
                            break
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            if (item.isInvoiceSelected && isDeleteOrMerge) {
                viewHolderItem.chkSelectInvoice.isChecked = true
            } else {
                viewHolderItem.chkSelectInvoice.isChecked = false
            }
            //added for PNF-2501
            viewHolderItem.itemView.setOnLongClickListener(object : OnLongClickListener {
                override fun onLongClick(v: View): Boolean {
                    //long press should be enable for all the invoices except for the processing ones
                    if (lstInQueueAndUploadedList[position].uploadedInvoice != null &&
                        !(lstInQueueAndUploadedList[position].uploadedInvoice!!.isStatus(Invoice.Status.PROCESSING))
                    ) {
                        if (lstInQueueAndUploadedList[position].uploadedInvoice != null) {
                            lstInQueueAndUploadedList[position].uploadedInvoice!!.isInvoiceSelected =
                                true
                            invoicesSelectedForMergeDelete.add(lstInQueueAndUploadedList[position])
                        }
                        setDeleteMergeActiveInactive(true)
                        listener.onListItemLongPressed()
                    }
                    return true
                }
            })
            viewHolderItem.chkSelectInvoice.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    if (viewHolderItem.chkSelectInvoice.isChecked) {
                        if (invoicesSelectedForMergeDelete.size == 0) {
                            lstInQueueAndUploadedList[position].uploadedInvoice!!.isInvoiceSelected =
                                true
                            invoicesSelectedForMergeDelete.add(lstInQueueAndUploadedList[position])
                        } else {
                            for (i in invoicesSelectedForMergeDelete.indices) {
                                if (invoicesSelectedForMergeDelete[i].inQueueForUploadInvoice != null) {
                                    //set all the in queue as deselected
                                    for (j in lstInQueueAndUploadedList.indices) {
                                        if (lstInQueueAndUploadedList[j].inQueueForUploadInvoice != null) {
                                            lstInQueueAndUploadedList[j].inQueueForUploadInvoice?.isInvoiceSelectedForDeleteOrMerge =
                                                false
                                        }
                                    }
                                    invoicesSelectedForMergeDelete.clear()
                                }
                            }
                            if (lstInQueueAndUploadedList[position].uploadedInvoice != null) {
                                lstInQueueAndUploadedList[position].uploadedInvoice!!.isInvoiceSelected =
                                    true
                                invoicesSelectedForMergeDelete.add(lstInQueueAndUploadedList[position])
                            }
                        }
                    } else {
                        val deSelectedInvoice = lstInQueueAndUploadedList[position]
                        for (i in invoicesSelectedForMergeDelete.indices) {
                            if (invoicesSelectedForMergeDelete[i].uploadedInvoice != null && deSelectedInvoice.uploadedInvoice != null) {
                                if ((invoicesSelectedForMergeDelete[i].uploadedInvoice!!.invoiceId == deSelectedInvoice.uploadedInvoice!!.invoiceId)) {
                                    lstInQueueAndUploadedList[position].uploadedInvoice!!.isInvoiceSelected =
                                        false
                                    invoicesSelectedForMergeDelete.removeAt(i)
                                }
                            }
                        }
                    }
                    listener.getSelectedInvoicesDeleteOrMerge(invoicesSelectedForMergeDelete)
                    notifyDataSetChanged()
                }
            })
            viewHolderItem.itemView.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    AnalyticsHelper.logAction(
                        context,
                        AnalyticsHelper.TAP_ITEM_UPLOADS_INVOICES_INVOICE
                    )
                    createImageFullscreenDialog(item)
                }
            })
        }
    }

    override fun getItemCount(): Int {
        return lstInQueueAndUploadedList.size
    }

    override fun getItemViewType(position: Int): Int {
        if (lstInQueueAndUploadedList[position].header != null) {
            return VIEW_TYPE_HEADING
        } else if (lstInQueueAndUploadedList[position].inQueueForUploadInvoice != null) {
            return VIEW_TYPE_IN_QUEUE
        } else if (lstInQueueAndUploadedList[position].inQueueForUploadMergedInvoice != null) {
            return VIEW_TYPE_IN_QUEUE
        } else return if (lstInQueueAndUploadedList.get(position).uploadedInvoice != null) {
            VIEW_TYPE_UPLOADED
        } else {
            0
        }
    }

    class ViewHolderUploaded(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgInvoice: ImageView
        var txtInvoiceName: TextView
        var txtTimeUploaded: TextView
        var imgSwipeIndicator: ImageView
        var txtInvoiceStatus: TextView
        var chkSelectInvoice: CheckBox
        var txtInvoiceMergedAtTime: TextView
        var txtUploadFailedIndicator: TextView

        init {
            imgInvoice = itemView.findViewById(R.id.upload_tab_invoice_image)
            txtInvoiceName = itemView.findViewById(R.id.txt_image_name)
            txtTimeUploaded = itemView.findViewById(R.id.txt_time_uploaded)
            imgSwipeIndicator = itemView.findViewById(R.id.img_swipe_indicator)
            txtInvoiceStatus = itemView.findViewById(R.id.txt_invoice_status)
            txtUploadFailedIndicator = itemView.findViewById(R.id.upload_failed_indicator)
            chkSelectInvoice = itemView.findViewById(R.id.checkBox_select_uploaded_invoice)
            txtInvoiceMergedAtTime = itemView.findViewById(R.id.txt_invoice_merged_at)
            setTypefaceView(txtInvoiceName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            setTypefaceView(txtTimeUploaded, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_ITALICS)
            setTypefaceView(txtInvoiceStatus, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            setTypefaceView(
                txtInvoiceMergedAtTime,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }

        companion object {
            val PLACE_HOLDER_INVOICE_IMAGE_WIDTH = CommonMethods.dpToPx(36)
            val PLACE_HOLDER_INVOICE_IMAGE_HEIGHT = CommonMethods.dpToPx(36)
        }
    }

    class ViewHolderInQueue(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgInvoice: ImageView
        var txtInvoiceName: TextView
        var txtTimeUploaded: TextView
        var imgSwipeIndicator: ImageView
        var txtInvoiceStatus: TextView
        var chkSelectInvoice: CheckBox
        var progressbarInvoiceUploading: ProgressBar
        var txtUploadFailedIndicator: TextView
        var btnRetry: ImageButton
        var inQueueRow: ConstraintLayout

        init {
            imgInvoice = itemView.findViewById(R.id.upload_tab_invoice_image)
            txtInvoiceName = itemView.findViewById(R.id.txt_image_name)
            txtTimeUploaded = itemView.findViewById(R.id.txt_time_uploaded)
            imgSwipeIndicator = itemView.findViewById(R.id.img_swipe_indicator)
            txtInvoiceStatus = itemView.findViewById(R.id.txt_invoice_status)
            progressbarInvoiceUploading = itemView.findViewById(R.id.progressBar_uploading_invoice)
            txtUploadFailedIndicator = itemView.findViewById(R.id.upload_failed_indicator)
            btnRetry = itemView.findViewById(R.id.img_retry_upload)
            chkSelectInvoice = itemView.findViewById(R.id.checkBox_select_in_queue_invoice)
            inQueueRow = itemView.findViewById(R.id.lyt_in_queue_row)
            setTypefaceView(txtInvoiceName, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            setTypefaceView(txtTimeUploaded, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
            setTypefaceView(txtInvoiceStatus, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        }

        companion object {
            val PLACE_HOLDER_INVOICE_IMAGE_WIDTH = CommonMethods.dpToPx(36)
            val PLACE_HOLDER_INVOICE_IMAGE_HEIGHT = CommonMethods.dpToPx(36)
        }
    }

    inner class ViewHolderHeading(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtHeading: TextView
        var btnMergeOrDelete: TextView
        val SWIPE_BUTTON_WIDTH = CommonMethods.dpToPx(70)
        val SWIPE_BUTTON_TEXT_SIZE = CommonMethods.dpToPx(14)
        val SWIPE_BUTTON_PADDING_BETWEEN_TEXT_IMAGE = CommonMethods.dpToPx(11)

        init {
            txtHeading = itemView.findViewById(R.id.txt_heading_data)
            btnMergeOrDelete = itemView.findViewById(R.id.txt_merge_delete)
            setTypefaceView(txtHeading, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD)
            setTypefaceView(btnMergeOrDelete, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        }
    }

    fun notifyDataChanged(
        invoiceUploadTabList: MutableList<InvoiceUploadsListDataMgr>,
        isRemoved: Boolean,
        isNewUpload: Boolean,
        isItemStatuschanged: Boolean,
        position: Int
    ) {
        lstInQueueAndUploadedList = invoiceUploadTabList
        if (isRemoved) {
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, lstInQueueAndUploadedList.size)
        } else if (isNewUpload) {
            notifyItemInserted(position)
            notifyItemRangeChanged(position, lstInQueueAndUploadedList.size)
        } else if (isItemStatuschanged) {
            notifyItemChanged(position)
        } else {
            notifyDataSetChanged()
        }
    }

    fun setDeleteMergeActiveInactive(isActive: Boolean) {
        if (isActive) {
            isDeleteOrMerge = true
        } else {
            isDeleteOrMerge = false
        }
        notifyDataSetChanged()
    }

    private fun createImageFullscreenDialog(invoiceMgr: InvoiceMgr?) {
        val dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_image_fullscreen)
        dialog.window!!.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val viewPager = dialog.findViewById<ViewPager>(R.id.imageView)
        viewPager.offscreenPageLimit = 0
        viewPager.adapter =
            InvoiceImagesSliderAdapter(context, (invoiceMgr)!!, object : InvoiceImageListener {
                override fun invoiceUpdated(inv: Invoice?) {
                    for (i in lstInQueueAndUploadedList.indices) {
                        if (lstInQueueAndUploadedList.get(i).uploadedInvoice != null) {
                            if ((lstInQueueAndUploadedList.get(i).uploadedInvoice!!.invoiceId == inv!!.invoiceId)) {
                                val updatedInvoice: InvoiceUploadsListDataMgr =
                                    InvoiceUploadsListDataMgr()
                                val invoiceMgr: InvoiceMgr =
                                    ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                                        ZeemartBuyerApp.gsonExposeExclusive.toJson(inv),
                                        InvoiceMgr::class.java
                                    )
                                updatedInvoice.uploadedInvoice = invoiceMgr
                                lstInQueueAndUploadedList.set(i, updatedInvoice)
                                notifyDataChanged(
                                    lstInQueueAndUploadedList, false, false, true, i
                                )
                                return
                            }
                        }
                    }
                }

                override fun invoiceDeleted(inv: Invoice?, isDeleted: Boolean) {
                    for (i in lstInQueueAndUploadedList.indices) {
                        if (lstInQueueAndUploadedList.get(i).uploadedInvoice != null) {
                            if ((lstInQueueAndUploadedList.get(i).uploadedInvoice!!.invoiceId == inv!!.invoiceId)) {
                                dialog.dismiss()
                                removeAt(i)
                                return
                            }
                        }
                    }
                }

                override fun onRejectedInvoiceBackPressed() {
                    //no action required from list of uploads screen
                }
            })
        dialog.show()
    }

    fun removeAt(position: Int) {
        if (lstInQueueAndUploadedList[position].uploadedInvoice != null) {
            val json = createDeleteJsonForInvoice(
                lstInQueueAndUploadedList[position].uploadedInvoice!!.invoiceId
            )
            val outlet: MutableList<Outlet?> = ArrayList()
            outlet.add(SharedPref.defaultOutlet)
            deleteUnprocessedInvoice(context, outlet as List<Outlet>?, object : GetRequestStatusResponseListener {
                override fun onSuccessResponse(status: String?) {
                    listener.invoicesCountByStatus()
                }

                override fun onErrorResponse(error: VolleyErrorHelper?) {
                    getToastRed(context.getString(R.string.txt_delete_draft_fail))
                }
            }, json)
            lstInQueueAndUploadedList.removeAt(position)
            notifyDataChanged(lstInQueueAndUploadedList, true, false, false, position)
        }
    }

    companion object {
        private val VIEW_TYPE_HEADING = 1
        private val VIEW_TYPE_IN_QUEUE = 2
        private val VIEW_TYPE_UPLOADED = 3
    }
}