package zeemart.asia.buyers.network

import android.content.Context
import com.android.volley.Request
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.helper.ApiParamsHelper
import zeemart.asia.buyers.helper.CommonMethods
import zeemart.asia.buyers.interfaces.ProductDetailsListener
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.ProductAttributeModel
import zeemart.asia.buyers.models.ProductDetailModel
import zeemart.asia.buyers.modelsimport.ProductAttributeSerializer

/**
 * Created by ParulBhandari on 1/23/2018.
 */
object ProductsApi {
    //   api id - 11.6 Get a Specific Product
    @JvmStatic
    fun getSpecificProduct(
        context: Context?,
        productSKU: String?,
        outletId: String,
        listener: ProductDetailsListener
    ) {
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setSku(productSKU!!)
        val uri: String = apiParamsHelper.getUrl(ServiceConstant.ENDPOINT_PRODUCT_DETAIL)
        val outlet = Outlet()
        outlet.outletId = outletId
        val outlets: MutableList<Outlet> = ArrayList<Outlet>()
        outlets.add(outlet)
        val getProductDetailRequest = ZeemartAPIRequest(
            context,
            Request.Method.GET,
            uri,
            null,
            CommonMethods.getHeaderFromOutlets(outlets),
            { response ->
                val gson: Gson = GsonBuilder().registerTypeAdapter(
                    ProductAttributeModel::class.java,
                    ProductAttributeSerializer()
                ).create()
                val productDetailData: ProductDetailModel =
                    gson.fromJson<ProductDetailModel>(response, ProductDetailModel::class.java)
                listener.onSuccessResponse(productDetailData)
            }) { error -> listener.onErrorResponse(error as VolleyErrorHelper) }
        VolleyRequest.Companion.getInstance(context)!!
            .addToRequestQueue<String>(getProductDetailRequest)
    }
}