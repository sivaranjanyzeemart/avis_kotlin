package zeemart.asia.buyers.network

import android.content.Context
import com.android.volley.Request
import com.google.gson.JsonSyntaxException
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.models.OrderPayments.PaymentBankAccountDetails
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.paymentimport.LinkedSupplierPreference
import zeemart.asia.buyers.models.paymentimport.PayInvoiceResponse
import zeemart.asia.buyers.models.paymentimport.PaymentCardDetails
import zeemart.asia.buyers.models.paymentimportimport.PayInvoiceRequest
import zeemart.asia.buyers.models.paymentimportimport.PayOrderRequest

class PaymentApi {
    interface CompanyCardDetailsListener {
        fun onSuccessResponse(paymentCardDetails: PaymentCardDetails?)
        fun onErrorResponse(error: VolleyErrorHelper?)
    }

    interface CompanyPreferenceListener {
        fun onSuccessResponse(linkedSupplierPreference: LinkedSupplierPreference?)
        fun onErrorResponse(error: VolleyErrorHelper?)
    }

    interface PayInvoiceListener {
        fun onSuccessResponse(payInvoice: PayInvoiceResponse?)
        fun onErrorResponse(error: VolleyErrorHelper?)
    }

    interface RetrieveAccountDetailsListener {
        fun onSuccessResponse(payInvoice: PaymentBankAccountDetails?)
        fun onErrorResponse(error: VolleyErrorHelper?)
    }

    interface StripeTokenListener {
        fun onSuccessResponse(payInvoice: PaymentBankAccountDetails?)
        fun onErrorResponse(error: VolleyErrorHelper?)
    }

    fun getStripeToken(apiParamsHelper: ApiParamsHelper?, context: Context?) {}

