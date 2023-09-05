package zeemart.asia.buyers.network

import android.content.Context
import com.android.volley.Request
import com.android.volley.VolleyError
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.fromJson
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.userAgreement.Agreements
import zeemart.asia.buyers.models.userAgreement.ValidateAgreements

/**
 * Created by RajPrudhvi on 15/06/2020.
 */
object UserAgreement {
    fun retrieveAndValidateAgreement(
        context: Context?,
        listener: ValidateAgreementResponseListener,
        outlets: List<Outlet>?
    ) {
        val endpointUrl = ServiceConstant.ENDPOINT_RETRIEVE_AGREEMENTS
        val addFavRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            endpointUrl,
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            { response ->
                if (!StringHelper.isStringNullOrEmpty(response)) {
                    val retrieveAgreement = fromJson(response, Agreements::class.java)
                    if (retrieveAgreement != null && retrieveAgreement.data != null && retrieveAgreement.data!!.buyer != null && retrieveAgreement.data!!.buyer!!.current != null && retrieveAgreement.data!!.buyer!!.current!!.term != null) {
                        if (!StringHelper.isStringNullOrEmpty(retrieveAgreement.data!!.buyer!!.current!!.policy!!.agreementId) && !StringHelper.isStringNullOrEmpty(
                                retrieveAgreement.data!!.buyer!!.current!!.term!!.agreementId
                            )
                        ) {
                            val url = ServiceConstant.ENDPOINT_VALIDATE_AGREEMENT
                            val apiParamsHelper = ApiParamsHelper()
                            apiParamsHelper.setTermAgreementId(retrieveAgreement.data!!.buyer!!.current!!.term!!.agreementId!!)
                            apiParamsHelper.setPolicyAgreementId(retrieveAgreement.data!!.buyer!!.current!!.policy!!.agreementId!!)
                            apiParamsHelper.setClientType(ZeemartAppConstants.BUYER)
                            val getEssentials = ZeemartAPIRequest(
                                context,
                                Request.Method.POST,
                                apiParamsHelper.getUrl(url),
                                null,
                                CommonMethods.getHeaderFromOutlets(outlets),
                                { response ->
                                    if (!StringHelper.isStringNullOrEmpty(response)) {
                                        val retrieveValidateAgreement =
                                            fromJson(response, ValidateAgreements::class.java)
                                        listener.onSuccessResponse(
                                            retrieveValidateAgreement,
                                            retrieveAgreement
                                        )
                                    }
                                }) { listener.onErrorResponse() }
                            VolleyRequest.Companion.getInstance(context)!!
                                .addToRequestQueue<String>(getEssentials)
                        }
                    } else {
                        listener.onErrorResponse()
                    }
                }
            }) { listener.onErrorResponse() }
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(addFavRequest)
    }

    fun acceptAgreement(
        context: Context?,
        retrieveAgreement: ValidateAgreements,
        mListener: AcceptAgreementListener
    ) {
        val url = ServiceConstant.ENDPOINT_ACCEPT_AGREEMENT
        val apiParamsHelper = ApiParamsHelper()
        for (i in retrieveAgreement.data!!.indices) {
            if (retrieveAgreement.data!![i].agreementType == "Policy") {
                apiParamsHelper.setPolicyAgreementId(retrieveAgreement.data!![i].agreementId!!)
            } else {
                apiParamsHelper.setTermAgreementId(retrieveAgreement.data!![i].agreementId!!)
            }
        }
        apiParamsHelper.setPlatForm(ZeemartAppConstants.ANDROID)
        apiParamsHelper.setClientType(ZeemartAppConstants.BUYER)
        val getEssentials = ZeemartAPIRequest(
            context,
            Request.Method.PUT,
            apiParamsHelper.getUrl(url),
            null,
            CommonMethods.getHeaderFromOutlets(SharedPref.allOutlets!!),
            { response -> mListener.onSuccessResponse(response) }) { error ->
            mListener.onErrorResponse(
                error
            )
        }
        VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(getEssentials)
    }

    interface ValidateAgreementResponseListener {
        fun onSuccessResponse(validateAgreements: ValidateAgreements?, agreements: Agreements?)
        fun onErrorResponse()
    }

    interface AcceptAgreementListener {
        fun onErrorResponse(error: VolleyError?)
        fun onSuccessResponse(response: String?)
    }
}