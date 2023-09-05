package zeemart.asia.buyers.modelsimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.*
import zeemart.asia.buyers.models.RetrieveSpecificPromoCode.PromoCodes

class RetrieveMultiplePromoCodes {
    class Response : BaseResponse() {
        @SerializedName("data")
        @Expose
        var data: ResponseWithPaginationData? = null
    }

    class ResponseWithPaginationData : Pagination() {
        @SerializedName("promoCodes")
        @Expose
        var promoCodeList: List<PromoCodes>? = null
    }
}