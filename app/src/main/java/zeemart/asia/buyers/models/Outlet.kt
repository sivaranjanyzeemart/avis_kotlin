package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import zeemart.asia.buyers.helper.SharedPref
import java.util.*

/**
 * Created by ParulBhandari on 12/11/2017.
 */
open class Outlet {
    @SerializedName("outletId")
    @Expose
    var outletId: String? = null

    @SerializedName("outletName")
    @Expose
    var outletName: String? = null

    @SerializedName("timeZone")
    @Expose
    var timeZone: String? = null

    @SerializedName("invoiceEmailId")
    @Expose
    var invoiceEmailId: String? = null

    constructor() {}

    val timeZoneFromOutlet: TimeZone?
        get() {
            val outlets = SharedPref.buyerDetail?.outlet
            if (outlets != null) {
                for (o in outlets) {
                    try {
                        if (o.outletId.equals(outletId, ignoreCase = true)) {
                            return TimeZone.getTimeZone(o.timeZone)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            return null
        }

    constructor(outletId: String?, outletName: String?) {
        this.outletId = outletId
        this.outletName = outletName
    }

    override fun hashCode(): Int {
        return outletId.hashCode()
    }

    override fun equals(obj: Any?): Boolean {
        return if (obj !is Outlet) {
            false
        } else {
            outletId == obj.outletId
        }
    }

    /**
     * get the outlet name from login details matching the outlet Id from order details
     *
     * @return current outlet name
     */
    val currentOutletName: String?
        get() {
            var outletName = ""
            if (SharedPref.buyerDetail != null) {
                val outletList = SharedPref.buyerDetail?.outlet
                if (outletList != null && outletList.size > 0) {
                    for (i in outletList.indices) {
                        if (outletList[i].outletId == outletId) {
                            outletName = outletList[i].outletName.toString()
                            break
                        }
                    }
                }
            }
            return outletName
        }
}