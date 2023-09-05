package zeemart.asia.buyers.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.Uri
import android.text.TextUtils
import android.text.format.Formatter
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.TextView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import io.intercom.android.sdk.Intercom
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.context
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.invoices.InQueueForUploadDataModel
import zeemart.asia.buyers.invoices.InvoiceDataManager
import zeemart.asia.buyers.login.BuyerLoginActivity
import zeemart.asia.buyers.models.BuyerDetails
import zeemart.asia.buyers.models.DeviceDimensionUI
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.OutletFuturesModel
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel
import zeemart.asia.buyers.network.PushNotificationApi.registerDeviceUserLogsOut
import zeemart.asia.buyers.network.VolleyErrorHelper
import zeemart.asia.buyers.network.VolleyRequest.Companion.getInstance
import java.io.File
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*

/**
 * Created by ParulBhandari on 12/12/2017.
 */
object CommonMethods {
    fun SentenceCap(string: String): String {
        return string.substring(0, 1).uppercase(Locale.getDefault()) + string.substring(1)
            .lowercase(
                Locale.getDefault()
            )
    }

    fun SupplierThumbNailShortCutText(supplierName: String): String {
        val myName = supplierName.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        var combinedName = ""
        for (i in myName.indices) {
            if (!myName[i].isEmpty()) {
                combinedName = combinedName + myName[i][0]
            }
        }
        val output = combinedName.replace("[^a-zA-Z ]".toRegex(), "")
        return output.uppercase(Locale.getDefault())
    }

    fun SupplierThumbNailTextColor(supplierName: String, context: Context): Int {
        val firstLetter = supplierName[0].toString()
        return if (firstLetter.uppercase(Locale.getDefault()) == "A" || firstLetter.uppercase(
                Locale.getDefault()
            ) == "B" || firstLetter.uppercase(
                Locale.getDefault()
            ) == "C" || firstLetter.uppercase(Locale.getDefault()) == "D"
        ) {
            context.resources.getColor(R.color.label_text_color_a_d)
        } else if (firstLetter.uppercase(Locale.getDefault()) == "E" || firstLetter.uppercase(
                Locale.getDefault()
            ) == "F" || firstLetter.uppercase(Locale.getDefault()) == "G" || firstLetter.uppercase(
                Locale.getDefault()
            ) == "H"
        ) {
            context.resources.getColor(R.color.label_text_color_e_h)
        } else if (firstLetter.uppercase(Locale.getDefault()) == "I" || firstLetter.uppercase(
                Locale.getDefault()
            ) == "J" || firstLetter.uppercase(
                Locale.getDefault()
            ) == "K" || firstLetter.uppercase(Locale.getDefault()) == "L"
        ) {
            context.resources.getColor(R.color.label_text_color_i_l)
        } else if (firstLetter.uppercase(Locale.getDefault()) == "M" || firstLetter.uppercase(
                Locale.getDefault()
            ) == "N" || firstLetter.uppercase(Locale.getDefault()) == "O" || firstLetter.uppercase(
                Locale.getDefault()
            ) == "P"
        ) {
            context.resources.getColor(R.color.label_text_color_m_p)
        } else if (firstLetter.uppercase(Locale.getDefault()) == "Q" || firstLetter.uppercase(
                Locale.getDefault()
            ) == "R" || firstLetter.uppercase(Locale.getDefault()) == "S" || firstLetter.uppercase(
                Locale.getDefault()
            ) == "T"
        ) {
            context.resources.getColor(R.color.label_text_color_q_t)
        } else if (firstLetter.uppercase(Locale.getDefault()) == "U" || firstLetter.uppercase(
                Locale.getDefault()
            ) == "V" || firstLetter.uppercase(Locale.getDefault()) == "W" || firstLetter.uppercase(
                Locale.getDefault()
            ) == "X"
        ) {
            context.resources.getColor(R.color.label_text_color_u_x)
        } else if (firstLetter.uppercase(Locale.getDefault()) == "Y" || firstLetter.uppercase(
                Locale.getDefault()
            ) == "Z"
        ) {
            context.resources.getColor(R.color.label_text_color_y_z)
        } else {
            context.resources.getColor(R.color.label_text_color)
        }
    }

