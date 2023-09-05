package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class RetrieveCompaniesResponse {
    class Response : BaseResponse() {
        @SerializedName("data")
        @Expose
        var data: ResponseWithPaginationData? = null
    }

    class ResponseWithPaginationData : Pagination() {
        @SerializedName("data")
        @Expose
        var companies: List<Companies>? = null
    }

    class SpecificCompany {
        @SerializedName("data")
        @Expose
        var data: Companies? = null
    }
}