package zeemart.asia.buyers.reports

/**
 * Created by ParulBhandari on 2/17/2018.
 */
enum class ReportTimePeriod(val timePeriodValue: String) {
    THISWEEK("ThisWeek"), LASTWEEK("LastWeek"), LASTTOLASTWEEK("LastToLastWeek"), THISMONTH("ThisMonth"), LASTMONTH(
        "LastMonth"
    ),
    LASTTOLASTMONTH("LastToLastMonth"), CUSTOM("Custom");

}