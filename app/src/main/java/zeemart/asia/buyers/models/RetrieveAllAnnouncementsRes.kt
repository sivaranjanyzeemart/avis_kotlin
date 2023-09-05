package zeemart.asia.buyers.modelsimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.*

/**
 * Created by RajPrudhviMarella on 06/20/2019.
 */
class RetrieveAllAnnouncementsRes {
    class Response : BaseResponse() {
        @SerializedName("data")
        @Expose
        var data: RetrieveAllAnnouncementsRes.ResponseWithPaginationData? = null
    }

    class ResponseWithPaginationData : Pagination() {
        @SerializedName("announcements")
        @Expose
        var announcementsList: List<RetrieveSpecificAnnouncementRes.Announcements>? = null
    }
}