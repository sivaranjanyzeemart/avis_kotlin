package zeemart.asia.buyers.models.order

import zeemart.asia.buyers.models.marketlist.Product
import zeemart.asia.buyers.models.orderimportimport.DraftOrdersBySKUPaginated

class CartListDraftOrderUI {
    var isSelectAllHeader = false
    var isSupplierDetailHeader = false
    var isDraftProduct = false
    var isDraftNotes = false
    var isEditDraft = false
    var order: Orders? = null
    var product: Product? = null

    class OrderDeliveryDateCompare : Comparator<DraftOrdersBySKUPaginated.DraftOrdersBySKU> {
        override fun compare(i1: DraftOrdersBySKUPaginated.DraftOrdersBySKU, i2: DraftOrdersBySKUPaginated.DraftOrdersBySKU): Int {
            if (i1.orders?.timeDelivered == null || i2.orders?.timeDelivered == null) return 0
            val r = i2.orders!!.timeDelivered?.let { i1.orders!!.timeDelivered?.minus(it)?.toInt() }
            return r!!
        }
    }
}