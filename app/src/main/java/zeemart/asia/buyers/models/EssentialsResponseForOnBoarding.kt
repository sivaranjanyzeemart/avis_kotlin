package zeemart.asia.buyers.modelsimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.EssentialsBaseResponse

/**
 * Created by RajPrudhviMarella on 9/15/2020.
 */
class EssentialsResponseForOnBoarding {
    @SerializedName("data")
    @Expose
    var data: EssentialsBaseResponse? = null
}