package zeemart.asia.buyers.invoices

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.activities.BaseNavigationActivity
import zeemart.asia.buyers.adapter.InvoiceImagesSliderAdapter
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.interfaces.PermissionCallback
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.invoice.Invoice
import zeemart.asia.buyers.network.InvoiceHelper
import zeemart.asia.buyers.network.VolleyErrorHelper

/**
 * Created by Muthumari on 2/8/2019.
 */
class RejectedInvoiceActivity : AppCompatActivity() {
    private var invoiceId: String? = null
    private var viewPager: ViewPager? = null
    private var permissionCallback: PermissionCallback? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_image_fullscreen)
        val bundle: Bundle? = getIntent().getExtras()
        if (bundle != null) {
            if (bundle.containsKey(ZeemartAppConstants.INVOICE_ID)) {
                invoiceId = bundle.getString(ZeemartAppConstants.INVOICE_ID)
                viewPager = findViewById<ViewPager>(R.id.imageView)
                viewPager!!.setOffscreenPageLimit(0)
                val outlet: MutableList<Outlet> = ArrayList<Outlet>()
                outlet.add(SharedPref.defaultOutlet!!)
                InvoiceHelper.GetAllInvoicesForParticularOrder(
                    this,
                    null,
                    true,
                    object : InvoiceHelper.GetInvoices {
                        override fun result(invoices: List<Invoice>?) {
                            for (i in invoices?.indices!!) {
                                if (invoices[i].invoiceId == invoiceId) {
                                    loadRejectedInvoice(invoices[i])
                                    break
                                }
                            }
                        }
                    }
                )
                //                InvoiceHelper.GetRejectedInvoice(this, outlet, invoiceId, new InvoiceHelper.GetRejectedInvoice() {
//                    @Override
//                    public void result(Invoice invoice) {
//                        if (invoice != null) {
//                            loadRejectedInvoice(invoice);
//                        }
//                    }
//                });
            }
        }
    }

    private fun loadRejectedInvoice(invoice: Invoice?) {
        viewPager?.setAdapter(
            InvoiceImagesSliderAdapter(
                this,
                invoice!!,
                object : InvoiceImagesSliderAdapter.InvoiceImageListener {
                    override fun invoiceUpdated(inv: Invoice?) {}
                    override fun invoiceDeleted(inv: Invoice?, isDeleted: Boolean) {
                        if (invoice != null) {
                            if (invoice.invoiceId == inv?.invoiceId) {
                                val json: String =
                                    InvoiceHelper.createDeleteJsonForInvoice(inv?.invoiceId)
                                val outlet: MutableList<Outlet> = ArrayList<Outlet>()
                                outlet.add(inv?.outlet!!)
                                InvoiceHelper.deleteUnprocessedInvoice(
                                    this@RejectedInvoiceActivity,
                                    outlet,
                                    object : GetRequestStatusResponseListener {
                                        override fun onSuccessResponse(status: String?) {
                                            //show dashboard
                                            startActivity(
                                                Intent(
                                                    this@RejectedInvoiceActivity,
                                                    BaseNavigationActivity::class.java
                                                ).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                            )
                                        }

                                        override fun onErrorResponse(error: VolleyErrorHelper?) {
                                            ZeemartBuyerApp.getToastRed(
                                                this@RejectedInvoiceActivity.getString(
                                                    R.string.txt_delete_draft_fail
                                                )
                                            )
                                        }
                                    },
                                    json
                                )
                            }
                        }
                    }

                    override fun onRejectedInvoiceBackPressed() {
                        finish()
                        val newIntent =
                            Intent(this@RejectedInvoiceActivity, BaseNavigationActivity::class.java)
                        newIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        startActivity(newIntent)
                    }
                })
        )
    }

    fun requestPermission(requestCode: Int, permissionCallback: PermissionCallback?) {
        this.permissionCallback = permissionCallback
        when (requestCode) {
            PermissionCallback.WRITE_IMAGE -> {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    // Permission is not granted => Request for permission
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ),
                        requestCode
                    )
                } else {
                    if (permissionCallback != null) {
                        permissionCallback.allowed(requestCode)
                    }
                }
            }
            else -> {}
        }
    }
}