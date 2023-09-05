package zeemart.asia.buyers.models.orderimportimport

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class OrderSummary {
    @SerializedName("currMonthOrderCount")
    @Expose
    var currMonthOrderCount: Int? = null

    @SerializedName("currWeekOrderCount")
    @Expose
    var currWeekOrderCount: Int? = null

    @SerializedName("lastMonthOrderCount")
    @Expose
    var lastMonthOrderCount: Int? = null

    @SerializedName("lastWeekOrderCount")
    @Expose
    var lastWeekOrderCount: Int? = null

    @SerializedName("todayPendingDeliveries")
    @Expose
    var todayPendingDeliveries: Int? = null

    @SerializedName("totalSpendingCurrDay")
    @Expose
    var totalSpendingCurrDay = 0.0

    @SerializedName("totalSpendingCurrMonth")
    @Expose
    var totalSpendingCurrMonth = 0.0

    @SerializedName("totalSpendingCurrWeek")
    @Expose
    var totalSpendingCurrWeek = 0.0

    @SerializedName("totalSpendingLastMonth")
    @Expose
    var totalSpendingLastMonth = 0.0

    @SerializedName("totalSpendingLastTwoMonths")
    @Expose
    var totalSpendingLastTwoMonths = 0.0

    @SerializedName("totalSpendingLastWeek")
    @Expose
    var totalSpendingLastWeek = 0.0

    @SerializedName("totalSpendingQuarterly")
    @Expose
    var totalSpendingQuarterly = 0.0
}