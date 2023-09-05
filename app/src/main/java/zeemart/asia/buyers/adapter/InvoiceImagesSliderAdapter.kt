package zeemart.asia.buyers.adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.http.SslError
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.webkit.SslErrorHandler
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.viewpager.widget.PagerAdapter
import com.android.volley.Request
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.Target
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastGreen
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.getToastRed
import zeemart.asia.buyers.activities.BaseNavigationActivity
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.FileHelper.getBitmapUri
import zeemart.asia.buyers.helper.FileHelper.savePdfToDocuments
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.PermissionCallback
import zeemart.asia.buyers.invoices.InvoiceCameraPreview
import zeemart.asia.buyers.invoices.InvoiceDetailActivity
import zeemart.asia.buyers.invoices.RejectedInvoiceActivity
import zeemart.asia.buyers.models.ImagesModel
import zeemart.asia.buyers.models.invoice.Invoice
import zeemart.asia.buyers.models.invoice.InvoiceMgr
import zeemart.asia.buyers.network.InputStreamVolleyRequest
import zeemart.asia.buyers.network.InvoiceHelper.EditInvoice
import zeemart.asia.buyers.network.VolleyRequest.Companion.getInstance
import zeemart.asia.buyers.orders.OrderDetailsActivity

/**
 * Created by ParulBhandari on 2/1/2018.
 * ImageSliderAdapter is used to display Invoice Images
 * called from
 * [RejectedInvoiceActivity]
 * [InvoiceDetailActivity]
 * [zeemart.asia.buyers.invoices.InvoiceInQueueUploadListAdapter]
 */
