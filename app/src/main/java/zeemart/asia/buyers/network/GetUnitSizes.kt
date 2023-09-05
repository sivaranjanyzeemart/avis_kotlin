package zeemart.asia.buyers.network

import android.content.Context
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.fromJsonList
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.interfaces.UnitSizeResponseListener
import zeemart.asia.buyers.models.UnitSizeModel

/**
 * Created by ParulBhandari on 12/18/2017.
 */
class GetUnitSizes     //get the map for the response array
    (private val context: Context, private val unitSizeResponseListener: UnitSizeResponseListener) {
    // api id - 43.3 Retrieve UnitSizes
    val unitSizeMap: Unit
        get() {
            val stringRequest =
                StringRequest(Request.Method.GET, ServiceConstant.ENDPOINT_UIT_SIZE, { response ->
                    val unitSizeDetails = fromJsonList(
                        response,
                        UnitSizeModel::class.java,
                        object : TypeToken<List<UnitSizeModel?>?>() {}.type
                    )
                    SharedPref.write(
                        SharedPref.UNIT_SIZE_LIST,
                        ZeemartBuyerApp.gsonExposeExclusive.toJson(unitSizeDetails)
                    )
                    //get the map for the response array
                    val unitSizeMap = getUnitSizeResponseMap(unitSizeDetails)
                    unitSizeResponseListener.onSuccessResponse(unitSizeMap)
                }) { error -> unitSizeResponseListener.onErrorResponse(error) }
            VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<String>(stringRequest)
        }

    /**
     * get the Map for the unit sizes
     */
    fun getUnitSizeResponseMap(unitSizeDetails: List<UnitSizeModel>?): Map<String?, UnitSizeModel> {
        val unitSizeMap: MutableMap<String?, UnitSizeModel> = HashMap()
        for (i in unitSizeDetails!!.indices) {
            unitSizeMap[unitSizeDetails[i].unitSize] = unitSizeDetails[i]
        }
        return unitSizeMap
    }
}