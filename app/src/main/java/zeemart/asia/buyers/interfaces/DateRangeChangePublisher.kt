package zeemart.asia.buyers.interfaces

/**
 * Created by ParulBhandari on 2/19/2018.
 */
interface DateRangeChangePublisher {
    fun addDateRangeChangeObserver(o: DateRangeChangeObserver?)
    fun removeDateRangeChangeObserver(o: DateRangeChangeObserver?)
    fun notifyObserver(timePeriodValue: String?, startDate: String?, endDate: String?)
}