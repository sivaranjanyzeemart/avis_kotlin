package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.marketlist.Settings

class Supplier {
    @SerializedName("supplierId")
    @Expose
    var supplierId: String? = null

    @SerializedName("supplierName")
    @Expose
    var supplierName: String? = null

    @SerializedName("logoURL")
    @Expose
    var logoURL: String? = null

    @SerializedName("shortDesc")
    @Expose
    var shortDesc: String? = null

    @Expose
    var settings: Settings? = null
    var isSupplierSelected = false
    override fun hashCode(): Int {
        return supplierId.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is Supplier) {
            false
        } else {
            supplierId == other.supplierId
        }
    }
}