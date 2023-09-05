package zeemart.asia.buyers.selfOnBoarding

import android.animation.ArgbEvaluator
import android.app.TaskStackBuilder
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.android.volley.VolleyError
import com.google.android.material.tabs.TabLayout
import org.json.JSONException
import org.json.JSONObject
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.activities.BaseNavigationActivity
import zeemart.asia.buyers.activities.UpdateAppDialogActivity
import zeemart.asia.buyers.adapter.SplashViewPagerAdapter
import zeemart.asia.buyers.constants.NotificationConstants
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.UnitSizeResponseListener
import zeemart.asia.buyers.invoices.RejectedInvoiceActivity
import zeemart.asia.buyers.login.BuyerLoginActivity
import zeemart.asia.buyers.models.UnitSizeModel
import zeemart.asia.buyers.network.GetUnitSizes
import zeemart.asia.buyers.notifications.NotificationAnnouncementDetails
import zeemart.asia.buyers.orders.OrderDetailsActivity

/**
 * Created by RajPrudhviMarella on 9/15/2020.
 */
class WelcomeScreenActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager
    private lateinit var btnLogin: Button
    private lateinit var btnSignUp: Button
    private lateinit var tabLayout: TabLayout
    private lateinit var lytRow: ConstraintLayout
    private lateinit var btnNavigation: Button
    private lateinit var txtDivider: TextView
    private lateinit var imgLogo: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        viewPager = findViewById(R.id.splash_view_pager)
        btnLogin = findViewById(R.id.btn_log_in)
        allUnitSizeShortNames
        btnSignUp = findViewById(R.id.btn_sign_up)
        btnNavigation = findViewById(R.id.btn_navigation)
        tabLayout = findViewById(R.id.tab_layout)
        txtDivider = findViewById(R.id.txt_divider)
        imgLogo = findViewById(R.id.logo)
        lytRow = findViewById(R.id.lyt_row)
        lytRow.setBackgroundColor(resources.getColor(R.color.transparent))
        val splashViewPagerAdapter = SplashViewPagerAdapter(this@WelcomeScreenActivity)
        btnLogin.setOnClickListener(View.OnClickListener {
            if (viewPager != null && viewPager!!.currentItem == 1) {
                AnalyticsHelper.logGuestAction(
                    this@WelcomeScreenActivity,
                    AnalyticsHelper.TAP_WELCOME2_LOGIN
                )
            } else {
                AnalyticsHelper.logGuestAction(
                    this@WelcomeScreenActivity,
                    AnalyticsHelper.TAP_WELCOME1_LOGIN
                )
            }
            val intent = Intent(this@WelcomeScreenActivity, BuyerLoginActivity::class.java)
            intent.putExtra(ZeemartAppConstants.CALLED_FROM, ZeemartAppConstants.CALLED_FOR_SIGN_IN)
            startActivity(intent)
        })
        btnSignUp.setOnClickListener(View.OnClickListener {
            if (viewPager != null && viewPager!!.currentItem == 1) {
                AnalyticsHelper.logGuestAction(
                    this@WelcomeScreenActivity,
                    AnalyticsHelper.TAP_WELCOME2_SIGNUP
                )
            } else {
                AnalyticsHelper.logGuestAction(
                    this@WelcomeScreenActivity,
                    AnalyticsHelper.TAP_WELCOME1_SIGNUP
                )
            }
            val intent = Intent(this@WelcomeScreenActivity, BuyerLoginActivity::class.java)
            intent.putExtra(ZeemartAppConstants.CALLED_FROM, ZeemartAppConstants.CALLED_FOR_SIGN_UP)
            startActivity(intent)
        })
        viewPager.setAdapter(splashViewPagerAdapter)
        tabLayout.setupWithViewPager(viewPager)
        val colors = arrayOf(
            resources.getColor(R.color.color_azul_two),
            resources.getColor(R.color.light_green_splash_screen),
            resources.getColor(
                R.color.light_green_splash_screen
            )
        )
        viewPager.setOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                if (position < viewPager.getAdapter()!!.count - 1 && position < colors.size - 1) {
                    lytRow.setBackgroundColor(
                        (ArgbEvaluator().evaluate(
                            positionOffset,
                            colors[position],
                            colors[position + 1]
                        ) as Int)
                    )
                } else {
                    lytRow.setBackgroundColor(colors[colors.size - 1])
                }
                if (position == 0) {
                    btnNavigation.setBackground(resources.getDrawable(R.drawable.btn_splash_white))
                    imgLogo.setImageDrawable(resources.getDrawable(R.drawable.zmlogowhite))
                    btnNavigation.setTextColor(resources.getColor(R.color.color_azul_two))
                    btnLogin.setTextColor(resources.getColor(R.color.white))
                    txtDivider.setBackgroundColor(resources.getColor(R.color.white))
                    btnSignUp.setTextColor(resources.getColor(R.color.white))
                    btnNavigation.setText(resources.getString(R.string.txt_lets_start))
                    btnNavigation.setOnClickListener(View.OnClickListener {
                        AnalyticsHelper.logGuestAction(
                            this@WelcomeScreenActivity,
                            AnalyticsHelper.TAP_WELCOME1_LETS_START
                        )
                        viewPager.setCurrentItem(1, true)
                    })
                } else if (position == 1) {
                    btnNavigation.setBackground(resources.getDrawable(R.drawable.btn_splash_blue))
                    imgLogo.setImageDrawable(resources.getDrawable(R.drawable.zm_logo_black))
                    btnNavigation.setTextColor(resources.getColor(R.color.white))
                    btnLogin.setTextColor(resources.getColor(R.color.dark_grey))
                    txtDivider.setBackgroundColor(resources.getColor(R.color.dark_grey))
                    btnSignUp.setTextColor(resources.getColor(R.color.dark_grey))
                    btnNavigation.setText(resources.getString(R.string.txt_next))
                    btnNavigation.setOnClickListener(View.OnClickListener {
                        AnalyticsHelper.logGuestAction(
                            this@WelcomeScreenActivity,
                            AnalyticsHelper.TAP_WELCOME1_NEXT
                        )
                        viewPager.setCurrentItem(2, true)
                    })
                } else {
                    btnNavigation.setBackground(resources.getDrawable(R.drawable.btn_splash_blue))
                    imgLogo.setImageDrawable(resources.getDrawable(R.drawable.zm_logo_black))
                    btnNavigation.setTextColor(resources.getColor(R.color.white))
                    btnLogin.setTextColor(resources.getColor(R.color.dark_grey))
                    txtDivider.setBackgroundColor(resources.getColor(R.color.dark_grey))
                    btnSignUp.setTextColor(resources.getColor(R.color.dark_grey))
                    btnNavigation.setText(resources.getString(R.string.txt_see_whats_avaialable))
                    btnNavigation.setOnClickListener(View.OnClickListener {
                        AnalyticsHelper.logGuestAction(
                            this@WelcomeScreenActivity,
                            AnalyticsHelper.TAP_WELCOME2_SEEWHATSAVAILABLE
                        )
                        val intent =
                            Intent(this@WelcomeScreenActivity, EnterAddressActivity::class.java)
                        startActivity(intent)
                    })
                }
            }

            override fun onPageSelected(position: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}
        })
        setFont()
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

    private fun setFont() {
        setTypefaceView(btnLogin, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(btnSignUp, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR)
        setTypefaceView(btnNavigation, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD)
    }

    override fun onResume() {
        super.onResume()
        Log.d("EXTRA DATA", intent.extras.toString() + "******")
        val password = SharedPref.read(SharedPref.PASSWORD_ENCRYPTED, null)
        if (!StringHelper.isStringNullOrEmpty(password)) {
            /**
             * called when a notification is clicked from the notification tray
             * and the app is in the background
             */
            if (intent.extras != null) {
                val bundle = intent.extras
                if (bundle!!.containsKey(NotificationConstants.URI_KEY)) {
                    var type: String? = null
                    var orderId: String? = null
                    var invoiceId: String? = null
                    var announcementId: String? = null
                    try {
                        val jsonObjURI = JSONObject(bundle.getString(NotificationConstants.URI_KEY))
                        type = jsonObjURI.getString("type")
                        val jsonObjectParameters = jsonObjURI.getJSONObject("parameters")
                        if (type == NotificationConstants.INVOICE_DETAILS) {
                            invoiceId = jsonObjectParameters.getString("invoiceId")
                        } else if (type == NotificationConstants.ANNOUNCEMENTS) {
                            announcementId = jsonObjectParameters.getString("announcementId")
                        } else {
                            orderId = jsonObjectParameters.getString("orderId")
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    if (type != null && type == NotificationConstants.ORDER_DETAILS && orderId != null && bundle.containsKey(
                            NotificationConstants.OUTLETID_KEY
                        )
                    ) {
                        val intent = Intent(this, OrderDetailsActivity::class.java)
                        intent.putExtra(ZeemartAppConstants.ORDER_ID, orderId)
                        intent.putExtra(
                            ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID,
                            bundle.getString(NotificationConstants.OUTLETID_KEY)
                        )
                        val stackBuilder = TaskStackBuilder.create(this)
                        stackBuilder.addNextIntentWithParentStack(intent)
                        stackBuilder.startActivities()
                    } else if (type != null && type == NotificationConstants.INVOICE_DETAILS && invoiceId != null && bundle.containsKey(
                            NotificationConstants.OUTLETID_KEY
                        )
                    ) {
                        val intent = Intent(this, RejectedInvoiceActivity::class.java)
                        intent.putExtra(ZeemartAppConstants.INVOICE_ID, invoiceId)
                        intent.putExtra(
                            ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID,
                            bundle.getString(NotificationConstants.OUTLETID_KEY)
                        )
                        val stackBuilder = TaskStackBuilder.create(this)
                        stackBuilder.addNextIntentWithParentStack(intent)
                        stackBuilder.startActivities()
                    } else if (type != null && type == NotificationConstants.ANNOUNCEMENTS && announcementId != null && bundle.containsKey(
                            NotificationConstants.OUTLETID_KEY
                        )
                    ) {
                        val intent = Intent(this, NotificationAnnouncementDetails::class.java)
                        intent.putExtra(ZeemartAppConstants.ANNOUNCEMENT_ID, announcementId)
                        intent.putExtra(
                            ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID,
                            bundle.getString(NotificationConstants.OUTLETID_KEY)
                        )
                        intent.putExtra(
                            ZeemartAppConstants.CALLED_FROM,
                            NotificationConstants.CALLED_FROM_ANNOUNCEMENT_DETAILS
                        )
                        val stackBuilder = TaskStackBuilder.create(this)
                        stackBuilder.addNextIntentWithParentStack(intent)
                        stackBuilder.startActivities()
                    }
                } else {
                    startActivity(
                        Intent(
                            this,
                            BaseNavigationActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    )
                }
                finish()
            } else {
                startActivity(
                    Intent(
                        this,
                        BaseNavigationActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                )
                finish()
            }
        } else {
            if (SharedPref.readBool(ZeemartAppConstants.DISPLAY_UPDATE_DIALOG_IS_VISIBLE, false)!!) {
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
                )!!
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
    }
}