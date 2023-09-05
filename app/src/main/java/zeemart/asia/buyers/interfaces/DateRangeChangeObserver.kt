package zeemart.asia.buyers.interfaces

/**
 * Created by ParulBhandari on 2/19/2018.
 */
interface DateRangeChangeObserver {
    fun onDateRangeChanged(TimePeriodValue: String?, startDate: String?, endDate: String?)
}