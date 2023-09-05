package zeemart.asia.buyers.helper.navigation

import zeemart.asia.buyers.invoices.InQueueForUploadDataModel

/**
 * Created by ParulBhandari on 7/11/2018.
 * add "invoices" tab specific data which need to be passed to invoices tab fragment from the BaseNavigation activity
 * @see InvoicesFragment must implement this interface to get the callback
 * and respective callback method should be added to the interface
 */
interface InvoiceTabNavigationListener : HomeNavigationTabListener {
    fun onDataRequestForUpload(invoiceDataList: List<InQueueForUploadDataModel?>?)
}