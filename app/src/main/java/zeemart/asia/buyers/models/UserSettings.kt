package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class UserSettings {
    @SerializedName("inventorySettings")
    @Expose
    var inventorySettings: UserInventorySettings? = null
}