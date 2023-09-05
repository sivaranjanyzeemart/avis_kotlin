package zeemart.asia.buyers.models.marketlist

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.Outlet
import zeemart.asia.buyers.models.Pagination
import zeemart.asia.buyers.models.Supplier

/**
 * Created by ParulBhandari on 12/19/2017.
 */
open class DetailSupplierDataModel {
    @SerializedName("timeCreated")
    @Expose
    var timeCreated: Int? = null

    @SerializedName("timeUpdated")
    @Expose
    var timeUpdated: Int? = null

    @SerializedName("outlet")
    @Expose
    var outlet: Outlet? = null

    @SerializedName("supplier")
    @Expose
    lateinit var supplier: Supplier

    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("customOutletId")
    @Expose
    var customOutletId: String? = null

    @SerializedName("customBillingId")
    @Expose
    var customBillingId: String? = null

    @SerializedName("lastOrdered")
    @Expose
    var lastOrdered: Int? = null

    @SerializedName("disableOrderingReason")
    @Expose
    var disableOrderingReason: String? = null

    @SerializedName("useDefault")
    @Expose
    var useDefault: Boolean? = null

    @SerializedName("orderDisabled")
    @Expose
    var orderDisabled: Boolean? = null

    @SerializedName("disableCancel")
    @Expose
    var disableCancel: Boolean? = null

    @SerializedName("isFavourite")
    @Expose
    private val isFavourite: Boolean? = null

    @SerializedName("odpId")
    @Expose
    var odpId: String? = null

    @SerializedName("deliveryDates")
    @Expose
    var deliveryDates: List<DeliveryDate>? = null

    @SerializedName("deliveryFeePolicy")
    @Expose
    var deliveryFeePolicy: DeliveryFeePolicy? = null

    //Added to display favorite option on top of supplier list when on browse screen for new order
    @Expose
    var isFavoriteProducts = false

    @Expose
    var isSupplierSelected = false

    @Expose
    var isFillToPar = false
    fun isOrderDisabled(): Boolean {
        return orderDisabled!!
    }

    fun setOrderDisabled(orderDisabled: Boolean) {
        this.orderDisabled = orderDisabled
    }

    override fun hashCode(): Int {
        //if for some reason order does not have a supplier details in it(staging data issue)
        return if (supplier.supplierId != null) {
            supplier.supplierId.hashCode()
        } else {
            1
        }
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is DetailSupplierDataModel) {
            false
        } else {
            this.supplier.supplierId == other.supplier.supplierId
        }
    }

    inner class SupplierPagination : Pagination() {
        @Expose
        @SerializedName("suppliers")
        var suppliers: List<DetailSupplierDataModel>? = null
    }

    inner class SupplierResponseForDeliveryPreferences {
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

        @Expose
        @SerializedName("data")
        var suppliers: List<DetailSupplierDataModel>? = null
    }
}