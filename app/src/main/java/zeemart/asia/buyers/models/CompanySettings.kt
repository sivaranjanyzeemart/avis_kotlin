package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CompanySettings {
    @SerializedName("payments")
    @Expose
    var payments: CompanySettingsPayments? = null
}