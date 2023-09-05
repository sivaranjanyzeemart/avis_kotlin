package zeemart.asia.buyers.interfaces

import zeemart.asia.buyers.models.marketlist.Product

/**
 * Created by ParulBhandari on 12/16/2017.
 */
interface ReviewOrderItemChangeListener {
    fun onItemQuantityChanged(selectedProducts: List<Product?>?)
}