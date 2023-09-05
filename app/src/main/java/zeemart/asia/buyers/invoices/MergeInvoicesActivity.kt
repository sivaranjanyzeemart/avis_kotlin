package zeemart.asia.buyers.invoices

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.StringHelper

class MergeInvoicesActivity : AppCompatActivity() {
    private var txtCancel: TextView? = null
    private var txtInvoiceImageNoIndicator: TextView? = null
    private var lstPhotos: RecyclerView? = null
    private var imageUriRotationList: ArrayList<InQueueForUploadDataModel>? = null
    private var selectedImagesToMerged = ArrayList<InQueueForUploadDataModel>()
    private var btnSubmit: RelativeLayout? = null
    private var txtUpload: TextView? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_merge_invoices)
        lstPhotos = findViewById<RecyclerView>(R.id.lst_photos)
        lstPhotos!!.setNestedScrollingEnabled(false)
        lstPhotos!!.setLayoutManager(GridLayoutManager(this, 2))
        txtCancel = findViewById<TextView>(R.id.txt_cancel)
        ZeemartBuyerApp.setTypefaceView(
            txtCancel,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        txtCancel!!.setOnClickListener(View.OnClickListener {
            val newIntent = Intent()
            val invoiceImageListJson: String =
                ZeemartBuyerApp.gsonExposeExclusive.toJson(imageUriRotationList)
            newIntent.putExtra(ZeemartAppConstants.INVOICE_IMAGE_URI, invoiceImageListJson)
            setResult(ZeemartAppConstants.ResultCode.RESULT_MERGED, newIntent)
            finish()
        })
        txtInvoiceImageNoIndicator = findViewById<TextView>(R.id.txt_invoice_image_no_indicator)
        ZeemartBuyerApp.setTypefaceView(
            txtInvoiceImageNoIndicator,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        if (getIntent().getExtras() != null) {
            val uriListString: String? =
                getIntent().getExtras()?.getString(ZeemartAppConstants.INVOICE_IMAGE_URI)
            imageUriRotationList =
                ZeemartBuyerApp.gsonExposeExclusive.fromJson<ArrayList<InQueueForUploadDataModel>>(
                    uriListString,
                    object : TypeToken<ArrayList<InQueueForUploadDataModel?>?>() {}.type
                )
            selectedImagesToMerged = ArrayList()
            for (i in imageUriRotationList!!.indices) {
                if (imageUriRotationList!![i].isMerged) {
                    selectedImagesToMerged.add(imageUriRotationList!![i])
                }
            }
        }
        setAdapter()
        btnSubmit = findViewById<RelativeLayout>(R.id.lyt_review_invoice_bottom_bar)
        btnSubmit!!.setOnClickListener(View.OnClickListener {
            for (i in selectedImagesToMerged.indices) {
                selectedImagesToMerged[i].isMerged = true
            }
            val UpdateInvoice = ArrayList(selectedImagesToMerged)
            for (i in imageUriRotationList!!.indices) {
                if (!imageUriRotationList!![i].isInvoiceSelectedForDeleteOrMerge) {
                    if (!UpdateInvoice.contains(imageUriRotationList!![i])) UpdateInvoice.add(
                        imageUriRotationList!![i]
                    )
                }
            }
            AnalyticsHelper.logGuestAction(
                this@MergeInvoicesActivity,
                AnalyticsHelper.TAP_MERGE_INVOICES
            )
            val newIntent = Intent()
            val invoiceImageListJson: String =
                ZeemartBuyerApp.gsonExposeExclusive.toJson(UpdateInvoice)
            newIntent.putExtra(
                ZeemartAppConstants.CALLED_FROM,
                ZeemartAppConstants.CALLED_FROM_REVIEW_INVOICE_IMAGE_ACTIVITY
            )
            newIntent.putExtra(ZeemartAppConstants.INVOICE_IMAGE_URI, invoiceImageListJson)
            setResult(ZeemartAppConstants.ResultCode.RESULT_MERGED, newIntent)
            finish()
        })
        txtUpload = findViewById<TextView>(R.id.txt_upload)
        txtUpload!!.setText(
            String.format(
                getResources().getString(R.string.txt_select_atleast_two_files),
                2
            )
        )
        ZeemartBuyerApp.setTypefaceView(
            txtUpload,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        btnSubmit!!.setClickable(false)
    }

    private fun setAdapter() {
        val UpdateInvoice = ArrayList<InQueueForUploadDataModel>()
        val mergedInvoices: MutableList<InQueueForUploadDataModel> = ArrayList()
        for (i in imageUriRotationList!!.indices) {
            if (imageUriRotationList!![i].isMerged) {
                mergedInvoices.add(imageUriRotationList!![i])
            } else {
                UpdateInvoice.add(imageUriRotationList!![i])
            }
        }
        if (mergedInvoices.size > 0) {
            UpdateInvoice.add(0, mergedInvoices[0])
            UpdateInvoice[0].imagesCount = mergedInvoices.size
        } else {
            UpdateInvoice[0].imagesCount = 0
        }
        lstPhotos?.setAdapter(
            ReviewInvoiceAdapter(
                this@MergeInvoicesActivity,
                true,
                UpdateInvoice,
                object : ReviewInvoiceAdapter.OnInvoiceImageClick {
                    override fun onImageClicked(inQueueForUploadDataModel: InQueueForUploadDataModel?) {
                        val newIntent =
                            Intent(this@MergeInvoicesActivity, InvoicePreReviewPage::class.java)
                        val imageUriRotationLists = ArrayList<InQueueForUploadDataModel>()
                        if (inQueueForUploadDataModel?.isMerged!!) {
                            for (i in imageUriRotationList!!.indices) {
                                if (imageUriRotationList!![i].isMerged) {
                                    imageUriRotationLists.add(imageUriRotationList!![i])
                                }
                            }
                        } else {
                            imageUriRotationLists.add(inQueueForUploadDataModel)
                        }
                        val invoiceImageListJson: String =
                            ZeemartBuyerApp.gsonExposeExclusive.toJson(imageUriRotationLists)
                        newIntent.putExtra(
                            ZeemartAppConstants.INVOICE_IMAGE_URI,
                            invoiceImageListJson
                        )
                        newIntent.putExtra(
                            ZeemartAppConstants.ALL_SELECTED_INVOICES,
                            ZeemartBuyerApp.gsonExposeExclusive.toJson(imageUriRotationList)
                        )
                        startActivityForResult(
                            newIntent,
                            ZeemartAppConstants.RequestCode.CameraPreviewActivityAdjustImage
                        )
                    }

                    override fun onFilterSelected(inQueueForUploadDataModel: InQueueForUploadDataModel?) {
                        UpdateSubmitScreen()
                        for (i in selectedImagesToMerged.indices) {
                            if (selectedImagesToMerged[i].imageFilePath == inQueueForUploadDataModel?.imageFilePath && selectedImagesToMerged[i].imageDirectoryPath == inQueueForUploadDataModel?.imageDirectoryPath) {
                                selectedImagesToMerged[i].isInvoiceSelectedForDeleteOrMerge = true
                            }
                        }
                        if (!selectedImagesToMerged.contains(inQueueForUploadDataModel)) {
                            selectedImagesToMerged.add(inQueueForUploadDataModel!!)
                        }
                    }

                    override fun onFilterDeselected(inQueueForUploadDataModel: InQueueForUploadDataModel?) {
                        for (i in imageUriRotationList!!.indices) {
                            if (imageUriRotationList!![i].isInvoiceSelectedForDeleteOrMerge && imageUriRotationList!![i].countOfMerged != 0 && imageUriRotationList!![i].countOfMerged > inQueueForUploadDataModel?.countOfMerged!!) {
                                imageUriRotationList!![i].countOfMerged =
                                    imageUriRotationList!![i].countOfMerged - 1
                            }
                        }
                        if (lstPhotos!!.getAdapter() != null) {
                            lstPhotos!!.getAdapter()?.notifyDataSetChanged()
                        }
                        for (i in selectedImagesToMerged.indices) {
                            if (selectedImagesToMerged[i].imageFilePath == inQueueForUploadDataModel?.imageFilePath && selectedImagesToMerged[i].imageDirectoryPath == inQueueForUploadDataModel?.imageDirectoryPath) {
                                selectedImagesToMerged.remove(selectedImagesToMerged[i])
                            }
                        }
                        UpdateSubmitScreen()
                    }
                })
        )
    }

    protected override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == ZeemartAppConstants.ResultCode.RESULT_MERGED) {
            if (data?.getExtras() != null) {
                val uriListString: String? =
                    data!!.getExtras()?.getString(ZeemartAppConstants.INVOICE_IMAGE_URI)
                var invoiceImageDataList = ArrayList<InQueueForUploadDataModel>()
                if (!StringHelper.isStringNullOrEmpty(uriListString)) {
                    invoiceImageDataList =
                        ZeemartBuyerApp.gsonExposeExclusive.fromJson<ArrayList<InQueueForUploadDataModel>>(
                            uriListString,
                            object : TypeToken<ArrayList<InQueueForUploadDataModel?>?>() {}.type
                        )
                }
                Log.e("came here", "onActivityResult: " + invoiceImageDataList.size)
                imageUriRotationList = ArrayList()
                imageUriRotationList!!.addAll(invoiceImageDataList)
                if (imageUriRotationList!!.size == 0) {
                    val newIntent = Intent()
                    setResult(ZeemartAppConstants.ResultCode.RESULT_MERGED, newIntent)
                    val invoiceImageListJson: String =
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(imageUriRotationList)
                    newIntent.putExtra(ZeemartAppConstants.INVOICE_IMAGE_URI, invoiceImageListJson)
                    finish()
                } else {
                    setAdapter()
                    UpdateSubmitScreen()
                }
                //
            }
        }
        if (resultCode == ZeemartAppConstants.ResultCode.RESULT_DELETED) {
            if (data?.getExtras() != null) {
                val uriListString: String? =
                    data.getExtras()!!.getString(ZeemartAppConstants.INVOICE_IMAGE_URI)
                var invoiceImageDataList = ArrayList<InQueueForUploadDataModel>()
                if (!StringHelper.isStringNullOrEmpty(uriListString)) {
                    invoiceImageDataList =
                        ZeemartBuyerApp.gsonExposeExclusive.fromJson<ArrayList<InQueueForUploadDataModel>>(
                            uriListString,
                            object : TypeToken<ArrayList<InQueueForUploadDataModel?>?>() {}.type
                        )
                }
                if (imageUriRotationList!!.size != invoiceImageDataList.size) {
                    imageUriRotationList = ArrayList()
                    imageUriRotationList!!.addAll(invoiceImageDataList)
                    for (i in imageUriRotationList!!.indices) {
                        imageUriRotationList!![i].isInvoiceSelectedForDeleteOrMerge = false
                    }
                }
                if (imageUriRotationList!!.size == 0) {
                    val newIntent = Intent()
                    setResult(ZeemartAppConstants.ResultCode.RESULT_MERGED, newIntent)
                    val invoiceImageListJson: String =
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(imageUriRotationList)
                    newIntent.putExtra(ZeemartAppConstants.INVOICE_IMAGE_URI, invoiceImageListJson)
                    finish()
                } else {
                    setAdapter()
                    UpdateSubmitScreen()
                }
                //
            }
        }
    }

    private fun UpdateSubmitScreen() {
        var counter = 0
        for (i in imageUriRotationList!!.indices) {
            if (imageUriRotationList!![i].isInvoiceSelectedForDeleteOrMerge) {
                counter++
            }
        }
        if (counter < 2) {
            txtUpload?.setText(
                String.format(
                    getResources().getString(R.string.txt_select_atleast_two_files),
                    2
                )
            )
            btnSubmit?.setClickable(false)
            btnSubmit?.setBackground(getResources().getDrawable(R.drawable.btn_rounded_dark_grey))
        } else {
            txtUpload?.setText(
                String.format(
                    getResources().getString(R.string.txt_merge_selected),
                    counter
                )
            )
            btnSubmit?.setClickable(true)
            btnSubmit?.setBackground(getResources().getDrawable(R.drawable.btn_rounded_blue))
        }
    }

    override fun onBackPressed() {
        val newIntent = Intent()
        val invoiceImageListJson: String =
            ZeemartBuyerApp.gsonExposeExclusive.toJson(imageUriRotationList)
        newIntent.putExtra(ZeemartAppConstants.INVOICE_IMAGE_URI, invoiceImageListJson)
        setResult(ZeemartAppConstants.ResultCode.RESULT_MERGED, newIntent)
        finish()
    }
}