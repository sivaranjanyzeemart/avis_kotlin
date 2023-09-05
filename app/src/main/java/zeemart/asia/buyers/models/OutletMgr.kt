package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.helper.SharedPref
import java.util.*

/**
 * Created by ParulBhandari on 7/25/2018.
 */
class OutletMgr : Outlet() {
    @Expose
    var isOutletSelected = false

    companion object {//sort the outlet list alphabetically
        /**
         * fetch the outlet list for filtering on outlet
         * in Deliveries and View Order screen
         * @return List of outlet filters
         */
        @JvmStatic
        val outletsFilters: MutableList<OutletMgr>?
            get() {
                val outletFiltersJson = SharedPref.read(SharedPref.OUTLET_FILTER_LIST, "")
                val outletFiltersList =
                    ZeemartBuyerApp.gsonExposeExclusive.fromJson<MutableList<OutletMgr>>(
                        outletFiltersJson,
                        object : TypeToken<List<OutletMgr?>?>() {}.type
                    )
                //sort the outlet list alphabetically
                outletFiltersList?.sortWith { o1, o2 ->
                    o2.outletName?.lowercase(Locale.getDefault())
                        ?.let { o1.outletName?.lowercase(Locale.getDefault())?.compareTo(it) }!!
                }
                return outletFiltersList
            }

        /**
         * save the list of outlet to shared preferences
         * @param outletFiltersList
         */
        @JvmStatic
        fun setOutletFilters(outletFiltersList: List<OutletMgr?>?) {
            val filterOutletListJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(outletFiltersList)
            SharedPref.write(SharedPref.OUTLET_FILTER_LIST, filterOutletListJson)
        }

        @JvmStatic
        fun clearAllOutletFilterData() {
            SharedPref.removeVal(SharedPref.OUTLET_FILTER_LIST)
        }

        @JvmStatic
        val selectedOutlet: List<OutletMgr>?
            get() {
                val outletList = outletsFilters
                if (outletList != null) {
                    val itr = outletList.iterator()
                    while (itr.hasNext()) {
                        val outletData = itr.next()
                        if (!outletData.isOutletSelected) {
                            itr.remove()
                        }
                    }
                }
                return outletList
            }
    }
}