package zeemart.asia.buyers.helper

import android.content.Context
import com.android.volley.Request
import com.android.volley.VolleyError
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.fromJson
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.helper.DateHelper.StartDateEndDate
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.OutletSpendingHistoryDataListener
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.invoice.Invoice.ExportCsvUrlData
import zeemart.asia.buyers.models.reportsimport.ReportResponse.*
import zeemart.asia.buyers.models.reportsimportimport.ReportSpendingDataModel
import zeemart.asia.buyers.network.GetOutletSpendingHistoryData
import zeemart.asia.buyers.network.VolleyErrorHelper
import zeemart.asia.buyers.network.VolleyRequest.Companion.getInstance
import zeemart.asia.buyers.network.ZeemartAPIRequest
import zeemart.asia.buyers.reports.ReportTimePeriod
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by ParulBhandari on 2/9/2018.
 */
object ReportApi {
    const val SKU_SUPPLIER_NAME = "SKU_SUPPLIER_NAME"
    const val REPORT_SEARCH_ACTIVITY = "REPORT_SEARCH_ACTIVITY"
    const val TOTAL_SPENDING_DATA = "TotalSpendingData"
    const val SPENDING_DATA_PERIOD = "SpendingDataPeriod"
    const val START_DATE = "START_DATE"
    const val END_DATE = "END_DATE"
    const val TOTAL_SPENDING_DETAILS_CALLED_FOR = "TOTAL_SPENDING_DETAILS_CALLED_FOR"
    const val CALLED_FROM_FRAGMENT = "CALLED_FROM_FRAGMENT"
    const val TOTAL_SPENDING_CATEGORY_FRAGMENT = "TOTAL_SPENDING_CATEGORY_FRAGMENT"
    const val TOTAL_SPENDING_TAG_FRAGMENT = "TOTAL_SPENDING_TAG_FRAGMENT"
    const val TOTAL_SPENDING_SUPPLIER_FRAGMENT = "TOTAL_SPENDING_SUPPLIER_FRAGMENT"
    const val REPORT_SPENDING_DETAIL_ACTIVITY = "REPORT_SPENDING_DETAIL_ACTIVITY"
    const val SKU_NAME = "SKU_NAME"
    const val TOTAL_SPENDING_SKU_FRAGMENT = "TOTAL_SPENDING_SKU_FRAGMENT"
    const val DASHBOARD_WEEK_FRAGMENT = "DASHBOARD_WEEK_FRAGMENT"
    const val ALL_PRICE_UPDATE_ACTIVITY = "ALL_PRICE_UPDATE_ACTIVITY"
    const val START_END_DATE_RANGE = "START_END_DATE_RANGE"
    const val SUPPLIER_NAME = "SUPPLIER_NAME"
    const val SUPPLIER_ID = "SUPPLIER_ID"
    const val CATEGORY_NAME = "CATEGORY_NAME"
    const val TAG = "TAG"
    fun daysBetweenDates(dateType: String, startDate: String?, endDate: String?): Int {
        var days = 0
        if (dateType == ReportTimePeriod.THISWEEK.timePeriodValue) {
            days = 7
        } else if (dateType == ReportTimePeriod.LASTWEEK.timePeriodValue) {
            days = 7
        } else if (dateType == ReportTimePeriod.LASTTOLASTWEEK.timePeriodValue) {
            days = 7
        } else if (dateType == ReportTimePeriod.THISMONTH.timePeriodValue) {
            val startDateMonth = Calendar.getInstance(DateHelper.marketTimeZone)
            startDateMonth[Calendar.DAY_OF_MONTH] = 1
            DateHelper.setCalendarStartOfDayTime(startDateMonth)
            val endDateMonth = Calendar.getInstance(DateHelper.marketTimeZone)
            endDateMonth.add(Calendar.MONTH, 1)
            endDateMonth[Calendar.DAY_OF_MONTH] = 1
            DateHelper.setCalendarStartOfDayTime(endDateMonth)
            days = TimeUnit.MILLISECONDS.toDays(
                Math.abs(endDateMonth.timeInMillis - startDateMonth.timeInMillis)
            ).toInt()
        } else if (dateType == ReportTimePeriod.LASTMONTH.timePeriodValue) {
            val startDateMonth = Calendar.getInstance(DateHelper.marketTimeZone)
            startDateMonth.add(Calendar.MONTH, -1)
            startDateMonth[Calendar.DAY_OF_MONTH] = 1
            DateHelper.setCalendarStartOfDayTime(startDateMonth)
            val endDateMonth = Calendar.getInstance(DateHelper.marketTimeZone)
            endDateMonth[Calendar.DAY_OF_MONTH] = 1
            DateHelper.setCalendarStartOfDayTime(endDateMonth)
            days = TimeUnit.MILLISECONDS.toDays(
                Math.abs(endDateMonth.timeInMillis - startDateMonth.timeInMillis)
            ).toInt()
        } else if (dateType == ReportTimePeriod.LASTTOLASTMONTH.timePeriodValue) {
            val startDateMonth = Calendar.getInstance(DateHelper.marketTimeZone)
            startDateMonth.add(Calendar.MONTH, -2)
            startDateMonth[Calendar.DAY_OF_MONTH] = 1
            DateHelper.setCalendarStartOfDayTime(startDateMonth)
            val endDateMonth = Calendar.getInstance(DateHelper.marketTimeZone)
            endDateMonth.add(Calendar.MONTH, -1)
            endDateMonth[Calendar.DAY_OF_MONTH] = 1
            DateHelper.setCalendarStartOfDayTime(endDateMonth)
            days = TimeUnit.MILLISECONDS.toDays(
                Math.abs(endDateMonth.timeInMillis - startDateMonth.timeInMillis)
            ).toInt()
        } else if (dateType == ReportTimePeriod.CUSTOM.timePeriodValue) {
            val startDateLong = DateHelper.returnEpochTimeSOD(startDate)
            val endDateLong = DateHelper.returnEpochTimeEOD(endDate)
            val startDateCustom = Calendar.getInstance(DateHelper.marketTimeZone)
            startDateCustom.timeInMillis = startDateLong * 1000
            DateHelper.setCalendarStartOfDayTime(startDateCustom)
            val endDateCustom = Calendar.getInstance(DateHelper.marketTimeZone)
            endDateCustom.timeInMillis = endDateLong * 1000
            endDateCustom.add(Calendar.DAY_OF_MONTH, 1)
            DateHelper.setCalendarStartOfDayTime(endDateCustom)
            days = TimeUnit.MILLISECONDS.toDays(
                Math.abs(endDateCustom.timeInMillis - startDateCustom.timeInMillis)
            ).toInt()
        }
        return days
    }

