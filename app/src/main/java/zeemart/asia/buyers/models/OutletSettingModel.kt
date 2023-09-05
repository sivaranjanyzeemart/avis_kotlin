package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class OutletSettingModel {
    @SerializedName("timeZone")
    @Expose
    var timeZone: String? = null

    @SerializedName("enableSpecialRequest")
    @Expose
    var enableSpecialRequest: Boolean? = true
        get() {
            if (field == null) enableSpecialRequest = true
            return field
        }

    @SerializedName("enableDeliveryInstruction")
    @Expose
    var enableDeliveryInstruction: Boolean? = false
        get() {
            if (field == null) enableDeliveryInstruction = false
            return field
        }

    @SerializedName("enableGRN")
    @Expose
    var isEnableGRN = false

    @SerializedName("customDeliveryInstructions")
    @Expose
    var customDeliveryInstructions: List<String>? = null

    @SerializedName("notification")
    @Expose
    var notification: Notification? = null
    val deliveryInstructionSelectionList: List<DeliveryInstructionIsSelected>?
        get() {
            val deliveryInstructionIsSelectedList: MutableList<DeliveryInstructionIsSelected> =
                ArrayList()
            val deliveryInstructions = customDeliveryInstructions
            return if (deliveryInstructions != null && deliveryInstructions.size > 0) {
                for (i in deliveryInstructions.indices) {
                    val deliveryInstruction = DeliveryInstructionIsSelected()
                    deliveryInstruction.deliveryInstruction = deliveryInstructions[i]
                    deliveryInstruction.isSelected = false
                    deliveryInstructionIsSelectedList.add(deliveryInstruction)
                }
                deliveryInstructionIsSelectedList
            } else {
                null
            }
        }

    class Notification {
        @SerializedName("weeklyReport")
        @Expose
        var weeklyReport: WeeklyReport? = null
    }

    class WeeklyReport {
        @SerializedName("status")
        @Expose
        var status: String? = null

        @SerializedName("emails")
        @Expose
        var emails: List<String>? = null

        @SerializedName("frequency")
        @Expose
        var frequency: List<String>? = null

        @SerializedName("weeklyOn")
        @Expose
        var weeklyOn: List<WeeklyOn>? = null
    }

    inner class WeeklyOn {
        @SerializedName("day")
        @Expose
        var day: String? = null

        @SerializedName("time")
        @Expose
        var time: String? = null
    }

    class DeliveryInstructionIsSelected {
        var deliveryInstruction: String? = null
        var isSelected = false
    }
}