package zeemart.asia.buyers.models.reportsimportimport

import zeemart.asia.buyers.models.invoiceimport.PriceUpdateModelBaseResponse
import zeemart.asia.buyers.models.orderimport.HeaderViewOrderUI

/**
 * Created by ParulBhandari on 3/1/2018.
 */
class PriceUpdateListWithStickyHeaderUI {
    var isHeader = false
    var headerData: HeaderViewOrderUI? = null
    lateinit var skuPriceData: PriceUpdateModelBaseResponse.PriceDetailModel
}