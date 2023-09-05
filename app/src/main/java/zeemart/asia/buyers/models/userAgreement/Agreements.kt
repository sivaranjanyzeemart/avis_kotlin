package zeemart.asia.buyers.models.userAgreement

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.Supplier

/**
 * Created by RajPrudhvi on 15/06/2020.
 */
class Agreements {
    @SerializedName("path")
    @Expose
    var path: String? = null

    @SerializedName("timestamp")
    @Expose
    var timestamp: String? = null

    @SerializedName("status")
    @Expose
    var status: Int? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("data")
    @Expose
    var data: Data? = null

    inner class Data {
        @SerializedName("buyer")
        @Expose
        var buyer: Buyer? = null

        @SerializedName("supplier")
        @Expose
        var supplier: Supplier? = null
    }

    inner class Buyer {
        @SerializedName("current")
        @Expose
        var current: Current? = null
    }

    inner class Current {
        @SerializedName("term")
        @Expose
        var term: Term? = null

        @SerializedName("policy")
        @Expose
        var policy: Policy? = null
    }

    inner class Policy {
        @SerializedName("status")
        @Expose
        var status: String? = null

        @SerializedName("version")
        @Expose
        var version: Double? = null

        @SerializedName("effectiveFrom")
        @Expose
        var effectiveFrom: Int? = null

        @SerializedName("accountType")
        @Expose
        var accountType: String? = null

        @SerializedName("files")
        @Expose
        var files: List<String>? = null

        @SerializedName("agreementType")
        @Expose
        var agreementType: String? = null

        @SerializedName("agreementId")
        @Expose
        var agreementId: String? = null
    }

    inner class Term {
        @SerializedName("status")
        @Expose
        var status: String? = null

        @SerializedName("version")
        @Expose
        var version: Double? = null

        @SerializedName("effectiveFrom")
        @Expose
        var effectiveFrom: Int? = null

        @SerializedName("accountType")
        @Expose
        var accountType: String? = null

        @SerializedName("files")
        @Expose
        var files: List<String>? = null

        @SerializedName("agreementType")
        @Expose
        var agreementType: String? = null

        @SerializedName("agreementId")
        @Expose
        var agreementId: String? = null
    }
}