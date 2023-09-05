package zeemart.asia.buyers.modelsimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.orderimport.DealsBaseResponse


/**
 * Created by RajPrudhviMarella on 9/15/2020.
 */
class DealsResponseForOnBoarding {
    @SerializedName("data")
    @Expose
    var data: DealsBaseResponse? = null
}