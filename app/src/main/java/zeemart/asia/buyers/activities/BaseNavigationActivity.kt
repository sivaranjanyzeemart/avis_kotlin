package zeemart.asia.buyers.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.android.volley.VolleyError
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.aurelhubert.ahbottomnavigation.notification.AHNotification
import com.google.gson.reflect.TypeToken
import io.intercom.android.sdk.Intercom
import zeemart.asia.buyers.R
import zeemart.asia.buyers.UserAgreementAndTermsPolicy.UserAgreementActivity
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.adapter.TabsPagerAdapter
import zeemart.asia.buyers.constants.DebugConstants
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.navigation.HomeNavigationTabListener
import zeemart.asia.buyers.helper.navigation.InvoiceTabNavigationListener
import zeemart.asia.buyers.helper.NoSwipeViewPager
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.interfaces.OnFragmentInteractionListener
import zeemart.asia.buyers.interfaces.PermissionCallback
import zeemart.asia.buyers.interfaces.UnitSizeResponseListener
import zeemart.asia.buyers.inventory.InventoryDashBoardFragment
import zeemart.asia.buyers.invoices.InQueueForUploadDataModel
import zeemart.asia.buyers.invoices.InvoiceDataManager
import zeemart.asia.buyers.invoices.InvoicesFragment
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.UnitSizeModel
import zeemart.asia.buyers.models.UserPermission
import zeemart.asia.buyers.models.invoice.Invoice
import zeemart.asia.buyers.models.userAgreement.Agreements
import zeemart.asia.buyers.models.userAgreement.ValidateAgreements
import zeemart.asia.buyers.more.MoreFragment
import zeemart.asia.buyers.network.GetUnitSizes
import zeemart.asia.buyers.network.InvoiceHelper
import zeemart.asia.buyers.network.InvoiceHelper.GetInvoices
import zeemart.asia.buyers.network.InvoiceHelper.invoiceCountbyStatus
import zeemart.asia.buyers.network.UserAgreement
import zeemart.asia.buyers.network.UserAgreement.ValidateAgreementResponseListener
import zeemart.asia.buyers.network.VolleyErrorHelper
import zeemart.asia.buyers.orders.DashboardFragment
import zeemart.asia.buyers.reports.reportsummary.ReportDashboardFragment

class BaseNavigationActivity : FragmentActivity(), OnFragmentInteractionListener {

    private lateinit var homeNavigationTabListener: HomeNavigationTabListener
    var rejectedFailedInvoiceCount = 0
    lateinit var viewPager: NoSwipeViewPager
    private lateinit var bottomNavigationView: AHBottomNavigation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
        //get the unit sizes shortnames
        allUnitSizeShortNames
        userAgreementAcceptedOrNot()
        bottomNavigationView = findViewById(R.id.navigation)
        viewPager = findViewById(R.id.tabViewPager)
        viewPager.setPagingEnabled(false)
        viewPager.setOffscreenPageLimit(3)
        val tabsPagerAdapter = TabsPagerAdapter(supportFragmentManager)
        reloadTab(this@BaseNavigationActivity, ZeemartAppConstants.HOME_TAB_ID)

