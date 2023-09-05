package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.helper.SharedPref

enum class DeliveryStatus(var statusName: String) {
    RECEIVED("Received"), NOT_RECEIVED("NotReceived");

    class DeliveryStatusWithFilter {
        @Expose
        var deliveryStatus: DeliveryStatus? = null

        @Expose
        var isStatusSelectedForFilter = false
    }

    companion object {
//        const val RECEIVED_VAL :String = "Received"
//        const val NOT_RECEIVED_VAL :String = "NotReceived"
        @JvmStatic
        fun initializeDeliveryStatusFilters() {
            SharedPref.removeVal(SharedPref.DELIVERY_STATUS_FILTER_LIST)
            val filterDeliveryStatusList: MutableList<DeliveryStatusWithFilter> = ArrayList()
            val deliveryStatusReceived = DeliveryStatusWithFilter()
            val received = RECEIVED
            deliveryStatusReceived.deliveryStatus = received
            deliveryStatusReceived.isStatusSelectedForFilter = false
            filterDeliveryStatusList.add(deliveryStatusReceived)
            val deliveryStatusNotReceived = DeliveryStatusWithFilter()
            val notReceived = NOT_RECEIVED
            deliveryStatusNotReceived.deliveryStatus = notReceived
            deliveryStatusNotReceived.isStatusSelectedForFilter = false
            filterDeliveryStatusList.add(deliveryStatusNotReceived)
            val filterDeliveryList =
                ZeemartBuyerApp.gsonExposeExclusive.toJson(filterDeliveryStatusList)
            SharedPref.write(SharedPref.DELIVERY_STATUS_FILTER_LIST, filterDeliveryList)
        }

        @JvmStatic
        val filterListFromSharedPrefs: List<DeliveryStatusWithFilter>
            get() {
                val filterStatusList =
                    SharedPref.read(SharedPref.DELIVERY_STATUS_FILTER_LIST, "")
                return ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    filterStatusList,
                    object : TypeToken<List<DeliveryStatusWithFilter?>?>() {}.type
                )
            }

        @JvmStatic
        fun setAllDeliveryStatusUpdatedList(list: List<DeliveryStatusWithFilter?>?) {
            val filterDeliveryList = ZeemartBuyerApp.gsonExposeExclusive.toJson(list)
            SharedPref.write(SharedPref.DELIVERY_STATUS_FILTER_LIST, filterDeliveryList)
        }

        /**
         * return all the selected status name in a Set of Strings for filtering the deliveries
         *
         * @return
         */
        @JvmStatic
        val statusSelectedForFilters: Set<String>
            get() {
                val selectedDeliveryStatusForFiltering: MutableSet<String> = HashSet()
                val allDeliveryStatusFilters = filterListFromSharedPrefs
                for (i in allDeliveryStatusFilters.indices) {
                    if (allDeliveryStatusFilters[i].isStatusSelectedForFilter) {
                        selectedDeliveryStatusForFiltering.add(allDeliveryStatusFilters[i].deliveryStatus!!.statusName)
                    }
                }
                return selectedDeliveryStatusForFiltering
            }

        @JvmStatic
        fun clearAllDeliveryStatusFilterData() {
            SharedPref.removeVal(SharedPref.DELIVERY_STATUS_FILTER_LIST)
        }
    }
}