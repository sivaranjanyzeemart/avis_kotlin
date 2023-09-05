package zeemart.asia.buyers.models.marketlist

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class DeliveryDate {
    @SerializedName("deliveryDate")
    @Expose
    var deliveryDate: Long? = null

    @SerializedName("cutOffDate")
    @Expose
    var cutOffDate: Long? = null

    @SerializedName("isPH")
    @Expose
    var isPH = false

    @SerializedName("isEvePH")
    @Expose
    var isEvePH = false
    var dateSelected = false

    companion object {
        @JvmStatic
        fun GetSelectedDate(list: List<DeliveryDate>): DeliveryDate? {
            for (item in list) {
                if (item.dateSelected) {
                    if (item.isPH) {
                        return item
                    } else if (item.isEvePH) {
                        return item
                    }
                }
            }
            return null
        }
    }
}