package zeemart.asia.buyers.models.marketlist

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.helper.StringHelper

/**
 * Created by ParulBhandari on 3/29/2018.
 */
class Inventory {
    @SerializedName("allowNegative")
    @Expose
    var allowNegative: Boolean? = null

    @SerializedName("status")
    @Expose
    var status: String? = null

    enum class Status(private val inventoryStatus: String) {
        ACTIVE("ACTIVE"), INACTIVE("INACTIVE");

        override fun toString(): String {
            return inventoryStatus
        }
    }

    fun isStatus(inventoryStatus: Status): Boolean {
        return if (!StringHelper.isStringNullOrEmpty(status)) {
            status == inventoryStatus.toString()
        } else false
    }
}