class InvoiceImagesSliderAdapter(
    private val context: Context, //private InvoiceMgr invoiceMgr;
    private var invoice: Invoice, private val invoiceImageListener: InvoiceImageListener,
) : PagerAdapter() {
    interface InvoiceImageListener {
        fun invoiceUpdated(inv: Invoice?)
        fun invoiceDeleted(inv: Invoice?, isDelete: Boolean)

        // hide RejectedInvoiceActivity when back click Rejected Invoice Notification
        fun onRejectedInvoiceBackPressed()
    }

    private lateinit var imageUrls: List<String>
    private lateinit var progressBar: ProgressBar
    private lateinit var rejectReason: TextView
    private lateinit var txtUpdatedOn: TextView
    private lateinit var sharingIntent: Intent
    private lateinit var image: Target
    private lateinit var lytRejectedReason: RelativeLayout
    private lateinit var lytHideShow: RelativeLayout
    private lateinit var lytUpload: LinearLayout
    private lateinit var txtUpload: TextView

    init {
        if (invoice != null && invoice!!.images != null) imageUrls = invoice!!.invoiceImagesList
    }

    override fun getCount(): Int {
        return if (imageUrls != null) {
            imageUrls!!.size
        } else {
            1
        }
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return (view == `object`)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageLayout: View =
            LayoutInflater.from(context).inflate(R.layout.layout_image_swipe, container, false)
        val webView: WebView = imageLayout.findViewById(R.id.webview_product)
        val more_btn: ImageView = imageLayout.findViewById(R.id.more_btn)
        val pageNumber: TextView = imageLayout.findViewById(R.id.pageNumber)
        val reject: TextView = imageLayout.findViewById(R.id.rejected)
        rejectReason = imageLayout.findViewById(R.id.rejected_reason)
        txtUpdatedOn = imageLayout.findViewById(R.id.txt_updated_on)
        progressBar = imageLayout.findViewById(R.id.progress_bar_image_download)
        txtUpload = imageLayout.findViewById(R.id.txt_upload)
        lytUpload = imageLayout.findViewById(R.id.lyt_upload)
        lytRejectedReason = imageLayout.findViewById(R.id.lyt_rejected)
        lytHideShow = imageLayout.findViewById(R.id.lyt_hide_show)
        lytRejectedReason.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (lytHideShow.getVisibility() == View.VISIBLE) {
                    lytHideShow.setVisibility(View.GONE)
                } else {
                    val animate: TranslateAnimation = TranslateAnimation(
                        0f,  // fromXDelta
                        0f,  // toXDelta
                        lytHideShow.getHeight().toFloat(),  // fromYDelta
                        0f
                    ) // toYDelta
                    animate.duration = 300
                    animate.fillAfter = true
                    lytHideShow.startAnimation(animate)
                    lytHideShow.setVisibility(View.VISIBLE)
                }
            }
        })
        ZeemartBuyerApp.setTypefaceView(
            pageNumber,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            reject,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            rejectReason,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtUpload,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtUpdatedOn,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        if (invoice!!.daysElapsed != null) {
            if (invoice!!.daysElapsed == 0) {
                txtUpdatedOn.setText(
                    context.resources.getString(R.string.txt_uploaded) + " " + DateHelper.getDateInDateMonthFormat(
                        invoice!!.timeCreated
                    ) + " (" + context.getString(R.string.txt_today) + ") "
                )
            } else if (invoice!!.daysElapsed == 1) {
                txtUpdatedOn.setText(
                    context.resources.getString(R.string.txt_uploaded) + " " + DateHelper.getDateInDateMonthFormat(
                        invoice!!.timeCreated
                    ) + " (" + context.getString(R.string.txt_yesterday) + ") "
                )
            } else {
                txtUpdatedOn.setText(
                    context.resources.getString(R.string.txt_uploaded) + " " + DateHelper.getDateInDateMonthFormat(
                        invoice!!.timeCreated
                    ) + " (" + invoice!!.daysElapsed.toString() + " " + context.getString(R.string.txt_working_days_ago) + ") "
                )
            }
        } else {
            if (invoice!!.invoiceDate != null && invoice!!.invoiceDate != 0L) {
                txtUpdatedOn.setText(DateHelper.getDateInDateMonthFormat(invoice!!.invoiceDate))
            } else {
                txtUpdatedOn.setText(DateHelper.getDateInDateMonthFormat(invoice!!.timeCreated))
            }
        }
        val resourceUrl: String = imageUrls!!.get(position)
        if (imageUrls!!.size > 1) {
            pageNumber.text = String.format(
                context.getString(R.string.format_txt_page_number),
                "" + (position + 1),
                "" + imageUrls!!.size
            )
        } else {
            if (invoice!!.supplier != null && !StringHelper.isStringNullOrEmpty(invoice!!.supplier?.supplierName)) {
                pageNumber.text = invoice!!.supplier?.supplierName
            } else {
                pageNumber.text = context.resources.getString(R.string.txt_unknown_supplier)
            }
            //            for (ImagesModel imagesModel : invoice.getImages()) {
//                for (String name : imagesModel.getImageFileNames()) {
//                    if (resourceUrl.equals(imagesModel.getImageURL() + name)) {
//                        pageNumber.setText(name);
//                    }
//                }
//            }
        }
        if (invoice!!.isStatus(Invoice.Status.REJECTED)) {
            lytRejectedReason.setVisibility(View.VISIBLE)
            reject.visibility = View.VISIBLE
            rejectReason.setVisibility(View.VISIBLE)
            rejectReason.setText(invoice!!.rejectReason?.reasonText)
            if ((invoice != null) && (invoice!!.orderIds != null) && !StringHelper.isStringNullOrEmpty(
                    invoice!!.orderIds?.get(0)
                )
            ) {
                lytUpload.setVisibility(View.VISIBLE)
            } else {
                lytUpload.setVisibility(View.GONE)
            }
        } else {
            reject.visibility = View.GONE
            rejectReason.setVisibility(View.GONE)
            lytRejectedReason.setVisibility(View.GONE)
        }
        if (resourceUrl.contains(ServiceConstant.FILE_EXT_PDF)) {
            val settings: WebSettings = webView.settings
            settings.javaScriptEnabled = true
            webView.settings.pluginState = WebSettings.PluginState.ON
            webView.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
            webView.loadUrl(ServiceConstant.ENDPOINT_GOOGLE_DOC_EMBED + resourceUrl)
        } else {
            val settings: WebSettings = webView.settings
            settings.javaScriptEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            webView.webViewClient = object : WebViewClient() {
                override fun onReceivedSslError(
                    view: WebView,
                    handler: SslErrorHandler,
                    error: SslError,
                ) {
                    val builder: AlertDialog.Builder = AlertDialog.Builder(
                        context
                    )
                    var message: String? = context.getString(R.string.ssl_txt_ssl_cert_error_title)
                    when (error.primaryError) {
                        SslError.SSL_UNTRUSTED -> message =
                            context.getString(R.string.ssl_txt_ca_not_trusted)
                        SslError.SSL_EXPIRED -> message =
                            context.getString(R.string.ssl_txt_cert_expired)
                        SslError.SSL_IDMISMATCH -> message =
                            context.getString(R.string.ssl_txt_cert_hostname_mismatch)
                        SslError.SSL_NOTYETVALID -> message =
                            context.getString(R.string.ssl_txt_cert_invalid)
                        else -> {}
                    }
                    message += context.getString(R.string.ssl_txt_do_you_want_to_continue)
                    builder.setTitle(context.getString(R.string.ssl_txt_ssl_cert_error_title))
                    builder.setMessage(message)
                    builder.setPositiveButton(
                        R.string.ssl_txt_continue,
                        object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface, which: Int) {
                                handler.proceed()
                            }
                        })
                    builder.setNegativeButton(
                        R.string.txt_cancel,
                        object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface, which: Int) {
                                handler.cancel()
                            }
                        })
                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                }
            }
            webView.loadUrl(resourceUrl)
        }
        imageLayout.isFocusableInTouchMode = true
        imageLayout.requestFocus()
        imageLayout.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
                if (invoice == null && keyCode == KeyEvent.KEYCODE_BACK) {
                    // hide RejectedInvoiceActivity when back click  - Rejected Invoice Notification
                    invoiceImageListener.onRejectedInvoiceBackPressed()
                    return true
                } else {
                    return false
                }
            }
        })
        //To access local variable - create temp variable
        val finalResourceUrl: String = resourceUrl
        more_btn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                AnalyticsHelper.logAction(context, AnalyticsHelper.TAP_UPLOADS_INVOICES_DETAIL_MORE)
                menuPopup(more_btn, finalResourceUrl, position)
            }
        })
        lytUpload.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                AnalyticsHelper.logAction(context, AnalyticsHelper.TAP_DELETE_RE_UPLOAD_INVOICE)
                removeAt(position, false)
                SharedPref.writeBool(SharedPref.FLASH_INVOICE_UPLOAD, true)
                SharedPref.writeBool(SharedPref.AUTO_CROP_INVOICE_UPLOAD, true)
                val newIntent: Intent = Intent(context, InvoiceCameraPreview::class.java)
                newIntent.putExtra(
                    ZeemartAppConstants.CALLED_FROM,
                    ZeemartAppConstants.CALLED_FROM_ORDER_DETAILS
                )
                newIntent.putExtra(ZeemartAppConstants.ORDER_ID, invoice!!.orderIds?.get(0))
                (context as Activity).startActivityForResult(
                    newIntent,
                    ZeemartAppConstants.RequestCode.GoodsReceivedNoteDashBoardActivity
                )
            }
        })
        container.addView(imageLayout, 0)
        return imageLayout
    }

    fun removeAt(position: Int, isDelete: Boolean) {
        invoice!!.outlet = SharedPref.defaultOutlet
        val jsonStr = ZeemartBuyerApp.gsonExposeExclusive.toJson(invoice, InvoiceMgr::class.java)
        val tmpInvoiceMgr =
            ZeemartBuyerApp.gsonExposeExclusive.fromJson(jsonStr, InvoiceMgr::class.java)
        val resourceUrl = imageUrls!![position]
        for (imagesModel: ImagesModel in tmpInvoiceMgr.images!!) {
            for (name: String in imagesModel.imageFileNames) {
                if ((resourceUrl == imagesModel.imageURL + name)) {
                    imagesModel.imageFileNames.remove(name)
                    break
                }
            }
        }
        tmpInvoiceMgr.lastUpdatedBy = SharedPref.currentUserDetail
        val tmpInvoiceMgrStr =
            ZeemartBuyerApp.gsonExposeExclusive.toJson(tmpInvoiceMgr, InvoiceMgr::class.java)
        val inv =
            ZeemartBuyerApp.gsonExposeExclusive.fromJson(tmpInvoiceMgrStr, Invoice::class.java)
        if (inv.isStatus(Invoice.Status.PENDING) || inv.isStatus(Invoice.Status.PROCESSED)) {
            EditInvoice(context, inv, object : EditInvoice {
                override fun result(errorMsg: String?) {
                    if (errorMsg == null) {
                        invoice = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                            tmpInvoiceMgrStr,
                            Invoice::class.java
                        )
                        imageUrls = invoice.invoiceImagesList
                        val invoiceMgr = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                            tmpInvoiceMgrStr,
                            InvoiceMgr::class.java
                        )
                        if (imageUrls.size == 0) {
                            invoiceImageListener.invoiceDeleted(invoiceMgr, isDelete)
                        } else {
                            invoiceImageListener.invoiceUpdated(invoiceMgr)
                        }
                        notifyDataSetChanged()
                    } else {
                        getToastRed(errorMsg)
                    }
                }
            })
        } else if (inv.isStatus(Invoice.Status.REJECTED)) {
            invoice =
                ZeemartBuyerApp.gsonExposeExclusive.fromJson(tmpInvoiceMgrStr, Invoice::class.java)
            val invoiceMgr = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                tmpInvoiceMgrStr,
                InvoiceMgr::class.java
            )
            imageUrls = invoiceMgr.invoiceImagesList
            if (imageUrls.size == 0) {
                invoiceImageListener.invoiceDeleted(invoiceMgr, isDelete)
            } else {
                invoiceImageListener.invoiceUpdated(invoiceMgr)
            }
            notifyDataSetChanged()
        }
    }

    private fun alertDialog(
        mAdapter: InvoiceImagesSliderAdapter,
        position: Int,
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setPositiveButton(context.getString(R.string.txt_yes_delete)) { dialog, which ->
            mAdapter.removeAt(position, true)
            dialog.dismiss()
        }
        builder.setNegativeButton(context.getString(R.string.txt_cancel)) { dialog, which -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.setOnDismissListener { mAdapter.notifyDataSetChanged() }
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setTitle(context.getString(R.string.txt_delete_file))
        dialog.setMessage(context.getString(R.string.txt_delete_the_file))
        dialog.show()
        val deleteBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
        deleteBtn.setTextColor(context.resources.getColor(R.color.chart_red))
        deleteBtn.isAllCaps = false
        val cancelBtn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        cancelBtn.isAllCaps = false
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    fun menuPopup(anchor: View?, url: String, position: Int) {
        val popup = PopupMenu(context, anchor)
        //Inflating the Popup using xml file
        popup.menuInflater.inflate(R.menu.invoice_image_menu, popup.menu)
        if (invoice == null || (invoice != null && invoice!!.isProcessingProcessedDeleted)) {
            popup.menu.findItem(R.id.item_delete).isVisible = false
        }
        if ((invoice != null) && (invoice!!.orderIds != null) && !StringHelper.isStringNullOrEmpty(
                invoice!!.orderIds!![0]
            )
        ) {
            popup.menu.findItem(R.id.item_view_order).isVisible = true
        } else {
            popup.menu.findItem(R.id.item_view_order).isVisible = false
        }
        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.item_delete -> {
                    AnalyticsHelper.logAction(
                        context,
                        AnalyticsHelper.TAP_UPLOADS_INVOICES_DETAIL_DELETE
                    )
                    alertDialog(this@InvoiceImagesSliderAdapter, position)
                    true
                }
                R.id.item_view_order -> {
                    AnalyticsHelper.logAction(
                        context,
                        AnalyticsHelper.TAP_VIEW_RELATED_ORDER_TO_INVOICE
                    )
                    val newIntent = Intent(context, OrderDetailsActivity::class.java)
                    newIntent.putExtra(ZeemartAppConstants.ORDER_ID, invoice!!.orderIds!![0])
                    newIntent.putExtra(
                        ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID,
                        SharedPref.defaultOutlet?.outletId
                    )
                    context.startActivity(newIntent)
                    true
                }
                R.id.item_download -> {
                    val permissionCallbackDownload = returnDownloadPermissionCallback(url)
                    if (context is BaseNavigationActivity) {
                        context.requestPermission(
                            PermissionCallback.WRITE_IMAGE,
                            permissionCallbackDownload
                        )
                    } else if (context is InvoiceDetailActivity) {
                        context.requestPermission(
                            PermissionCallback.WRITE_IMAGE,
                            permissionCallbackDownload
                        )
                    } else if (context is RejectedInvoiceActivity) {
                        context.requestPermission(
                            PermissionCallback.WRITE_IMAGE,
                            permissionCallbackDownload
                        )
                    }
                    true
                }
                R.id.item_share -> {
                    AnalyticsHelper.logAction(
                        context,
                        AnalyticsHelper.TAP_UPLOADS_INVOICES_DETAIL_SHARE
                    )
                    val sharePermissionCallback = returnSharePermissionCallback(url)
                    if (context is BaseNavigationActivity) {
                        context.requestPermission(
                            PermissionCallback.WRITE_IMAGE,
                            sharePermissionCallback
                        )
                    } else if (context is InvoiceDetailActivity) {
                        context.requestPermission(
                            PermissionCallback.WRITE_IMAGE,
                            sharePermissionCallback
                        )
                    } else if (context is RejectedInvoiceActivity) {
                        context.requestPermission(
                            PermissionCallback.WRITE_IMAGE,
                            sharePermissionCallback
                        )
                    }
                    true
                }
                else -> false
            }
        })
        popup.show() //showing popup menu
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        //super.destroyItem(container, position, object);
        container.removeView(`object` as View)
    }

    private fun returnDownloadPermissionCallback(
        url: String,
    ): PermissionCallback? {
        return object : PermissionCallback {
            override fun denied(requestCode: Int) {}
            override fun allowed(requestCode: Int) {
                if (url.contains(ServiceConstant.FILE_EXT_PDF)) {
                    val request = InputStreamVolleyRequest(
                        Request.Method.GET,
                        url,
                        { response ->
                            savePdfToDocuments(context, response)
                            getToastGreen(context.getString(R.string.txt_save_to_documents))
                        },
                        { },
                        null
                    )
                    getInstance(context)!!.addToRequestQueue(request)
                } else {
                    image = object : Target {
                        override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom) {
                            getBitmapUri(context, bitmap)
                            getToastGreen(context.getString(R.string.txt_save_to_documents))
                        }

                        override fun onBitmapFailed(
                            e: java.lang.Exception,
                            errorDrawable: Drawable,
                        ) {
                            getToastRed(context.getString(R.string.txt_could_not_download))
                        }

                        override fun onPrepareLoad(placeHolderDrawable: Drawable) {}
                    }
                    Picasso.get()
                        .load(url)
                        .into(image)
                }
            }
        }
    }

    private fun returnSharePermissionCallback(
        url: String,
    ): PermissionCallback? {
        return object : PermissionCallback {
            override fun denied(requestCode: Int) {}
            override fun allowed(requestCode: Int) {
                if (url.contains(ServiceConstant.FILE_EXT_PDF)) {
                    val request = InputStreamVolleyRequest(
                        Request.Method.GET,
                        url,
                        { response ->
                            sharingIntent = Intent(Intent.ACTION_SEND)
                            val u = savePdfToDocuments(context, response)
                            sharingIntent.type = ServiceConstant.FILE_TYPE_PDF
                            sharingIntent.putExtra(Intent.EXTRA_STREAM, u)
                            context.startActivity(
                                Intent.createChooser(
                                    sharingIntent,
                                    context.resources.getText(R.string.txt_send_to)
                                )
                            )
                        },
                        { },
                        null
                    )
                    getInstance(context)!!.addToRequestQueue(request)
                } else {
                    image = object : Target {
                        override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom) {
                            if (bitmap != null) {
                                sharingIntent = Intent(Intent.ACTION_SEND)
                                val u = getBitmapUri(context, bitmap)
                                sharingIntent.type = ServiceConstant.FILE_TYPE_IMAGE
                                sharingIntent.putExtra(Intent.EXTRA_STREAM, u)
                                context.startActivity(
                                    Intent.createChooser(
                                        sharingIntent,
                                        context.resources.getText(R.string.txt_send_to)
                                    )
                                )
                            }
                        }

                        override fun onBitmapFailed(e: Exception, errorDrawable: Drawable) {}
                        override fun onPrepareLoad(placeHolderDrawable: Drawable) {}
                    }
                    Picasso.get()
                        .load(url)
                        .into(image)
                }
            }
        }
    }
}