package zeemart.asia.buyers.network

import android.content.Context
import com.android.volley.Request
import com.android.volley.VolleyError
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.fromJson
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.RetrieveCompaniesResponse
import zeemart.asia.buyers.models.RetrieveCompaniesResponse.SpecificCompany

object CompaniesApi {
    @JvmStatic
    fun retrieveCompanies(
        context: Context?,
        apiParamsHelper: ApiParamsHelper,
        outlets: List<Outlet>?,
        listener: CompaniesResponseListener
    ) {
        var url = ServiceConstant.ENDPOINT_RETRIEVE_COMPANIES
        url = apiParamsHelper.getUrl(url)
        val zeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            url,
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            { response ->
                val companiesData =
                    fromJson(response, RetrieveCompaniesResponse.Response::class.java)
                listener.onSuccessResponse(companiesData)
            }) { error -> listener.onErrorResponse(error) }
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(zeemartAPIRequest)
    }

    @JvmStatic
    fun retrieveSpecificCompany(
        context: Context?,
        apiParamsHelper: ApiParamsHelper,
        outlets: List<Outlet>?,
        listener: SpecificCompanyResponseListener
    ) {
        var url = ServiceConstant.ENDPOINT_RETRIEVE_SPECIFIC_COMPANY
        url = apiParamsHelper.getUrl(url)
        val zeemartAPIRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            url,
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            { response ->
                val companiesData = fromJson(response, SpecificCompany::class.java)
                listener.onSuccessResponse(companiesData)
            }) { error -> listener.onErrorResponse(error) }
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(zeemartAPIRequest)
    }

    fun uploadCompanyVerifyImage(
        context: Context?,
        companyId: String?,
        jsonRequest: String?,
        listener: GetRequestStatusResponseListener
    ) {
        var uri = ServiceConstant.ENDPOINT_UPDATE_COMPANIES
        val apiParamsHelper = ApiParamsHelper()
        val outlets = SharedPref.allOutlets
        apiParamsHelper.setCompanyId(companyId!!)
        uri = apiParamsHelper.getUrl(uri)
        val createNewDealOrderRequest = ZeemartAPIRequest(
            context,
            Request.Method.PUT,
            uri,
            jsonRequest,
            CommonMethods.getHeaderFromOutlets(outlets),
            { response ->
                if (!StringHelper.isStringNullOrEmpty(response)) {
                    listener.onSuccessResponse(response)
                }
            }) { error -> listener.onErrorResponse(error as VolleyErrorHelper) }
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(createNewDealOrderRequest)
    }

    interface CompaniesResponseListener {
        fun onSuccessResponse(response: RetrieveCompaniesResponse.Response?)
        fun onErrorResponse(error: VolleyError?)
    }

    interface SpecificCompanyResponseListener {
        fun onSuccessResponse(response: SpecificCompany?)
        fun onErrorResponse(error: VolleyError?)
    }
}