        //Add Fragments to PagerAdapter
        val homeFragment = DashboardFragment.newInstance()
        val moreFragment = MoreFragment.newInstance()
        tabsPagerAdapter.addFragments(homeFragment)
        if (UserPermission.HasInvoice()) {
            val invoicesFragment = InvoicesFragment.newInstance()
            tabsPagerAdapter.addFragments(invoicesFragment)
        }
        if (UserPermission.HasViewReport()) {
            val reportDashboardFragment = ReportDashboardFragment.newInstance()
            tabsPagerAdapter.addFragments(reportDashboardFragment)
        }
        if (UserPermission.HasViewInventory()) {
            val inventoryDashboardFragment = InventoryDashBoardFragment.newInstance()
            tabsPagerAdapter.addFragments(inventoryDashboardFragment)
        }
        tabsPagerAdapter.addFragments(moreFragment)
        viewPager.setAdapter(tabsPagerAdapter)
        setFailedRejectedInvoices()
        bottomNavigationView.setOnTabSelectedListener(
            AHBottomNavigation.OnTabSelectedListener { position, wasSelected ->
                Log.d(DebugConstants.ZEEMARTDEBUG, "inside on Tab selected android-->$position")
                viewPager.setCurrentItem(position)
                viewPager.getAdapter()!!.notifyDataSetChanged()
                /**
                 * call invoice API only when invoice tab is tapped
                 */
                /**
                 * call invoice API only when invoice tab is tapped
                 */
                if (viewPager.getAdapter() != null) {
                    if ((viewPager.getAdapter() as TabsPagerAdapter?)!!.getItem(position) is InvoicesFragment) {
                        AnalyticsHelper.logAction(
                            this@BaseNavigationActivity,
                            AnalyticsHelper.TAP_INVOICE_TAB
                        )
                        val mInvoiceFragment =
                            (viewPager.getAdapter() as TabsPagerAdapter?)!!.getItem(position) as InvoicesFragment
                        mInvoiceFragment.loadInvoiceFragmentData()
                    } else if ((viewPager.getAdapter() as TabsPagerAdapter?)!!.getItem(position) is ReportDashboardFragment) {
                        AnalyticsHelper.logAction(
                            this@BaseNavigationActivity,
                            AnalyticsHelper.TAP_REPORTS_TAB
                        )
                        val mReportsFragment =
                            (viewPager.getAdapter() as TabsPagerAdapter?)!!.getItem(position) as ReportDashboardFragment
                        mReportsFragment.loadReport()
                    } else if ((viewPager.getAdapter() as TabsPagerAdapter?)!!.getItem(position) is DashboardFragment) {
                        AnalyticsHelper.logAction(
                            this@BaseNavigationActivity,
                            AnalyticsHelper.TAP_ORDERS_TAB
                        )
                        val fragment =
                            (viewPager.getAdapter() as TabsPagerAdapter?)!!.getItem(position) as DashboardFragment
                        fragment.refreshDashboard()
                    } else if ((viewPager.getAdapter() as TabsPagerAdapter?)!!.getItem(position) is InventoryDashBoardFragment) {
                        AnalyticsHelper.logAction(
                            this@BaseNavigationActivity,
                            AnalyticsHelper.TAP_INVENTORY_TAB
                        )
                        val fragment =
                            (viewPager.getAdapter() as TabsPagerAdapter?)!!.getItem(position) as InventoryDashBoardFragment
                        fragment.loadInventory()
                    } else if ((viewPager.getAdapter() as TabsPagerAdapter?)!!.getItem(position) is MoreFragment) {
                        AnalyticsHelper.logAction(
                            this@BaseNavigationActivity,
                            AnalyticsHelper.TAP_MORE_TAB
                        )
                    }
                }
                true
            })
        setUnreadIntercomMessages()
    }

    //fun onFragmentInteraction(fragment: Fragment, uri: Uri) {}

    /**
     * get unread messages for intercom
     */
    fun setUnreadIntercomMessages() {
        val unreadMessages = Intercom.client().unreadConversationCount
        Intercom.client().addUnreadConversationCountListener { i ->
            Log.d("INTERCOM UNREADLISTENER", "$i******")
            if (i == 0) {
                removeBadge(ZeemartAppConstants.MORE_TAB_ID)
            } else {
                setUnreadBadgeOnTab(i, ZeemartAppConstants.MORE_TAB_ID)
            }
        }
        if (unreadMessages == 0) {
            removeBadge(ZeemartAppConstants.MORE_TAB_ID)
        } else {
            setUnreadBadgeOnTab(unreadMessages, ZeemartAppConstants.MORE_TAB_ID)
        }
        Log.d("INTERCOM UNREAD", "$unreadMessages******")
    }

    fun setUnreadBadgeOnTab(unreadMessages: Int, tabPosition: Int) {
        Handler().postDelayed({
            val notification = AHNotification.Builder()
                .setText(Integer.toString(unreadMessages))
                .setBackgroundColor(resources.getColor(R.color.pinky_red))
                .setTextColor(Color.WHITE)
                .build()
            // Adding notification to last item.
            bottomNavigationView.setNotification(notification, tabPosition)
        }, 1000)
    }

    fun removeBadge(position: Int) {
        bottomNavigationView.setNotification(
            AHNotification(),
            position
        )
    }

    /**
     * get the shortnames of unit sizes to be displayed throughout the app
     * eg Kilograms = Kg
     */
    val allUnitSizeShortNames: Unit
        get() {
            GetUnitSizes(this, object : UnitSizeResponseListener {
                override fun onSuccessResponse(unitSizeMap: Map<String?, UnitSizeModel?>?) {
                    val unitSizeMapStringJson =
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(unitSizeMap)
                    SharedPref.write(SharedPref.UNIT_SIZE_MAP, unitSizeMapStringJson)
                }

                override fun onErrorResponse(error: VolleyError?) {}
            }).unitSizeMap
        }

    override fun onBackPressed() {
        finish()
        ActivityCompat.finishAffinity(this)
    }

    var permissionCallback: PermissionCallback? = null
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
                    permissionCallback?.allowed(requestCode)
                }
            }
            PermissionCallback.TAKE_IMAGE -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    // Permission is not granted => Request for permission
                    ActivityCompat.requestPermissions(
                        this, arrayOf(Manifest.permission.CAMERA),
                        requestCode
                    )
                } else {
                    permissionCallback?.allowed(requestCode)
                }
            }
            else -> permissionCallback?.denied(requestCode)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissionCallback != null && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // permission was granted
            permissionCallback!!.allowed(requestCode)
        } else {
            // permission denied, boo! Disable the
            permissionCallback!!.denied(requestCode)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.extras != null && intent.extras!!.containsKey(ZeemartAppConstants.INVOICE_IMAGE_URI)) {
            val invoiceDataList: ArrayList<InQueueForUploadDataModel>
            val uriListString = intent.extras!!.getString(ZeemartAppConstants.INVOICE_IMAGE_URI)
            invoiceDataList = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                uriListString,
                object : TypeToken<ArrayList<InQueueForUploadDataModel?>?>() {}.type
            )
            (homeNavigationTabListener as InvoiceTabNavigationListener?)!!.onDataRequestForUpload(
                invoiceDataList
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (bottomNavigationView!!.currentItem == ZeemartAppConstants.HOME_TAB_ID) {
            bottomNavigationView!!.post {
                bottomNavigationView!!.currentItem =
                    ZeemartAppConstants.HOME_TAB_ID
            }
        }
        if (SharedPref.readBool(ZeemartAppConstants.DISPLAY_UPDATE_DIALOG_IS_VISIBLE, false) == true) {
            val newIntentFront = Intent(this, UpdateAppDialogActivity::class.java)
            newIntentFront.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            newIntentFront.putExtra(
                ZeemartAppConstants.UPDATE_DIALOG_TYPE,
                ZeemartAppConstants.UPDATE_APP_TO_LATEST_DIALOG
            )
            startActivity(newIntentFront)
        } else if (SharedPref.readBool(
                ZeemartAppConstants.DISPLAY_FORCE_UPDATE_DIALOG_IS_VISIBLE,
                false
            ) == true
        ) {
            val newIntent = Intent(this, UpdateAppDialogActivity::class.java)
            newIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            newIntent.putExtra(
                ZeemartAppConstants.UPDATE_DIALOG_TYPE,
                ZeemartAppConstants.FORCE_UPDATE
            )
            startActivity(newIntent)
        }
    }

    fun setTabFragmentUpdateListener(listener: HomeNavigationTabListener?) {
        if (listener != null) {
            homeNavigationTabListener = listener
        }
    }

    /**
     * update the failed and rejected count on bottom navigation bar
     */
    open fun setFailedRejectedInvoices() {
        rejectedFailedInvoiceCount = 0
        val invoiceDataManager = InvoiceDataManager(this)
        val pendingForUploadInvoices = invoiceDataManager.getAlltheInQueueInvoiceList(
            SharedPref.defaultOutlet?.outletId!!
        )
        if (pendingForUploadInvoices != null && pendingForUploadInvoices.size > 0) {
            rejectedFailedInvoiceCount = pendingForUploadInvoices.size
        }
        val invoiceStatuses = Invoice.Status.REJECTED.toString()
        invoiceCountbyStatus(this, invoiceStatuses, object : GetInvoices {
            override fun result(invoices: List<Invoice>?) {
                if (invoices != null && invoices.size > 0) {
                    for (i in invoices.indices) {
                        val rejectedInvoiceCount = Integer.toString(invoices[i].count!!)
                        if (invoices[i].isStatus(Invoice.Status.REJECTED) && !StringHelper.isStringNullOrEmpty(
                                rejectedInvoiceCount
                            )
                        ) {
                            rejectedFailedInvoiceCount =
                                rejectedFailedInvoiceCount + invoices[i].count!!
                        }
                    }
                } else {
                    updateInvoiceCount(this@BaseNavigationActivity, rejectedFailedInvoiceCount)
                }
                updateInvoiceCount(this@BaseNavigationActivity, rejectedFailedInvoiceCount)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == ZeemartAppConstants.ResultCode.RESULT_IMAGES) {
            removeAt()
        }
        if (resultCode == RESULT_CANCELED) {
            setFailedRejectedInvoices()
        }
    }

    fun removeAt() {
        val invoiceId = SharedPref.read(SharedPref.INVOICE_ID_DELETE, "")
        if (!StringHelper.isStringNullOrEmpty(invoiceId)) {
            val json = InvoiceHelper.createDeleteJsonForInvoice(invoiceId)
            val outlet: MutableList<Outlet> = ArrayList()
            outlet.add(SharedPref.defaultOutlet!!)
            InvoiceHelper.deleteUnprocessedInvoice(
                this@BaseNavigationActivity,
                outlet,
                object : GetRequestStatusResponseListener {
                    override fun onSuccessResponse(status: String?) {
                        viewPager!!.adapter!!.notifyDataSetChanged()
                    }

                    override fun onErrorResponse(error: VolleyErrorHelper?) {
                        ZeemartBuyerApp.getToastRed(getString(R.string.txt_delete_draft_fail))
                    }
                },
                json
            )
        }
    }

    private fun userAgreementAcceptedOrNot() {
        UserAgreement.retrieveAndValidateAgreement(
            this@BaseNavigationActivity,
            object : ValidateAgreementResponseListener {
                override fun onSuccessResponse(
                    validateAgreements: ValidateAgreements?,
                    agreements: Agreements?,
                ) {
                    if (validateAgreements != null && validateAgreements.data != null && validateAgreements.data!!.size > 0) {
                        val intent =
                            Intent(this@BaseNavigationActivity, UserAgreementActivity::class.java)
                        intent.putExtra(
                            ZeemartAppConstants.USER_VALID_AGREEMENT,
                            ZeemartBuyerApp.gsonExposeExclusive.toJson(validateAgreements)
                        )
                        intent.putExtra(
                            ZeemartAppConstants.USER_AGREEMENT,
                            ZeemartBuyerApp.gsonExposeExclusive.toJson(agreements)
                        )
                        startActivity(intent)
                    }
                }

                override fun onErrorResponse() {}
            },
            SharedPref.allOutlets
        )
    }

    companion object {
        private lateinit var bottomNavigationView: AHBottomNavigation
        private var viewPager: NoSwipeViewPager? = null

        @JvmStatic
        fun reloadTab(context: Context, tabId: Int) {
            val bottomNavigationView = (context as? BaseNavigationActivity)?.findViewById<AHBottomNavigation>(R.id.navigation)

            if (bottomNavigationView != null){
                bottomNavigationView!!.removeAllItems()
            }
            val item1 = AHBottomNavigationItem(
                context.getString(R.string.txt_bottom_tab_bar_order),
                R.drawable.orderstab
            )
            val item2 = AHBottomNavigationItem(
                context.getString(R.string.txt_bottom_tab_bar_invoice),
                R.drawable.invoicestab
            )
            val item3 = AHBottomNavigationItem(
                context.getString(R.string.txt_reports),
                R.drawable.report_tab_icon
            )
            val item4 = AHBottomNavigationItem(
                context.getString(R.string.more_menu_inventory),
                R.drawable.tab_inventory_white
            )
            val item5 = AHBottomNavigationItem(
                context.getString(R.string.txt_bottom_tab_bar_more),
                R.drawable.moretab
            )
            bottomNavigationView!!.addItem(item1)
            if (UserPermission.HasInvoice()) {
                bottomNavigationView!!.addItem(item2)
            }
            if (UserPermission.HasViewReport()) {
                bottomNavigationView!!.addItem(item3)
            }
            if (UserPermission.HasViewInventory()) {
                bottomNavigationView!!.addItem(item4)
            }
            bottomNavigationView!!.addItem(item5)
            UserPermission.setTabIdsAsPerPermissions()
            bottomNavigationView!!.defaultBackgroundColor =
                context.resources.getColor(R.color.black)
            bottomNavigationView!!.accentColor =
                context.resources.getColor(R.color.squash)
            bottomNavigationView!!.titleState =
                AHBottomNavigation.TitleState.ALWAYS_SHOW
            bottomNavigationView!!.currentItem = tabId
        }

        @JvmStatic
        fun updateInvoiceCount(context: Context?, count: Int) {
            val baseNavigationActivity = context as? BaseNavigationActivity
            val bottomNavigationView = baseNavigationActivity?.findViewById<AHBottomNavigation>(R.id.navigation)

            if (bottomNavigationView != null && count != 0) {
                val notification = AHNotification.Builder()
                    .setText(count.toString())
                    .setBackgroundColor(context.resources.getColor(R.color.pinky_red))
                    .setTextColor(Color.WHITE)
                    .build()

                bottomNavigationView.setNotification(
                    notification,
                    ZeemartAppConstants.INVOICES_TAB_ID
                )
            } else if (bottomNavigationView != null) {
                bottomNavigationView.setNotification(
                    AHNotification(),
                    ZeemartAppConstants.INVOICES_TAB_ID
                )
            }
        }
    }

    override fun onFragmentInteraction(fragment: Fragment?, uri: Uri?) {
        TODO("Not yet implemented")
    }
}