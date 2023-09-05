package zeemart.asia.buyers.models.invoiceimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.PriceDetails
import zeemart.asia.buyers.models.Supplier
import zeemart.asia.buyers.modelsimport.RetrieveSpecificAnnouncementRes

/**
 * Created by ParulBhandari on 2/12/2018.
 */
class PriceUpdateModelBaseResponse {
    @SerializedName("data")
    @Expose
    var data: MutableList<PriceDetailModel>? = null

    class PriceDetailModel {
        @SerializedName("status")
        @Expose
        var status: String? = null

        @SerializedName("outlet")
        @Expose
        var outlet: Outlet? = null

        @SerializedName("supplier")
        @Expose
        var supplier: Supplier? = null

        @SerializedName("sku")
        @Expose
        var sku: String? = null

        @SerializedName("productName")
        @Expose
        var productName: String? = null

        @SerializedName("unitSize")
        @Expose
        var unitSize: String? = null

        @SerializedName("quantity")
        @Expose
        var quantity: Double? = null

        @SerializedName("invoiceDate")
        @Expose
        var invoiceDate: Long? = null

        @SerializedName("invoiceId")
        @Expose
        var invoiceId: String? = null

        @SerializedName("invoiceNum")
        @Expose
        var invoiceNum: String? = null

        @SerializedName("isAdminHubUpdate")
        @Expose
        var isAdminHubUpdate = false

        @SerializedName("updatedBy")
        @Expose
        var updatedBy: RetrieveSpecificAnnouncementRes.Announcements.CreatedBy? = null

        @SerializedName("discount")
        @Expose
        var discount: PriceDetails? = null

        @SerializedName("totalUnitPrice")
        @Expose
        var totalUnitPrice: PriceDetails? = null

        @SerializedName("unitPrice")
        @Expose
        var unitPrice: PriceDetails? = null

        @SerializedName("prevUnitPrice")
        @Expose
        var prevUnitPrice: PriceDetails? = null
    }
}