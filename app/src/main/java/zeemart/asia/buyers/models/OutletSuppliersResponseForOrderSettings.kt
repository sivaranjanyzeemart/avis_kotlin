package zeemart.asia.buyers.modelsimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.models.*

class OutletSuppliersResponseForOrderSettings {
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
    var data: List<OutletSuppliersResponseForOrderSettings.Datum>? = null

    inner class Datum {
        @SerializedName("outlet")
        @Expose
        var outlet: Outlet? = null

        @Expose
        var isDisplaySuppliersList = false

        @SerializedName("supplier")
        @Expose
        var suppliers: List<Supplier>? = null
    }
}