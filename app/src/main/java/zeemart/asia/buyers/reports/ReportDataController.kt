package zeemart.asia.buyers.reports

import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.DateHelper.StartDateEndDate
import zeemart.asia.buyers.models.PriceDetails
import zeemart.asia.buyers.models.Supplier
import zeemart.asia.buyers.models.invoiceimport.PriceUpdateModelBaseResponse
import zeemart.asia.buyers.models.reportsimport.QuantityBoughtUI
import zeemart.asia.buyers.models.reportsimport.ReportResponse
import zeemart.asia.buyers.models.reportsimport.SkuDateBoughtUI
import java.util.*

/**
 * Created by ParulBhandari on 2/12/2018.
 */
class ReportDataController {
    fun getSKUByQuantityBoughtData(spendingDetailList: ArrayList<ReportResponse.TotalSpendingDetailsByRangeData>?): ArrayList<QuantityBoughtUI> {
        val quantityBoughtData = ArrayList<QuantityBoughtUI>()
        val uomMap: MutableMap<String, ArrayList<ReportResponse.TotalSpendingDetailsByRangeData>> = HashMap()
        if (spendingDetailList != null && spendingDetailList.size > 0) {
            for (i in spendingDetailList.indices) {
                if (uomMap.size == 0) {
                    val prodList = ArrayList<ReportResponse.TotalSpendingDetailsByRangeData>()
                    prodList.add(spendingDetailList[i])
                    uomMap[spendingDetailList[i].uom!!] = prodList
                } else {
                    if (uomMap.containsKey(spendingDetailList[i].uom)) {
                        uomMap[spendingDetailList[i].uom]!!.add(spendingDetailList[i])
                    } else {
                        val prodList = ArrayList<ReportResponse.TotalSpendingDetailsByRangeData>()
                        prodList.add(spendingDetailList[i])
                        uomMap[spendingDetailList[i].uom!!] = prodList
                    }
                }
            }
        }
        for (key in uomMap.keys) {
            val quantityBoughtDetails = QuantityBoughtUI()
            val productList = uomMap[key]!!
            var amountTotal = 0.0
            var quantity = 0.0
            for (i in productList.indices) {
                amountTotal = amountTotal + productList[i].amount!!
                quantity = quantity + productList[i].quantity!!
            }
            quantityBoughtDetails.amount = amountTotal
            quantityBoughtDetails.quantity = quantity
            quantityBoughtDetails.uom = key
            if (amountTotal != 0.0) quantityBoughtData.add(quantityBoughtDetails)
        }
        Collections.sort(quantityBoughtData, Comparator { o1, o2 ->
            if (o2.amount!! < o1.amount!!) {
                return@Comparator -1
            } else if (o1.amount!! < o2.amount!!) {
                return@Comparator 1
            }
            0
        })
        return quantityBoughtData
    }

    fun getSKUByDateData(spendingDetailList: ArrayList<ReportResponse.TotalSpendingDetailsByRangeData>?): ArrayList<SkuDateBoughtUI?> {
        val skuDateBoughtList = ArrayList<SkuDateBoughtUI?>()

        /**Map of Map because the data is sorted by date and for everydate
         * the product can be bought by different uoms eg. banana can be bought by dozen and kg on same date. */
        val byDateMap: MutableMap<String, MutableMap<String, SkuDateBoughtUI>> = HashMap()
        if (spendingDetailList != null && spendingDetailList.size > 0) {
            for (i in spendingDetailList.indices) {
                if (byDateMap.size == 0) {
                    val skuDetailsByUom: MutableMap<String, SkuDateBoughtUI> = HashMap()
                    val skuData = SkuDateBoughtUI()
                    skuData.invoiceDate = spendingDetailList[i].invoiceDate
                    skuData.quantity = spendingDetailList[i].quantity
                    skuData.amount = spendingDetailList[i].amount
                    skuData.uom = spendingDetailList[i].uom
                    skuDetailsByUom[spendingDetailList[i].uom!!] = skuData
                    byDateMap[dateFormatForSkuByDateList(spendingDetailList[i].invoiceDate)] =
                        skuDetailsByUom
                } else {
                    if (byDateMap.containsKey(dateFormatForSkuByDateList(spendingDetailList[i].invoiceDate))) {
                        val skuDetailsByUom = byDateMap[dateFormatForSkuByDateList(
                            spendingDetailList[i].invoiceDate
                        )]!!
                        if (skuDetailsByUom.containsKey(spendingDetailList[i].uom)) {
                            val skuDateBoughtModel = skuDetailsByUom[spendingDetailList[i].uom]
                            val updateQuantity =
                                skuDateBoughtModel!!.quantity?.plus(spendingDetailList[i].quantity!!)
                            val updateAmount =
                                skuDateBoughtModel.amount?.plus(spendingDetailList[i].amount!!)
                            skuDetailsByUom[spendingDetailList[i].uom]!!.quantity = updateQuantity
                            skuDetailsByUom[spendingDetailList[i].uom]!!.amount = updateAmount
                        } else {
                            val skuData = SkuDateBoughtUI()
                            skuData.invoiceDate = spendingDetailList[i].invoiceDate
                            skuData.quantity = spendingDetailList[i].quantity
                            skuData.amount = spendingDetailList[i].amount
                            skuData.uom = spendingDetailList[i].uom
                            skuDetailsByUom[spendingDetailList[i].uom!!] = skuData
                        }
                    } else {
                        val skuDetailsByUom: MutableMap<String, SkuDateBoughtUI> = HashMap()
                        val skuData = SkuDateBoughtUI()
                        skuData.invoiceDate = spendingDetailList[i].invoiceDate
                        skuData.quantity = spendingDetailList[i].quantity
                        skuData.amount = spendingDetailList[i].amount
                        skuData.uom = spendingDetailList[i].uom
                        skuDetailsByUom[spendingDetailList[i].uom!!] = skuData
                        byDateMap[dateFormatForSkuByDateList(spendingDetailList[i].invoiceDate)] =
                            skuDetailsByUom
                    }
                }
            }
        }
        for (key in byDateMap.keys) {
            val byUOMMap: Map<String, SkuDateBoughtUI> = byDateMap[key]!!
            for (uomKey in byUOMMap.keys) {
                if (byUOMMap[uomKey]!!.amount != 0.0) skuDateBoughtList.add(byUOMMap[uomKey])
            }
        }
        Collections.sort(skuDateBoughtList, Comparator { o1, o2 ->
            if (o2?.invoiceDate!! < o1?.invoiceDate!!) {
                return@Comparator -1
            } else if (o1.invoiceDate!! < o2.invoiceDate!!) {
                return@Comparator 1
            }
            0
        })
        return skuDateBoughtList
    }

