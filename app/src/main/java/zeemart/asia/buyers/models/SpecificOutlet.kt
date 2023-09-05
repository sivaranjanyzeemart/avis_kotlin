package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class SpecificOutlet {
    @SerializedName("data")
    @Expose
    var data: Data? = null

    inner class Data {
        @SerializedName("outletId")
        @Expose
        var outletId: String? = null

        @SerializedName("status")
        @Expose
        var status: String? = null

        @SerializedName("name")
        @Expose
        var name: String? = null

        @SerializedName("company")
        @Expose
        var company: Company? = null

        @SerializedName("phone")
        @Expose
        var phone: String? = null

        @SerializedName("email")
        @Expose
        var email: String? = null

        @SerializedName("market")
        @Expose
        var market: String? = null

        @SerializedName("logoURL")
        @Expose
        var logoURL: String? = null

        @SerializedName("invoiceEmailId")
        @Expose
        var invoiceEmailId: String? = null

        @SerializedName("updatedStockCount")
        @Expose
        var updatedStockCount: Boolean? = null

        @SerializedName("settings")
        @Expose
        var settings: OutletSettingModel? = null

        @SerializedName("subscriptionSettings")
        @Expose
        var subscriptionSettings: SubscriptionSettings? = null

        @SerializedName("posIntegration")
        @Expose
        var posIntegration: PosIntegration? = null
    }

    class Plan {
        @SerializedName("subscriptionType")
        @Expose
        var subscriptionType: String? = null
    }

    class PosIntegration {
        @SerializedName("posIntegration")
        @Expose
        var posIntegration: Boolean? = null

        @SerializedName("vendorName")
        @Expose
        var vendorName: String? = null
    }

    class SubscriptionSettings {
        @SerializedName("sgGovtGrant")
        @Expose
        var sgGovtGrant: Boolean? = null

        @SerializedName("subscriptionEmails")
        @Expose
        var subscriptionEmails: String? = null

        @SerializedName("status")
        @Expose
        var status: String? = null

        @SerializedName("plan")
        @Expose
        var plan: Plan? = null
    }

    inner class Company {
        @SerializedName("companyId")
        @Expose
        var companyId: String? = null
    }

    enum class SubscriptionStatus(val statusName: String) {
        ACTIVE("Active"), SUSPENDED("Suspended");

        override fun toString(): String {
            return statusName
        }
    }

    enum class SubscriptionType(val typeName: String) {
        STARTER("Starter"), GROWTH("Growth"), PREMIUM("Premium");

        override fun toString(): String {
            return typeName
        }
    }
}