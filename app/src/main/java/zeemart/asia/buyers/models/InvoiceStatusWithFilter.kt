package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.models.invoice.Invoice

class InvoiceStatusWithFilter {
    @Expose
    var invoiceStatus: Invoice.InvoiceStatus? = null

    @Expose
    var isStatusSelectedForFilter = false

    companion object {
        @JvmStatic
        fun initializeInvoiceStatusFilters() {
            SharedPref.removeVal(SharedPref.INVOICE_STATUS_FILTER_LIST)
            val filterInvoiceStatusList: MutableList<InvoiceStatusWithFilter> = ArrayList()
            val invoiceStatusInvoiced = InvoiceStatusWithFilter()
            val invoiced = Invoice.InvoiceStatus.INVOICED
            invoiceStatusInvoiced.invoiceStatus = invoiced
            invoiceStatusInvoiced.isStatusSelectedForFilter = false
            filterInvoiceStatusList.add(invoiceStatusInvoiced)
            val invoiceStatusNotInvoices = InvoiceStatusWithFilter()
            val notInvoiced = Invoice.InvoiceStatus.NO_LINKED_INVOICE
            invoiceStatusNotInvoices.invoiceStatus = notInvoiced
            invoiceStatusNotInvoices.isStatusSelectedForFilter = false
            filterInvoiceStatusList.add(invoiceStatusNotInvoices)
            val filterInvoiceList =
                ZeemartBuyerApp.gsonExposeExclusive.toJson(filterInvoiceStatusList)
            SharedPref.write(SharedPref.INVOICE_STATUS_FILTER_LIST, filterInvoiceList)
        }

        @JvmStatic
        val filterListFromSharedPrefs: List<InvoiceStatusWithFilter>
            get() {
                val filterStatusList =
                    SharedPref.read(SharedPref.INVOICE_STATUS_FILTER_LIST, "")
                return ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    filterStatusList,
                    object : TypeToken<List<InvoiceStatusWithFilter?>?>() {}.type
                )
            }

        @JvmStatic
        fun setAllInvoiceStatusUpdatedList(list: List<InvoiceStatusWithFilter?>?) {
            val filterInvoiceList = ZeemartBuyerApp.gsonExposeExclusive.toJson(list)
            SharedPref.write(SharedPref.INVOICE_STATUS_FILTER_LIST, filterInvoiceList)
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
                        selectedInvoiceStatusForFiltering.add(allInvoiceStatusFilters[i].invoiceStatus?.statusName)
                    }
                }
                return selectedInvoiceStatusForFiltering
            }

        @JvmStatic
        fun clearAllInvoiceStatusFilterData() {
            SharedPref.removeVal(SharedPref.INVOICE_STATUS_FILTER_LIST)
        }
    }
}