package zeemart.asia.buyers.models.orderimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.BaseResponse
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.Supplier
import zeemart.asia.buyers.models.UserDetails
import zeemart.asia.buyers.models.marketlist.Product
import zeemart.asia.buyers.models.orderimportimport.OrderAmount

class PlaceDraftOrdersResponse : BaseResponse() {
    @SerializedName("data")
    @Expose
    var data: List<OrdersData>? = null

    class OrdersData {
        enum class Status(val value: String) {
            FAILURE("FAILURE"), SUCCESS("SUCCESS");

            override fun toString(): String {
                return value
            }
        }

        @SerializedName("status")
        @Expose
        var status: String? = null

        @SerializedName("data")
        @Expose
        var data: PlaceDraftOrdersResponse.Data? = null

        @SerializedName("errors")
        @Expose
        var errors: List<PlaceDraftOrdersResponse.Error>? = null
        fun isStatus(status: PlaceDraftOrdersResponse.OrdersData.Status): Boolean {
            return this.status == status.toString()
        }
    }

    inner class Data {
        @SerializedName("dateCreated")
        @Expose
        var dateCreated: Long? = null

        @SerializedName("dateUpdated")
        @Expose
        var dateUpdated: Long? = null

        @SerializedName("timeCreated")
        @Expose
        var timeCreated: Long? = null

        @SerializedName("timeUpdated")
        @Expose
        var timeUpdated: Long? = null

        @SerializedName("createdBy")
        @Expose
        var createdBy: UserDetails? = null

        @SerializedName("updatedBy")
        @Expose
        var updatedBy: UserDetails? = null

        @SerializedName("orderId")
        @Expose
        var orderId: String? = null

        @SerializedName("outlet")
        @Expose
        var outlet: Outlet? = null

        @SerializedName("supplier")
        @Expose
        var supplier: Supplier? = null

        @SerializedName("amount")
        @Expose
        var amount: OrderAmount? = null

        @SerializedName("orderStatus")
        @Expose
        var orderStatus: String? = null

        @SerializedName("products")
        @Expose
        var products: List<Product>? = null

        @SerializedName("approvers")
        @Expose
        var approvers: List<UserDetails>? = null

        @SerializedName("timeDrafted")
        @Expose
        var timeDrafted: Long? = null

        @SerializedName("draftedBy")
        @Expose
        var draftedBy: UserDetails? = null

        @SerializedName("notes")
        @Expose
        var notes: String? = null

        @SerializedName("promoCode")
        @Expose
        var promoCode: String? = null

        @SerializedName("id")
        @Expose
        var id: String? = null

        @SerializedName("timeCutOff")
        @Expose
        var timeCutOff: Long? = null

        @SerializedName("timeDelivered")
        @Expose
        var timeDelivered: Long? = null

        @SerializedName("dateDelivered")
        @Expose
        var dateDelivered: Long? = null
    }

    class Error {
        enum class ErrorCode(val value: String) {
            REQUEST_VALIDATION_ERROR("REQUEST_VALIDATION_ERROR"), ENTRY_NOT_FOUND_ERROR("ENTRY_NOT_FOUND_ERROR");

            override fun toString(): String {
                return value
            }
        }

        @SerializedName("errorCode")
        @Expose
        var errorCode: String? = null

        @SerializedName("message")
        @Expose
        var message = ""
        fun isErrorCode(errorCodePassed: PlaceDraftOrdersResponse.Error.ErrorCode): Boolean {
            return errorCode == errorCodePassed.toString()
        }
    }
}