    fun parsedText(
        context: Context,
        inQueueForUploadDataModel: InQueueForUploadDataModel,
        supplierFilters: List<DetailSupplierDataModel>,
    ) {
        var parsedText = ""
        if (inQueueForUploadDataModel.isInvoiceSelectedIsImage) {
            val invoiceDataManager = InvoiceDataManager(context)
            val file = File(inQueueForUploadDataModel.imageFilePath)
            val deviceDimensions = getDeviceDimensions(context)
            val bitmap = invoiceDataManager.decodeSampledBitmapFromImageUri(
                Uri.fromFile(file),
                deviceDimensions.deviceWidth,
                deviceDimensions.deviceHeight
            )
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val image = InputImage.fromBitmap(bitmap!!, 0)
            val result = recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val test = visionText.text
                    var OrderId = ""
                    var lstSupplier: DetailSupplierDataModel? = null
                    if (!StringHelper.isStringNullOrEmpty(test)) {
                        val splitStr =
                            test.split("\\r?\\n".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                        for (value in splitStr) {
                            if ((value.lowercase(Locale.getDefault())
                                    .contains("order #") || value.lowercase(
                                    Locale.getDefault()
                                )
                                    .contains("order ref:") || value.contains("Your P/O No:") || value.contains(
                                    "Our P/O No:"
                                ) || value.startsWith("2021") || value.startsWith("2020") || value.startsWith(
                                    "2022"
                                )) && value.length == 12 || value.startsWith("#2021") || value.startsWith(
                                    "#2022"
                                ) || value.startsWith("#2020") || value.startsWith("*2021") || value.startsWith(
                                    "*2020"
                                ) || value.startsWith("*2022")
                            ) {
                                var index = 0
                                if (value.startsWith("#") || value.startsWith("*")) {
                                    index = 1
                                }
                                if (value.lowercase(Locale.getDefault()).contains("order #")) {
                                    index = 7
                                }
                                if (value.lowercase(Locale.getDefault()).contains("order ref:")) {
                                    index = 10
                                }
                                if (value.contains("Our P/O No:")) {
                                    index = 11
                                }
                                if (value.contains("Your P/O No:")) {
                                    index = 12
                                }
                                OrderId = value.substring(index)
                                Log.e("OrderId", "OrderId: $OrderId")
                                //                                                Toast.makeText(context, "Order Id: " + OrderId, Toast.LENGTH_LONG).show();
                                break
                            }
                        }
                        for (i in supplierFilters.indices) {
                            for (s in splitStr) {
                                if (s.lowercase(Locale.getDefault()).contains(
                                        supplierFilters[i].supplier.supplierId!!.lowercase(Locale.getDefault())
                                    ) || s.lowercase(
                                        Locale.getDefault()
                                    ).contains(
                                        supplierFilters[i].supplier.supplierName!!.lowercase(Locale.getDefault())
                                    )
                                ) {
                                    lstSupplier = supplierFilters[i]
                                    Log.e(
                                        "supplier",
                                        "Supplier: " + ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                            lstSupplier
                                        )
                                    )
                                    //                                                    Toast.makeText(context, "Supplier Name: " + lstSupplier.getSupplier().getSupplierName(), Toast.LENGTH_LONG).show();
                                    break
                                }
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(
                        "response",
                        "onFailure: " + e.message.toString()
                    )
                }
            if (result.isComplete && result.isSuccessful) {
                parsedText = result.result.text
            }
        } else {
            var OrderId = ""
            var lstSupplier: DetailSupplierDataModel? = null
            try {
                val reader = PdfReader(inQueueForUploadDataModel.imageFilePath)
                val n = reader.numberOfPages
                for (j in 0 until n) {
                    parsedText = """
                        $parsedText${
                        PdfTextExtractor.getTextFromPage(reader, j + 1).trim { it <= ' ' }
                    }
                        
                        """.trimIndent() //Extracting the content from the different pages
                }
                Log.e("response", "onSuccess: $parsedText")
                reader.close()
            } catch (e: Exception) {
                Log.e("response", "onFailure: " + e.message)
            }
            if (!StringHelper.isStringNullOrEmpty(parsedText)) {
                val splitStr = parsedText.split("\\r?\\n".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                for (value in splitStr) {
                    if ((value.lowercase(Locale.getDefault())
                            .contains("order #") || value.lowercase(
                            Locale.getDefault()
                        ).contains("order ref:") || value.contains("Our P/O No:") || value.contains(
                            "Your P/O No:"
                        ) || value.startsWith("2021") || value.startsWith("2020") || value.startsWith(
                            "2022"
                        )) && value.length == 12 || value.startsWith("#2021") || value.startsWith("#2022") || value.startsWith(
                            "#2020"
                        ) || value.startsWith("*2021") || value.startsWith("*2020") || value.startsWith(
                            "*2022"
                        )
                    ) {
                        var index = 0
                        if (value.startsWith("#") || value.startsWith("*")) {
                            index = 1
                        }
                        if (value.lowercase(Locale.getDefault()).contains("order #")) {
                            index = 7
                        }
                        if (value.lowercase(Locale.getDefault()).contains("order ref:")) {
                            index = 10
                        }
                        if (value.contains("Our P/O No:")) {
                            index = 11
                        }
                        if (value.contains("Your P/O No:")) {
                            index = 12
                        }
                        OrderId = value.substring(index)
                        Log.e("OrderId", "OrderId: $OrderId")
                        //                        Toast.makeText(context, "Order Id: " + OrderId, Toast.LENGTH_LONG).show();
                        break
                    }
                }
                for (i in supplierFilters.indices) {
                    for (s in splitStr) {
                        Log.e("supplier value", "parsedText: $s")
                        if (s.lowercase(Locale.getDefault()).contains(
                                supplierFilters[i].supplier.supplierId!!.lowercase(
                                    Locale.getDefault()
                                )
                            ) || s.lowercase(Locale.getDefault()).contains(
                                supplierFilters[i].supplier.supplierName!!.lowercase(Locale.getDefault())
                            )
                        ) {
                            lstSupplier = supplierFilters[i]
                            Log.e(
                                "supplier",
                                "Supplier: " + ZeemartBuyerApp.gsonExposeExclusive.toJson(
                                    lstSupplier
                                )
                            )
                            //                            Toast.makeText(context, "Supplier Name: " + lstSupplier.getSupplier().getSupplierName(), Toast.LENGTH_SHORT).show();
                            break
                        }
                    }
                }
            }
        }
    }

    fun SupplierThumbNailBackgroundColor(supplierName: String, context: Context): Int {
        val firstLetter = supplierName[0].toString()
        return if (firstLetter.uppercase(Locale.getDefault()) == "A" || firstLetter.uppercase(
                Locale.getDefault()
            ) == "B" || firstLetter.uppercase(
                Locale.getDefault()
            ) == "C" || firstLetter.uppercase(Locale.getDefault()) == "D"
        ) {
            context.resources.getColor(R.color.label_background_color_a_d)
        } else if (firstLetter.uppercase(Locale.getDefault()) == "E" || firstLetter.uppercase(
                Locale.getDefault()
            ) == "F" || firstLetter.uppercase(Locale.getDefault()) == "G" || firstLetter.uppercase(
                Locale.getDefault()
            ) == "H"
        ) {
            context.resources.getColor(R.color.label_background_color_e_h)
        } else if (firstLetter.uppercase(Locale.getDefault()) == "I" || firstLetter.uppercase(
                Locale.getDefault()
            ) == "J" || firstLetter.uppercase(
                Locale.getDefault()
            ) == "K" || firstLetter.uppercase(Locale.getDefault()) == "L"
        ) {
            context.resources.getColor(R.color.label_background_color_i_l)
        } else if (firstLetter.uppercase(Locale.getDefault()) == "M" || firstLetter.uppercase(
                Locale.getDefault()
            ) == "N" || firstLetter.uppercase(
                Locale.getDefault()
            ) == "O" || firstLetter.uppercase(Locale.getDefault()) == "P"
        ) {
            context.resources.getColor(R.color.label_background_color_m_p)
        } else if (firstLetter.uppercase(Locale.getDefault()) == "Q" || firstLetter.uppercase(
                Locale.getDefault()
            ) == "R" || firstLetter.uppercase(Locale.getDefault()) == "S" || firstLetter.uppercase(
                Locale.getDefault()
            ) == "T"
        ) {
            context.resources.getColor(R.color.label_background_color_q_t)
        } else if (firstLetter.uppercase(Locale.getDefault()) == "U" || firstLetter.uppercase(
                Locale.getDefault()
            ) == "V" || firstLetter.uppercase(Locale.getDefault()) == "W" || firstLetter.uppercase(
                Locale.getDefault()
            ) == "X"
        ) {
            context.resources.getColor(R.color.label_background_color_u_x)
        } else if (firstLetter.uppercase(Locale.getDefault()) == "Y" || firstLetter.uppercase(
                Locale.getDefault()
            ) == "Z"
        ) {
            context.resources.getColor(R.color.label_background_color_y_z)
        } else {
            context.resources.getColor(R.color.label_background_color)
        }
    }

    val commaSepratedBuyerList: String?
        get() {
            var outletIds = ""
            val buyerDetails = SharedPref.read(SharedPref.BUYER_DETAIL, "")
            return if (!StringHelper.isStringNullOrEmpty(buyerDetails)) {
                val mBuyerDetails = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    buyerDetails,
                    BuyerDetails::class.java
                )
                if (mBuyerDetails != null && mBuyerDetails.outlet != null && mBuyerDetails.outlet!!.size > 0) {
                    for (i in mBuyerDetails.outlet!!.indices) {
                        outletIds = outletIds + mBuyerDetails.outlet!![i].outletId + ","
                    }
                    outletIds = outletIds.substring(0, outletIds.length - 1)
                }
                outletIds
            } else {
                null
            }
        }
    val commaSepratedCompanyList: String?
        get() {
            var companyIds = ""
            val buyerDetails = SharedPref.read(SharedPref.BUYER_DETAIL, "")
            return if (!StringHelper.isStringNullOrEmpty(buyerDetails)) {
                val mBuyerDetails = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    buyerDetails,
                    BuyerDetails::class.java
                )
                if (mBuyerDetails != null && mBuyerDetails.userDetails != null && mBuyerDetails.userDetails!!.company != null && mBuyerDetails.userDetails!!.company!!.size > 0) {
                    for (i in mBuyerDetails.userDetails!!.company!!.indices) {
                        companyIds =
                            companyIds + mBuyerDetails.userDetails!!.company!![i].companyId + ","
                    }
                    companyIds = companyIds.substring(0, companyIds.length - 1)
                }
                companyIds
            } else {
                null
            }
        }

    fun getCommaSeperatedCuisineListList(lstCuisineType: List<OutletFuturesModel.CuisineType>?): String? {
        var companyIds = ""
        return if (lstCuisineType != null && lstCuisineType.size > 0) {
            for (i in lstCuisineType.indices) {
                companyIds = companyIds + lstCuisineType[i].name + ", "
            }
            companyIds = companyIds.substring(0, companyIds.length - 2)
            companyIds
        } else {
            null
        }
    }

    /**
     * get the outlet header for All the API requests
     *
     * @param isAllOutlet
     * @return
     */
    // deprecated
    fun getHeaderMapBasedOnOutlet(
        isAllOutlet: Boolean,
        selectedOutletId: String,
    ): Map<String?, String?>? {
        return if (isAllOutlet) {
            val outletIds = commaSepratedBuyerList
            if (outletIds != null) {
                val outletHeader: MutableMap<String?, String?> =
                    HashMap<String?, String?>()
                outletHeader[ServiceConstant.REQUEST_BODY_BUYER_HEADER] = outletIds
                outletHeader
            } else {
                null
            }
        } else {
            if (StringHelper.isStringNullOrEmpty(selectedOutletId)) {
                val outletIds = commaSepratedBuyerList
                val outletHeader: MutableMap<String?, String?> =
                    HashMap<String?, String?>()
                outletHeader[ServiceConstant.REQUEST_BODY_BUYER_HEADER] = outletIds
                outletHeader
            } else {
                val outletHeader: MutableMap<String?, String?> =
                    HashMap<String?, String?>()
                outletHeader[ServiceConstant.REQUEST_BODY_BUYER_HEADER] = selectedOutletId
                outletHeader
            }
        }
    }

    // get header map for all outlets assigned to user
    val headerAllOutlets: Map<String, String>?
        get() {
            val outletIds = commaSepratedBuyerList
            val outletHeader: MutableMap<String?, String?> = HashMap<String?, String?>()
            outletHeader[ServiceConstant.REQUEST_BODY_BUYER_HEADER] = outletIds
            return outletHeader as Map<String, String>?
        }

    // get header map for given outlets
    fun getHeaderFromOutlets(outletList: List<Outlet>?): Map<String, String>? {
        if (outletList == null || outletList.size == 0) {
            return null
        }
        val outletIds: MutableList<String?> = ArrayList()
        for (o in outletList) {
            outletIds.add(o.outletId)
        }
        val selectedOutletIdsCsv = TextUtils.join(",", outletIds)
        val outletHeader: MutableMap<String?, String?> = HashMap<String?, String?>()
        outletHeader[ServiceConstant.REQUEST_BODY_BUYER_HEADER] = selectedOutletIdsCsv
        return outletHeader as Map<String, String>?
    }

    //    public static final String getListOfOutletsIds(String[] inputArray) {
    //        if (inputArray != null && inputArray.length > 0) {
    //            String orderIds = "";
    //            for (int i = 0; i < inputArray.length; i++) {
    //                orderIds = orderIds + inputArray[i] + ",";
    //            }
    //
    //            orderIds = orderIds.substring(0, orderIds.length() - 1);
    //            orderIds = "[" + orderIds + "]";
    //            return orderIds;
    //
    //        } else {
    //            return null;
    //        }
    //    }
    @JvmStatic
    fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }

