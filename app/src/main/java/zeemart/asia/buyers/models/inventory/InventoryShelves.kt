package zeemart.asia.buyers.models.inventoryimportimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.PriceDetails
import zeemart.asia.buyers.models.UserDetails


class InventoryShelves {
    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("dateCreated")
    @Expose
    var dateCreated: String? = null

    @SerializedName("dateUpdated")
    @Expose
    var dateUpdated: String? = null

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

    @SerializedName("shelveId")
    @Expose
    var shelveId: String? = null

    @SerializedName("shelveName")
    @Expose
    var shelveName: String? = null

    @SerializedName("outletId")
    @Expose
    var outletId: String? = null

    @SerializedName("summary")
    @Expose
    var summary: InventoryShelves.Summary? = null

    @SerializedName("countDate")
    @Expose
    var countDate: Long? = null

    @SerializedName("savedDraftTimeCreated")
    @Expose
    var savedDraftTimeCreated: Long? = null

    @SerializedName("isSavedDraftStockCount")
    @Expose
    var isSavedDraftStockCount = false

    class Summary {
        @SerializedName("productsInShelve")
        @Expose
        var productsInShelve: Int? = null

        @SerializedName("productsBelowParLevel")
        @Expose
        var productsBelowParLevel: Int? = null

        @SerializedName("productsOutOfStockWithNoPar")
        @Expose
        var productsOutOfStock = 0

        @SerializedName("lastEstimatedValue")
        @Expose
        var lastEstimatedValue: PriceDetails? = null

        @SerializedName("timeLastCounted")
        @Expose
        var timeLastCounted = 0L

        @SerializedName("dateLastCounted")
        @Expose
        var dateLastCounted: String? = null
    }
}