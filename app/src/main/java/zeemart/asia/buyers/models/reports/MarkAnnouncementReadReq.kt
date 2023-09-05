package zeemart.asia.buyers.models.reportsimportimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class MarkAnnouncementReadReq {
    @SerializedName("announcementIds")
    @Expose
    var announcementIds: List<String>? = null
}