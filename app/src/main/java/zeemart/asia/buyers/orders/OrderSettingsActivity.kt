package zeemart.asia.buyers.orders

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.adapter.OutletListAdapterForOrderSettings
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.DialogHelper
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.models.OrderSettingsRequest
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.Supplier
import zeemart.asia.buyers.modelsimport.OrderSettingDeliveryPreferences
import zeemart.asia.buyers.modelsimport.OutletSuppliersResponseForOrderSettings
import zeemart.asia.buyers.network.OrderSettingsApi

/**
 * Created by RajPrudhviMarella on 23/Aug/2021.
 */
class OrderSettingsActivity : AppCompatActivity() {
    private var btnBack: ImageView? = null
    private var txtHeader: TextView? = null
    private var txtSubHeader: TextView? = null
    private lateinit var lstOutlets: RecyclerView
    private var threeDotLoaderWhite: CustomLoadingViewWhite? = null
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_settings)
        btnBack = findViewById<ImageView>(R.id.bt_back_btn)
        btnBack!!.setOnClickListener { finish() }
        txtHeader = findViewById<TextView>(R.id.txt_header)
        threeDotLoaderWhite = findViewById<CustomLoadingViewWhite>(R.id.lyt_loading_overlay)
        txtSubHeader = findViewById<TextView>(R.id.txt_content)
        ZeemartBuyerApp.setTypefaceView(
            txtHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtSubHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        lstOutlets = findViewById<RecyclerView>(R.id.lst_outlets)
        lstOutlets.setLayoutManager(LinearLayoutManager(this))
    }

    private fun callOutletsApi() {
        threeDotLoaderWhite?.setVisibility(View.VISIBLE)
        OrderSettingsApi.getAllOutletsSuppliers(
            this@OrderSettingsActivity,
            object : OrderSettingsApi.OrderSettingOutletsListener {
                override fun onOutletSuccess(allStockShelveData: OutletSuppliersResponseForOrderSettings?) {
                    if (allStockShelveData != null && allStockShelveData.data != null && allStockShelveData.data!!
                            .size > 0
                    ) {
                        allStockShelveData.data!!.get(0).isDisplaySuppliersList
                        if (lstOutlets.getAdapter() != null) {
                            lstOutlets.getAdapter()!!.notifyDataSetChanged()
                        } else {
                            lstOutlets.setAdapter(
                                OutletListAdapterForOrderSettings(
                                    allStockShelveData.data!!,
                                    this@OrderSettingsActivity,
                                    object : OutletListAdapterForOrderSettings.onOutletSelected {
                                        override fun onOutletClicked(
                                            supplier: Supplier?,
                                            outlet: Outlet?
                                        ) {
                                            threeDotLoaderWhite?.setVisibility(View.VISIBLE)
                                            OrderSettingsApi.getDeliveryDates(
                                                this@OrderSettingsActivity,
                                                outlet?.outletId,
                                                supplier?.supplierId,
                                                object :
                                                    OrderSettingsApi.OrderSettingDeliveryDatesListener {
                                                    override fun onDeliveryDateListSuccess(allStockShelveData: OrderSettingDeliveryPreferences?) {
                                                        threeDotLoaderWhite?.setVisibility(View.GONE)
                                                        if (allStockShelveData != null && allStockShelveData.data != null) DialogHelper.showOrderSettingsVerificationDialog(
                                                            this@OrderSettingsActivity,
                                                            allStockShelveData.data!!.outlet
                                                                ?.outletName,
                                                            allStockShelveData.data!!
                                                                .supplier?.supplierName,
                                                            allStockShelveData.data!!,
                                                            object :
                                                                DialogHelper.onButtonSaveClickListener {
                                                                override fun onButtonSaveClicked(
                                                                    lstData: OrderSettingDeliveryPreferences.Data?,
                                                                    isWhatsapp: Boolean?,
                                                                    isSms: Boolean?
                                                                ) {
                                                                    threeDotLoaderWhite?.setVisibility(
                                                                        View.VISIBLE
                                                                    )
                                                                    val orderSettingsRequest =
                                                                        OrderSettingsRequest()
                                                                    orderSettingsRequest.cutOffTimes =
                                                                        lstData?.cutOffTimes
                                                                    if (!isWhatsapp!!) {
                                                                        lstData?.userNotifications
                                                                            ?.whatsapp
                                                                    }
                                                                    if (!isSms!!) {
                                                                        lstData?.userNotifications
                                                                            ?.phone
                                                                    }
                                                                    orderSettingsRequest.userNotifications =
                                                                        lstData?.userNotifications
                                                                    orderSettingsRequest.isUseDefault =
                                                                        false
                                                                    orderSettingsRequest.isOrderDisabled =
                                                                        false
                                                                    lstData?.outlet?.let {
                                                                        OrderSettingsApi.saveOrderSettings(
                                                                            this@OrderSettingsActivity,
                                                                            it,
                                                                            lstData.supplier
                                                                                ?.supplierId,
                                                                            ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                                                                orderSettingsRequest
                                                                            ),
                                                                            object :
                                                                                OrderSettingsApi.OrderSettingDeliveryDatesListener {
                                                                                override fun onDeliveryDateListSuccess(
                                                                                    allStockShelveData: OrderSettingDeliveryPreferences?
                                                                                ) {
                                                                                    if (allStockShelveData != null && allStockShelveData.status
                                                                                            ?.equals(200) == true
                                                                                    ) {
                                                                                        threeDotLoaderWhite?.setVisibility(
                                                                                            View.GONE
                                                                                        )
                                                                                        val dialog: AlertDialog =
                                                                                            DialogHelper.alertDialogSmallSuccess(
                                                                                                this@OrderSettingsActivity,
                                                                                                getString(
                                                                                                    R.string.txt_saved
                                                                                                )
                                                                                            )
                                                                                        Handler().postDelayed(
                                                                                            {
                                                                                                if (dialog != null && dialog.isShowing) {
                                                                                                    dialog.dismiss()
                                                                                                    lstOutlets.setAdapter(
                                                                                                        null
                                                                                                    )
                                                                                                    callOutletsApi()
                                                                                                }
                                                                                            },
                                                                                            4000
                                                                                        )
                                                                                    } else {
                                                                                        threeDotLoaderWhite?.setVisibility(
                                                                                            View.GONE
                                                                                        )
                                                                                        val dialogFailure: AlertDialog =
                                                                                            DialogHelper.alertDialogSmallFailure(
                                                                                                this@OrderSettingsActivity,
                                                                                                getString(
                                                                                                    R.string.txt_saving_failed
                                                                                                ),
                                                                                                getString(
                                                                                                    R.string.txt_please_try_again
                                                                                                )
                                                                                            )
                                                                                        dialogFailure.show()
                                                                                        Handler().postDelayed(
                                                                                            { dialogFailure.dismiss() },
                                                                                            2000
                                                                                        )
                                                                                    }
                                                                                }

                                                                                override fun errorResponse(
                                                                                    error: VolleyError?
                                                                                ) {
                                                                                    threeDotLoaderWhite?.setVisibility(
                                                                                        View.GONE
                                                                                    )
                                                                                    val dialogFailure: AlertDialog =
                                                                                        DialogHelper.alertDialogSmallFailure(
                                                                                            this@OrderSettingsActivity,
                                                                                            getString(
                                                                                                R.string.txt_saving_failed
                                                                                            ),
                                                                                            getString(
                                                                                                R.string.txt_please_try_again
                                                                                            )
                                                                                        )
                                                                                    dialogFailure.show()
                                                                                    Handler().postDelayed(
                                                                                        { dialogFailure.dismiss() },
                                                                                        2000
                                                                                    )
                                                                                }
                                                                            })
                                                                    }
                                                                }
                                                            })
                                                    }

                                                    override fun errorResponse(error: VolleyError?) {
                                                        threeDotLoaderWhite?.setVisibility(View.GONE)
                                                    }
                                                })
                                        }
                                    })
                            )
                        }
                        txtHeader?.setText(getResources().getString(R.string.txt_review_settings))
                        txtSubHeader?.setText(getResources().getString(R.string.txt_tap_supplier))
                    } else {
                        txtHeader?.setText(getResources().getString(R.string.txt_currently_no_order_settings_header))
                        txtSubHeader?.setText(getResources().getString(R.string.txt_currently_no_order_settings_sub_header))
                    }
                    threeDotLoaderWhite?.setVisibility(View.GONE)
                }

                override fun errorResponse(error: VolleyError?) {
                    threeDotLoaderWhite?.setVisibility(View.GONE)
                    txtHeader?.setText(getResources().getString(R.string.txt_currently_no_order_settings_header))
                    txtSubHeader?.setText(getResources().getString(R.string.txt_currently_no_order_settings_sub_header))
                }
            })
    }

    protected override fun onResume() {
        callOutletsApi()
        super.onResume()
    }
}