    private fun dateFormatForSkuByDateList(date: Long): String {
        val dateString = DateHelper.getDateInDateMonthYearFormat(date)
        val dayString = DateHelper.getDateIn3LetterDayFormat(date)
        return "$dateString ($dayString)"
    }

    fun getSKUPriceUpdateUOMMap(priceUpdateData: List<PriceUpdateModelBaseResponse.PriceDetailModel>): Map<String, MutableList<PriceUpdateModelBaseResponse.PriceDetailModel>> {
        val SKUPriceUpdateUOMMap: MutableMap<String, MutableList<PriceUpdateModelBaseResponse.PriceDetailModel>> = HashMap()
        for (i in priceUpdateData.indices) {
            if (SKUPriceUpdateUOMMap.size == 0) {
                val priceUpdateList: MutableList<PriceUpdateModelBaseResponse.PriceDetailModel> = ArrayList()
                priceUpdateList.add(priceUpdateData[i])
                SKUPriceUpdateUOMMap[priceUpdateData[i].unitSize!!] = priceUpdateList
            } else {
                if (SKUPriceUpdateUOMMap.containsKey(priceUpdateData[i].unitSize)) {
                    SKUPriceUpdateUOMMap[priceUpdateData[i].unitSize]!!.add(priceUpdateData[i])
                } else {
                    val priceUpdateList: MutableList<PriceUpdateModelBaseResponse.PriceDetailModel> = ArrayList()
                    priceUpdateList.add(priceUpdateData[i])
                    SKUPriceUpdateUOMMap[priceUpdateData[i].unitSize!!] = priceUpdateList
                }
            }
        }
        return SKUPriceUpdateUOMMap
    }

    class TotalSpendingCurrentAndReferenceData {
        var totalSpendingCurrent: PriceDetails? = null
        var totalSpendingReference: PriceDetails? = null
    }

    companion object {
        fun getTotalSpendingVariations(
            previousSpendingTotal: PriceDetails,
            currentSpendingTotal: PriceDetails
        ): Double {
            val percentageVariation: Double

            // if its wrong change getAmount to getDoubleValue
            val previousDateRangeAmount = previousSpendingTotal.amount
            val currentDateRangeAmount = currentSpendingTotal.amount
            return if (previousDateRangeAmount == 0.0) {
                0.00
            } else {
                percentageVariation =
                    (currentDateRangeAmount?.minus(previousDateRangeAmount!!))?.div(previousDateRangeAmount!!)?.times(100)!!
                percentageVariation
            }
        }

        fun getTotalSpendingByWeekOrMonth(
            reportResponseData: ReportResponse.TotalSpendingDetailsByRange?,
            startEndDate: StartDateEndDate
        ): Double {
            var spendingTotal = 0.0
            if (reportResponseData != null && reportResponseData.data != null && reportResponseData.data!!.size > 0) {
                for (i in reportResponseData.data!!.indices) {
                    if (startEndDate.startDateMillis <= reportResponseData.data!![i].dateTime!! &&
                        reportResponseData.data!![i].dateTime!! <= startEndDate.endDateMillis
                    ) {
                        spendingTotal =
                            spendingTotal + reportResponseData.data!![i].totalSpendingAmount!!
                    }
                }
            }
            return spendingTotal
        }

        fun getHighestTransactionSupplier(totalSpendingBySupplierList: List<ReportResponse.TotalSpendingBySupplierData>): Supplier {
            Collections.sort(totalSpendingBySupplierList) { o1, o2 ->
                val ret = o2.totalSpending?.minus(o1.totalSpending!!)
                ret?.toInt()!!
            }
            return totalSpendingBySupplierList[0].supplier!!
        }

        fun getTotalSpendingAndReferenceDateSpending(totalSpendingBySupplierList: List<ReportResponse.TotalSpendingBySupplierData>): TotalSpendingCurrentAndReferenceData {
            val totalSpendingCurrentAndReferenceData = TotalSpendingCurrentAndReferenceData()
            var totalSpending = 0.0
            var totalSpendingRef = 0.0
            for (i in totalSpendingBySupplierList.indices) {
                totalSpending = totalSpending + totalSpendingBySupplierList[i].totalSpending!!
                totalSpendingRef =
                    totalSpendingRef + totalSpendingBySupplierList[i].pastTotalSpending!!
            }
            val totalSpendingPrice = PriceDetails(totalSpending)
            val totalSpendingPriceRef = PriceDetails(totalSpendingRef)
            totalSpendingCurrentAndReferenceData.totalSpendingCurrent = totalSpendingPrice
            totalSpendingCurrentAndReferenceData.totalSpendingReference = totalSpendingPriceRef
            return totalSpendingCurrentAndReferenceData
        }
    }
}