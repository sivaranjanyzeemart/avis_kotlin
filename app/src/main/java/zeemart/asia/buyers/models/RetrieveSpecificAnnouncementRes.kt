package zeemart.asia.buyers.modelsimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.*

/**
 * Created by RajPrudhviMarella on 06/20/2019.
 */
class RetrieveSpecificAnnouncementRes : BaseResponse() {
    @SerializedName("data")
    @Expose
    var data: Announcements? = null

    inner class Announcements {
        @SerializedName("announcementId")
        @Expose
        var announcementId: String? = null

        @SerializedName("publishNow")
        @Expose
        var publishNow: Boolean? = null

        @SerializedName("timeToBePublished")
        @Expose
        var timeToBePublished: Long? = null

        @SerializedName("timeToBeExpired")
        @Expose
        var timeToBeExpired: Long? = null

        @SerializedName("market")
        @Expose
        var market: String? = null

        @SerializedName("isRead")
        @Expose
        var isRead: Boolean? = null

        @SerializedName("imageURL")
        @Expose
        var imageURL: String? = null

        @SerializedName("title")
        @Expose
        var title: String? = null

        @SerializedName("shortDescription")
        @Expose
        var shortDescription: String? = null

        @SerializedName("fullDescription")
        @Expose
        var fullDescription: String? = null

        @SerializedName("callToAction")
        @Expose
        var callToAction: CallToAction? = null

        @SerializedName("status")
        @Expose
        var status: String? = null

        @SerializedName("createdBy")
        @Expose
        var createdBy: CreatedBy? = null

        inner class CreatedBy {
            @SerializedName("id")
            @Expose
            var id: String? = null

            @SerializedName("firstName")
            @Expose
            var firstName: String? = null

            @SerializedName("lastName")
            @Expose
            var lastName: String? = null

            @SerializedName("imageURL")
            @Expose
            var imageURL: String? = null

            @SerializedName("phone")
            @Expose
            var phone: String? = null
        }

        inner class CallToAction {
            @SerializedName("externalLink")
            @Expose
            var externalLink: String? = null

            @SerializedName("buttonLabel")
            @Expose
            var buttonLabel: String? = null

            @SerializedName("gotoLink")
            @Expose
            var gotoLink: String? = null

            @SerializedName("gotoButtonLabel")
            @Expose
            var gotoButtonLabel: String? = null
        }
    }
}