    //   api id - 35.2 Retrieve Spendings (GroupBy Supplier or Outlet)
    fun retrieveSpendings(
        context: Context?,
        apiParamsHelper: ApiParamsHelper,
        listener: TotalSpendingSupplierResponseListener,
    ) {
        /*ApiParamsHelper apiParamsHelper = new ApiParamsHelper();
        apiParamsHelper.setStartDate(startDateLong);
        apiParamsHelper.setEndDate(endDateLong);
        apiParamsHelper.setGroupBy(ApiParamsHelper.GroupBy.SUPPLIER);*/
        var uri = ServiceConstant.ENDPOINT_REPORT_TOTAL_SPENDING
        uri = apiParamsHelper.getUrl(uri)
        val outlets: MutableList<Outlet> = ArrayList()
        outlets.add(SharedPref.defaultOutlet!!)
        val totalSpendingBySupplierRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            uri,
            null,
            CommonMethods.getHeaderFromOutlets(outlets) as Map<String, String>,
            { response ->
                val reportResponseData = fromJson(response, TotalSpendingBySupplier::class.java)
                listener.onTotalSpendingResponseSuccess(reportResponseData)
            }) { error -> listener.onTotalSpendingResponseError(error) }
        getInstance(context)!!.addToRequestQueue(totalSpendingBySupplierRequest)
    }

    /**
     * get fixed time period values
     * this week, last week, last to last week
     * this month, last month, last to last month
     *
     * @param reportTimePeriod
     * @return
     */
    fun getDateRange(reportTimePeriod: String): StartDateEndDate? {
        var startDateEndDate: StartDateEndDate? = null
        if (reportTimePeriod.equals(ReportTimePeriod.THISWEEK.timePeriodValue, ignoreCase = true)) {
            startDateEndDate = DateHelper.getWeekStartEndDate(DateHelper.WeekMonth.CURRENT_WEEK)
        } else if (reportTimePeriod.equals(
                ReportTimePeriod.LASTWEEK.timePeriodValue,
                ignoreCase = true
            )
        ) {
            startDateEndDate = DateHelper.getWeekStartEndDate(DateHelper.WeekMonth.LAST_WEEK)
        } else if (reportTimePeriod.equals(
                ReportTimePeriod.LASTTOLASTWEEK.timePeriodValue,
                ignoreCase = true
            )
        ) {
            startDateEndDate =
                DateHelper.getWeekStartEndDate(DateHelper.WeekMonth.LAST_TO_LAST_WEEK)
        } else if (reportTimePeriod.equals(
                ReportTimePeriod.THISMONTH.timePeriodValue,
                ignoreCase = true
            )
        ) {
            startDateEndDate = DateHelper.getMonthStartEndDate(DateHelper.WeekMonth.CURRENT_MONTH)
        } else if (reportTimePeriod.equals(
                ReportTimePeriod.LASTMONTH.timePeriodValue,
                ignoreCase = true
            )
        ) {
            startDateEndDate = DateHelper.getMonthStartEndDate(DateHelper.WeekMonth.LAST_MONTH)
        } else if (reportTimePeriod.equals(
                ReportTimePeriod.LASTTOLASTMONTH.timePeriodValue,
                ignoreCase = true
            )
        ) {
            startDateEndDate =
                DateHelper.getMonthStartEndDate(DateHelper.WeekMonth.LAST_TO_LAST_MONTH)
        }
        return startDateEndDate
    }

    //   api id - 35.4 Retrieve Category Spendings
    fun retrieveCategorySpendings(
        context: Context?,
        startDateLong: Long,
        endDateLong: Long,
        listener: TotalSpendingByCategoryResponseListener,
    ) {
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setStartDate(startDateLong)
        apiParamsHelper.setEndDate(endDateLong)
        var uri = ServiceConstant.ENDPOINT_REPORT_CATEGORY_SPENDING
        uri = apiParamsHelper.getUrl(uri)
        val outlets: MutableList<Outlet> = ArrayList()
        outlets.add(SharedPref.defaultOutlet!!)
        val totalSpendingBySupplierRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            uri,
            null,
            CommonMethods.getHeaderFromOutlets(outlets) as Map<String, String>,
            { response ->
                val reportResponseData = fromJson(response, TotalSpendingByCategory::class.java)
                listener.onTotalSpendingResponseSuccess(reportResponseData)
            }) { error -> listener.onTotalSpendingResponseError(error) }
        getInstance(context)!!.addToRequestQueue(totalSpendingBySupplierRequest)
    }

    //   api id - 35.3 Retrieve Sku Spendings
    fun retrieveSkuSpendings(
        context: Context?,
        startDateLong: Long,
        endDateLong: Long,
        suppliedId: String?,
        category: String?,
        listener: TotalSpendingBySkuResponseListener,
    ) {
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setStartDate(startDateLong)
        apiParamsHelper.setEndDate(endDateLong)
        if (!StringHelper.isStringNullOrEmpty(suppliedId)) apiParamsHelper.setSupplierId(suppliedId!!)
        if (!StringHelper.isStringNullOrEmpty(category)) apiParamsHelper.setCategory(category!!)
        var uri = ServiceConstant.ENDPOINT_REPORT_SKU_SPENDING
        uri = apiParamsHelper.getUrl(uri)
        val outlets: MutableList<Outlet> = ArrayList()
        outlets.add(SharedPref.defaultOutlet!!)
        val totalSpendingBySkuRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            uri,
            null,
            CommonMethods.getHeaderFromOutlets(outlets) as Map<String, String>,
            { response ->
                val reportResponseData = fromJson(response, TotalSpendingBySku::class.java)
                listener.onTotalSpendingResponseSuccess(reportResponseData)
            }) { error -> listener.onTotalSpendingResponseError(error) }
        getInstance(context)!!.addToRequestQueue(totalSpendingBySkuRequest)
    }

    fun retrieveTagsSpending(
        context: Context?,
        startDateLong: Long,
        endDateLong: Long,
        listener: TotalSpendingByTagsResponseListener,
    ) {
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setStartDate(startDateLong)
        apiParamsHelper.setEndDate(endDateLong)
        if (SharedPref.defaultOutlet != null && !StringHelper.isStringNullOrEmpty(SharedPref.defaultOutlet!!.outletId)) apiParamsHelper.setOutletId(
            SharedPref.defaultOutlet!!.outletId!!
        )
        var uri = ServiceConstant.ENDPOINT_REPORT_TAGS_SPENDING
        uri = apiParamsHelper.getUrl(uri)
        val outlets: MutableList<Outlet> = ArrayList()
        outlets.add(SharedPref.defaultOutlet!!)
        val totalSpendingBySkuRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            uri,
            null,
            CommonMethods.getHeaderFromOutlets(outlets) as Map<String, String>,
            { response ->
                val reportResponseData = fromJson(response, TotalSpendingByTags::class.java)
                listener.onTotalSpendingResponseSuccess(reportResponseData)
            }) { error -> listener.onTotalSpendingResponseError(error) }
        getInstance(context)!!.addToRequestQueue(totalSpendingBySkuRequest)
    }

    fun retrieveTagsDetailsSpending(
        context: Context?,
        startDateLong: Long,
        endDateLong: Long,
        tag: String?,
        listener: TotalSpendingByTagDetailResponseListener,
    ) {
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setStartDate(startDateLong)
        apiParamsHelper.setEndDate(endDateLong)
        apiParamsHelper.setTag(tag!!)
        if (SharedPref.defaultOutlet != null && !StringHelper.isStringNullOrEmpty(SharedPref.defaultOutlet!!.outletId)) apiParamsHelper.setOutletId(
            SharedPref.defaultOutlet!!.outletId!!
        )
        var uri = ServiceConstant.ENDPOINT_REPORT_TAGS_SPENDING_DETAILS
        uri = apiParamsHelper.getUrl(uri)
        val outlets: MutableList<Outlet> = ArrayList()
        outlets.add(SharedPref.defaultOutlet!!)
        val totalSpendingBySkuRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            uri,
            null,
            CommonMethods.getHeaderFromOutlets(outlets) as Map<String, String>,
            { response ->
                val reportResponseData = fromJson(response, TotalSpendingByTagDetail::class.java)
                listener.onTotalSpendingResponseSuccess(reportResponseData)
            }) { error -> listener.onTotalSpendingResponseError(error) }
        getInstance(context)!!.addToRequestQueue(totalSpendingBySkuRequest)
    }

    fun retrieveTagsDetailsChatSpending(
        context: Context?,
        apiParamsHelper: ApiParamsHelper,
        listener: TotalSpendingByTagDetailChartResponseListener,
    ) {
        if (SharedPref.defaultOutlet != null && !StringHelper.isStringNullOrEmpty(SharedPref.defaultOutlet!!.outletId)) apiParamsHelper.setOutletId(
            SharedPref.defaultOutlet!!.outletId!!
        )
        var uri = ServiceConstant.ENDPOINT_REPORT_TAGS_SPENDING_DETAILS
        uri = apiParamsHelper.getUrl(uri)
        val outlets: MutableList<Outlet> = ArrayList()
        outlets.add(SharedPref.defaultOutlet!!)
        val totalSpendingBySkuRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            uri,
            null,
            CommonMethods.getHeaderFromOutlets(outlets) as Map<String, String>,
            { response ->
                val reportResponseData =
                    fromJson(response, TotalSpendingByTagDetailChart::class.java)
                listener.onTotalSpendingResponseSuccess(reportResponseData)
            }) { error -> listener.onTotalSpendingResponseError(error) }
        getInstance(context)!!.addToRequestQueue(totalSpendingBySkuRequest)
    }

    //   api id - 35.6 Retrieve Total Spendings
    fun retrieveTotalSpendings(
        context: Context?,
        apiParamsHelper: ApiParamsHelper,
        listener: TotalSpendingDetailByRangeResponseListener,
    ) {
        var uri = ServiceConstant.ENDPOINT_REPORT_TOTAL_SPENDING_RANGE
        uri = apiParamsHelper.getUrl(uri)
        val outlets: MutableList<Outlet> = ArrayList()
        outlets.add(SharedPref.defaultOutlet!!)
        val totalSpendingByRangeRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            uri,
            null,
            CommonMethods.getHeaderFromOutlets(outlets) as Map<String, String>,
            { response ->
                val reportResponseData = fromJson(response, TotalSpendingDetailsByRange::class.java)
                listener.onTotalSpendingResponseSuccess(reportResponseData)
            }) { error -> listener.onTotalSpendingResponseError(error) }
        getInstance(context)!!.addToRequestQueue(totalSpendingByRangeRequest)
    }

    fun callTotalSpendingDeprecatedSaveDataInSharedPrefs(
        context: Context?,
        startDate: Long,
        endDate: Long,
    ) {
        GetOutletSpendingHistoryData(context!!, object : OutletSpendingHistoryDataListener {
            override fun onSuccessResponse(spendingDataModel: ReportSpendingDataModel?) {
                if (context != null && spendingDataModel?.spendings != null) {
                    val spendingDataJson =
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(spendingDataModel.spendings)
                    SharedPref.write(TOTAL_SPENDING_DATA, spendingDataJson)
                }
            }

            override fun onErrorResponse(error: VolleyErrorHelper?) {}
        }).getOutletSpendingData(startDate, endDate, true, SharedPref.defaultOutlet!!)
    }

    //   api id - 35.11 Invoice Raw data of list invoice Excel Download
    fun getCSVRawOrListOfInvoicesUrl(
        context: Context?,
        startDateEndDate: StartDateEndDate,
        isDetailedData: Boolean,
        listener: ExportCstDataListener,
    ) {
        var uri = ServiceConstant.ENDPOINT_REPORT_EXPORT_CSV_LIST_OF_INVOICES_AND_RAW_DATA
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setInvoiceStartDate(startDateEndDate.startDateMillis)
        apiParamsHelper.setInvoiceEndDate(startDateEndDate.endDateMillis)
        apiParamsHelper.setIsDetailed(isDetailedData)
        apiParamsHelper.setSortBy(ApiParamsHelper.SortField.INVOICE_DATE)
        uri = apiParamsHelper.getUrl(uri)
        val outlets: MutableList<Outlet> = ArrayList()
        outlets.add(SharedPref.defaultOutlet!!)
        val zeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            uri,
            null,
            CommonMethods.getHeaderFromOutlets(outlets) as Map<String, String>,
            { response ->
                val exportCsvUrlData = fromJson(response, ExportCsvUrlData::class.java)
                listener.onExportCsvDataResponseSuccess(exportCsvUrlData)
            }) { error -> listener.onExportCsvDataResponseError(error) }
        getInstance(context)!!.addToRequestQueue(zeemartAPIRequest)
    }

    //   api id - 35.12 Summary data for invoices Excel Download
    fun summaryDataForInvoicesExcelDownload(
        context: Context?,
        startDateEndDate: StartDateEndDate,
        listener: ExportCstDataListener,
    ) {
        var uri = ServiceConstant.ENDPOINT_REPORT_EXPORT_CSV_SUMMARY
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setInvoiceStartDate(startDateEndDate.startDateMillis)
        apiParamsHelper.setInvoiceEndDate(startDateEndDate.endDateMillis)
        apiParamsHelper.setSortBy(ApiParamsHelper.SortField.INVOICE_DATE)
        uri = apiParamsHelper.getUrl(uri)
        val outlets: MutableList<Outlet> = ArrayList()
        outlets.add(SharedPref.defaultOutlet!!)
        val zeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            uri,
            null,
            CommonMethods.getHeaderFromOutlets(outlets) as Map<String, String>,
            { response ->
                val exportCsvUrlData = fromJson(response, ExportCsvUrlData::class.java)
                listener.onExportCsvDataResponseSuccess(exportCsvUrlData)
            }) { error -> listener.onExportCsvDataResponseError(error) }
        getInstance(context)!!.addToRequestQueue(zeemartAPIRequest)
    }

    interface TotalSpendingSupplierResponseListener {
        fun onTotalSpendingResponseSuccess(reportResponseData: TotalSpendingBySupplier?)
        fun onTotalSpendingResponseError(error: VolleyError?)
    }

    interface TotalSpendingByCategoryResponseListener {
        fun onTotalSpendingResponseSuccess(reportResponseData: TotalSpendingByCategory?)
        fun onTotalSpendingResponseError(error: VolleyError?)
    }

    interface TotalSpendingBySkuResponseListener {
        fun onTotalSpendingResponseSuccess(reportResponseData: TotalSpendingBySku?)
        fun onTotalSpendingResponseError(error: VolleyError?)
    }

    interface TotalSpendingByTagsResponseListener {
        fun onTotalSpendingResponseSuccess(reportResponseData: TotalSpendingByTags?)
        fun onTotalSpendingResponseError(error: VolleyError?)
    }

    interface TotalSpendingByTagDetailResponseListener {
        fun onTotalSpendingResponseSuccess(reportResponseData: TotalSpendingByTagDetail?)
        fun onTotalSpendingResponseError(error: VolleyError?)
    }

    interface TotalSpendingByTagDetailChartResponseListener {
        fun onTotalSpendingResponseSuccess(reportResponseData: TotalSpendingByTagDetailChart?)
        fun onTotalSpendingResponseError(error: VolleyError?)
    }

    interface TotalSpendingDetailByRangeResponseListener {
        fun onTotalSpendingResponseSuccess(reportResponseData: TotalSpendingDetailsByRange?)
        fun onTotalSpendingResponseError(error: VolleyError?)
    }

    interface ExportCstDataListener {
        fun onExportCsvDataResponseSuccess(reportResponseData: ExportCsvUrlData?)
        fun onExportCsvDataResponseError(error: VolleyError?)
    }
}