    /**
     * @param context
     * @return
     */
    fun checkInternetConnection(context: Context): Boolean {
        val ConnectionManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = ConnectionManager.activeNetworkInfo
        return if (networkInfo != null && networkInfo.isConnected) {
            true
        } else false
    }

    @JvmStatic
    fun hideKeyboard(activity: Activity) {
        val view = activity.findViewById<View>(android.R.id.content)
        if (view != null) {
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun getDeviceDimensions(context: Context): DeviceDimensionUI {
        val displayMetrics = context.resources.displayMetrics
        val deviceDimension = DeviceDimensionUI()
        deviceDimension.deviceHeight = displayMetrics.heightPixels
        deviceDimension.deviceWidth = displayMetrics.widthPixels
        return deviceDimension
    }

    /**
     * hide the swipe refresh loader
     *
     * @param swipeRefreshLayout
     */
    @JvmStatic
    fun hideSwipeRefreshLoader(swipeRefreshLayout: SwipeRefreshLayout) {
        try {
            val f = swipeRefreshLayout.javaClass.getDeclaredField("mCircleView")
            f.isAccessible = true
            val img = f[swipeRefreshLayout] as ImageView
            img.alpha = 0.0f
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        }
    }

    fun getFileExtension(uri: Uri?, context: Context): String? {
        if (uri != null) {
            val cR = context.contentResolver
            val mime = MimeTypeMap.getSingleton()
            val type = mime.getExtensionFromMimeType(cR.getType(uri))
            Log.d("MIME Type", type + "*****" + cR.getType(uri))
            return type
        }
        return ""
    }

    /**
     * Force logout user
     *
     * @param context
     */
    fun clearLogout(context: Context) {
        Intercom.client().logout()
        SharedPref.clearAllSharedPreferencesData()
        val newIntent = Intent(context, BuyerLoginActivity::class.java)
        newIntent.putExtra(ZeemartAppConstants.CALLED_FROM, ZeemartAppConstants.CALLED_FROM_LOGOUT)
        newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(newIntent)
    }

    /**
     * unregister the user for push notifications and logs out
     *
     * @param context
     */
    fun unregisterLogout(context: Context?) {
        getInstance(context)!!.getRequestQueue()!!.cancelAll { true }
        if (!SharedPref.read(SharedPref.USER_ID, "").equals("")) {
            registerDeviceUserLogsOut(context,
                SharedPref.read(SharedPref.NOTIFICATION_DEVICE_TOKEN, "")!!,
                SharedPref.read(SharedPref.USER_ID, "")!!,
                object : GetRequestStatusResponseListener {
                    override fun onSuccessResponse(status: String?) {
                        clearLogout(context!!)
                    }

                    override fun onErrorResponse(error: VolleyErrorHelper?) {
                        clearLogout(context!!)
                    }
                })
        } else {
            clearLogout(context!!)
        }
    }

    @JvmStatic
    fun setFontForTabs(tabLayout: TabLayout) {
        val vg = tabLayout.getChildAt(0) as ViewGroup
        val tabsCount = vg.childCount
        for (j in 0 until tabsCount) {
            val vgTab = vg.getChildAt(j) as ViewGroup
            val tabChildCount = vgTab.childCount
            for (i in 0 until tabChildCount) {
                val tabViewChild = vgTab.getChildAt(i)
                if (tabViewChild is TextView) {
                    val typeface = Typeface.createFromAsset(
                        context.assets,
                        "fonts/" + ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
                    )
                    tabViewChild.setTypeface(typeface)
                }
            }
        }
    }

    /***
     * To prevent from multiple click on an item.
     */
    @JvmStatic
    fun avoidMultipleClicks(view: View?) {
        val DELAY_IN_MS: Long = 1000
        if (!view!!.isClickable) {
            return
        }
        view.isClickable = false
        view.postDelayed({ if (view != null) view.isClickable = true }, DELAY_IN_MS)
    }

    fun askAppReview(activity: Activity?) {
        val manager = ReviewManagerFactory.create(activity!!)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We can get the ReviewInfo object
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(
                    activity, reviewInfo
                )
                flow.addOnCompleteListener { }
            }
        }
    }

    val localIpAddress: String
        get() {
            try {
                val en = NetworkInterface.getNetworkInterfaces()
                while (en.hasMoreElements()) {
                    val intf = en.nextElement()
                    val enumIpAddr = intf.inetAddresses
                    while (enumIpAddr.hasMoreElements()) {
                        val inetAddress = enumIpAddr.nextElement()
                        if (!inetAddress.isLoopbackAddress) {
                            return Formatter.formatIpAddress(inetAddress.hashCode())
                        }
                    }
                }
            } catch (ex: SocketException) {
            }
            return ""
        }
}