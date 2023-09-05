package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.models.order.Orders

class OrderTypeWithFilter {
    @Expose
    var orderType: Orders.Type? = null

    @Expose
    var isStatusSelectedForFilter = false

    companion object {
        @JvmStatic
        fun initializeOrderTypeFilters() {
            SharedPref.removeVal(SharedPref.ORDER_TYPE_FILTER_LIST)
            val filterInvoiceStatusList: MutableList<OrderTypeWithFilter> = ArrayList()
            val orderTypeDeal = OrderTypeWithFilter()
            val deal = Orders.Type.DEAL
            orderTypeDeal.orderType = deal
            orderTypeDeal.isStatusSelectedForFilter = false
            filterInvoiceStatusList.add(orderTypeDeal)
            val orderTypeEssentials = OrderTypeWithFilter()
            val essentials = Orders.Type.ESSENTIALS
            orderTypeEssentials.orderType = essentials
            orderTypeEssentials.isStatusSelectedForFilter = false
            filterInvoiceStatusList.add(orderTypeEssentials)
            val filterInvoiceList =
                ZeemartBuyerApp.gsonExposeExclusive.toJson(filterInvoiceStatusList)
            SharedPref.write(SharedPref.ORDER_TYPE_FILTER_LIST, filterInvoiceList)
        }

        @JvmStatic
        val filterListFromSharedPrefs: List<OrderTypeWithFilter>
            get() {
                val filterOrderTypeList =
                    SharedPref.read(SharedPref.ORDER_TYPE_FILTER_LIST, "")
                return ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    filterOrderTypeList,
                    object : TypeToken<List<OrderTypeWithFilter?>?>() {}.type
                )
            }

        @JvmStatic
        fun setAllOrderTypeUpdatedList(list: List<OrderTypeWithFilter?>?) {
            val filterOrderTypeList = ZeemartBuyerApp.gsonExposeExclusive.toJson(list)
            SharedPref.write(SharedPref.ORDER_TYPE_FILTER_LIST, filterOrderTypeList)
        }

        /**
         * return all the selected status name in a Set of Strings for filtering the deliveries
         *
         * @return
         */
        @JvmStatic
        val statusSelectedForFilters: Set<String?>
            get() {
                val selectedInvoiceStatusForFiltering: MutableSet<String?> = HashSet()
                val allInvoiceStatusFilters = filterListFromSharedPrefs
                for (i in allInvoiceStatusFilters.indices) {
                    if (allInvoiceStatusFilters[i].isStatusSelectedForFilter) {
                        selectedInvoiceStatusForFiltering.add(allInvoiceStatusFilters[i].orderType?.name)
                    }
                }
                return selectedInvoiceStatusForFiltering
            }

        @JvmStatic
        fun clearAllOrderTypeFilterData() {
            SharedPref.removeVal(SharedPref.ORDER_TYPE_FILTER_LIST)
        }
    }
}