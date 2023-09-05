package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by saiful on 1/6/18.
 */
open class Pagination {
    @SerializedName("numberOfPages")
    @Expose
    var numberOfPages: Int? = null

    @SerializedName("numberOfRecords")
    @Expose
    var numberOfRecords: Int? = null

    @SerializedName("currentPageNumber")
    @Expose
    var currentPageNumber: Int? = null
}