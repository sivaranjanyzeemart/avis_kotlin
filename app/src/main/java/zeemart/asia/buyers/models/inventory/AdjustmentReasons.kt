package zeemart.asia.buyers.models.inventory

import zeemart.asia.buyers.R
import java.util.*


enum class AdjustmentReasons(val apiValue: String, val resId: Int) {
    Wastage("WASTAGE", R.string.txt_wastage), Transfer_Out(
        "TRANSFER_OUT",
        R.string.txt_transfer_out
    ),
    Transfer_In("TRANSFER_IN", R.string.txt_transfer_in), Missing(
        "MISSING",
        R.string.txt_missing
    ),
    Found("FOUND", R.string.txt_found), Promotion("PROMOTION", R.string.txt_promotion);

    class AdjustmentReasonSelected {
        var adjustmentReasons: AdjustmentReasons? = null
        var isSelected = false
    }

    companion object {
        @JvmStatic
        fun getReasonResId(apiValue: String): Int? {
            var resId: Int? = null
            val adjustmentReasons: List<AdjustmentReasons> = ArrayList(Arrays.asList(*values()))
            for (i in adjustmentReasons.indices) {
                if (adjustmentReasons[i].apiValue == apiValue) {
                    resId = adjustmentReasons[i].resId
                }
            }
            return resId
        }

        @JvmStatic
        fun isNegativeAdjustmentReason(apiValue: String): Boolean {
            return if (apiValue == Missing.apiValue || apiValue == Promotion.apiValue || apiValue == Wastage.apiValue || apiValue == Transfer_Out.apiValue) {
                true
            } else false
        }
    }
}