    companion object {
        //   api id - 31.4 Retrieve Company Card Payment Data
        @JvmStatic
        fun retrieveCompanyCardPaymentData(
            context: Context?,
            apiParamsHelper: ApiParamsHelper,
            outlet: Outlet,
            listener: CompanyCardDetailsListener
        ) {
            var uri: String = ServiceConstant.ENDPOINT_COMPANY_CARD_PAYMENT_DETAILS
            uri = apiParamsHelper.getUrl(uri)
            val outlets: MutableList<Outlet> = ArrayList<Outlet>()
            outlets.add(outlet)
            val getCompanyCardDetails = ZeemartAPIRequest(
                context,
                Request.Method.GET,
                uri,
                null,
                CommonMethods.getHeaderFromOutlets(outlets),
                { response ->
                    try {
                        val paymentCardDetails: PaymentCardDetails? =
                            ZeemartBuyerApp.fromJson(response, PaymentCardDetails::class.java)
                        listener.onSuccessResponse(paymentCardDetails)
                    } catch (e: JsonSyntaxException) {
                        e.printStackTrace()
                    }
                }) { error -> listener.onErrorResponse(error as VolleyErrorHelper) }
            VolleyRequest.Companion.getInstance(context)!!
                .addToRequestQueue<String>(getCompanyCardDetails)
        }

        //   api id - 18.1 View Linked Supplier Preference - (For Buyer)
        fun viewLinkedSupplierPreference(
            context: Context?,
            apiParamsHelper: ApiParamsHelper,
            outlet: Outlet,
            listener: CompanyPreferenceListener
        ) {
            var uri: String = ServiceConstant.ENDPOINT_COMPANY_PREFERENCE_LINKED_SUPPLIER
            uri = apiParamsHelper.getUrl(uri)
            val outlets: MutableList<Outlet> = ArrayList<Outlet>()
            outlets.add(outlet)
            val getCompanyPreference = ZeemartAPIRequest(
                context,
                Request.Method.GET,
                uri,
                null,
                CommonMethods.getHeaderFromOutlets(outlets),
                { response ->
                    try {
                        val linkedSupplierPreference: LinkedSupplierPreference? =
                            ZeemartBuyerApp.fromJson(response, LinkedSupplierPreference::class.java)
                        listener.onSuccessResponse(linkedSupplierPreference)
                    } catch (e: JsonSyntaxException) {
                        e.printStackTrace()
                    }
                }) { error -> listener.onErrorResponse(error as VolleyErrorHelper) }
            VolleyRequest.Companion.getInstance(context)!!
                .addToRequestQueue<String>(getCompanyPreference)
        }

        //   api id - 30.1 Pay Invoices
        @JvmStatic
        fun payInvoice(
            context: Context?,
            payInvoiceRequest: PayInvoiceRequest?,
            outlet: Outlet,
            listener: PayInvoiceListener
        ) {
            val uri: String = ServiceConstant.ENDPOINT_PAY_INVOICES
            val jsonRequestBody: String =
                ZeemartBuyerApp.gsonExposeExclusive.toJson(payInvoiceRequest)
            val outlets: MutableList<Outlet> = ArrayList<Outlet>()
            outlets.add(outlet)
            val getPayInvoiceRequest = ZeemartAPIRequest(
                context,
                Request.Method.POST,
                uri,
                jsonRequestBody,
                CommonMethods.getHeaderFromOutlets(outlets),
                { response ->
                    try {
                        val payInvoice: PayInvoiceResponse? =
                            ZeemartBuyerApp.fromJson(response, PayInvoiceResponse::class.java)
                        listener.onSuccessResponse(payInvoice)
                    } catch (e: JsonSyntaxException) {
                        e.printStackTrace()
                    }
                }) { error -> listener.onErrorResponse(error as VolleyErrorHelper) }
            VolleyRequest.Companion.getInstance(context)!!
                .addToRequestQueue<String>(getPayInvoiceRequest)
        }

        @JvmStatic
        fun payOrder(
            context: Context?,
            payOrderRequest: PayOrderRequest?,
            apiParamsHelper: ApiParamsHelper,
            outlet: Outlet,
            listener: PayInvoiceListener
        ) {
            val uri: String = apiParamsHelper.getUrl(ServiceConstant.ENDPOINT_PAY_ORDERS)
            val jsonRequestBody: String =
                ZeemartBuyerApp.gsonExposeExclusive.toJson(payOrderRequest)
            val outlets: MutableList<Outlet> = ArrayList<Outlet>()
            outlets.add(outlet)
            val getPayInvoiceRequest = ZeemartAPIRequest(
                context,
                Request.Method.POST,
                uri,
                jsonRequestBody,
                CommonMethods.getHeaderFromOutlets(outlets),
                { response ->
                    try {
                        val payInvoice: PayInvoiceResponse? =
                            ZeemartBuyerApp.fromJson(response, PayInvoiceResponse::class.java)
                        listener.onSuccessResponse(payInvoice)
                    } catch (e: JsonSyntaxException) {
                        e.printStackTrace()
                    }
                }) { error -> listener.onErrorResponse(error as VolleyErrorHelper) }
            VolleyRequest.Companion.getInstance(context)!!
                .addToRequestQueue<String>(getPayInvoiceRequest)
        }

        @JvmStatic
        fun setPaymentCardDefault(
            context: Context?,
            requestBody: String?,
            setPaymentCardDefaultResponseListener: GetRequestStatusResponseListener
        ) {
            val zeemartAPIRequest = ZeemartAPIRequest(
                context,
                Request.Method.PUT,
                ServiceConstant.ENDPOINT_PAYMENT_CARD_DEFAULT,
                requestBody,
                CommonMethods.headerAllOutlets,
                { response -> setPaymentCardDefaultResponseListener.onSuccessResponse(response) }) { error ->
                setPaymentCardDefaultResponseListener.onErrorResponse(
                    error as VolleyErrorHelper
                )
            }
            VolleyRequest.Companion.getInstance(context)!!
                .addToRequestQueue<String>(zeemartAPIRequest)
        }

        @JvmStatic
        fun addPaymentCard(
            context: Context?,
            requestBody: String?,
            setPaymentCardDefaultResponseListener: GetRequestStatusResponseListener
        ) {
            val zeemartAPIRequest = ZeemartAPIRequest(
                context,
                Request.Method.POST,
                ServiceConstant.ENDPOINT_PAYMENT_ADD_CARD,
                requestBody,
                CommonMethods.headerAllOutlets,
                { response -> setPaymentCardDefaultResponseListener.onSuccessResponse(response) }) { error ->
                setPaymentCardDefaultResponseListener.onErrorResponse(
                    error as VolleyErrorHelper
                )
            }
            VolleyRequest.Companion.getInstance(context)!!
                .addToRequestQueue<String>(zeemartAPIRequest)
        }

        //Retrieve Bank Transfer/Paynow Account Details
        @JvmStatic
        fun retrieveBankAccountDetails(
            context: Context?,
            outlet: Outlet,
            listener: RetrieveAccountDetailsListener
        ) {
            val apiParamsHelper = ApiParamsHelper()
            apiParamsHelper.setOutletId(outlet.outletId!!)
            var uri: String = ServiceConstant.ENDPOINT_RETRIEVE_BANK_TRANSFER_ACCOUNT_DETAILS
            uri = apiParamsHelper.getUrl(uri)
            val outlets: MutableList<Outlet> = ArrayList<Outlet>()
            outlets.add(outlet)
            val getBTAccountDetails = ZeemartAPIRequest(
                context,
                Request.Method.GET,
                uri,
                null,
                CommonMethods.getHeaderFromOutlets(outlets),
                { response ->
                    try {
                        val paymentBankAccountDetails: PaymentBankAccountDetails? =
                            ZeemartBuyerApp.fromJson<PaymentBankAccountDetails>(
                                response,
                                PaymentBankAccountDetails::class.java
                            )
                        listener.onSuccessResponse(paymentBankAccountDetails)
                    } catch (e: JsonSyntaxException) {
                        e.printStackTrace()
                    }
                }) { error -> listener.onErrorResponse(error as VolleyErrorHelper) }
            VolleyRequest.Companion.getInstance(context)!!
                .addToRequestQueue<String>(getBTAccountDetails)
        }
    }
}