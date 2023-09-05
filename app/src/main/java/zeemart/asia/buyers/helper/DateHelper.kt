package zeemart.asia.buyers.helper

import android.util.Log
import com.google.gson.annotations.Expose
import zeemart.asia.buyers.constants.ZeemartAppConstants
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by ParulBhandari on 8/21/2018.
 */
object DateHelper {
    private const val DATE_FORMAT_d_MM_yyyy = "d MMM yyyy"
    private const val DATE_FORMAT_D_MMM = "d MMM"
    private const val DATE_FORMAT_EEEE = "EEEE"
    private const val DATE_FORMAT_d_MMM_HH_mm = "d MMM HH:mm"
    private const val DATE_FORMAT_MMMM_yyyy = "MMMM yyyy"
    private const val DATE_FORMAT_EEE_d_MMM_yyyy = "EEE, d MMM yyyy"
    private const val DATE_FORMAT_EEEE_d_MMM_yyyy = "EEEE, d MMM yyyy"
    private const val DATE_FORMAT_yyyy_MM_dd_HH_mm_ss = "yyyyMMddHHmmss"
    private const val DATE_FORMAT_HH_mm = "HH:mm"
    private const val DATE_FORMAT_EEE = "EEE"
    private const val DATE_FORMAT_d_MMM_hh_mm_a = "d MMM, hh:mm a"
    private const val DATE_FORMAT_hh_mm_aaa = "hh:mm aaa"
    private const val DATE_FORMAT_d_MMM_yyyy_HH_mm = "d MMM yyyy, HH:mm"
    private const val DATE_FORMAT_d_MMM_yyyy_bracket_HH_mm = "d MMM yyyy (HH:mm)"
    @JvmStatic
    val marketTimeZone: TimeZone
        get() = ZeemartAppConstants.Market.`this`.getTimeZone()

    /**
     * get date in d MMM format
     *
     * @param date
     * @return
     */
    fun getDateInDateMonthFormat(date: Long?): String {
        return getDateInDateMonthFormat(date, null)
    }

    fun getDateInDateMonthFormat(date: Long?, timeZone: TimeZone?): String {
        if (date != null) {
            val mDate = Date(date.toLong() * 1000)
            val sdf =
                SimpleDateFormat(DATE_FORMAT_D_MMM, Locale.ENGLISH)
            sdf.timeZone = marketTimeZone
            if (timeZone != null) sdf.timeZone = timeZone
            return sdf.format(mDate)
        }
        return ""
    }

    /**
     * returns the weekday(full name)
     *
     * @param date
     * @return
     */
    fun getDateInFullDayFormat(date: Long?): String {
        return getDateInFullDayFormat(date, null)
    }

    fun getDateInFullDayFormat(date: Long?, timeZone: TimeZone?): String {
        if (date != null) {
            val mDate = Date(date.toLong() * 1000)
            val sdf =
                SimpleDateFormat(DATE_FORMAT_EEEE, Locale.ENGLISH)
            sdf.timeZone = marketTimeZone
            if (timeZone != null) sdf.timeZone = timeZone
            return sdf.format(mDate)
        }
        return ""
    }

    /**
     * returns the weekday(3 letter format)
     *
     * @param date
     * @return
     */
    fun getDateIn3LetterDayFormat(date: Long?): String {
        return getDateIn3LetterDayFormat(date, null)
    }

    @JvmStatic
    fun getDateIn3LetterDayFormat(date: Long?, timeZone: TimeZone?): String {
        if (date != null) {
            val mDate = Date(date.toLong() * 1000)
            val sdf =
                SimpleDateFormat(DATE_FORMAT_EEE)
            sdf.timeZone = marketTimeZone
            if (timeZone != null) sdf.timeZone = timeZone
            return sdf.format(mDate)
        }
        return ""
    }

    /**
     * returns the date in d MMM yyyy format
     */
    fun getDateInDateMonthYearFormat(date: Long?): String {
        return getDateInDateMonthYearFormat(date, null)
    }

    @JvmStatic
    fun getDateInDateMonthYearFormat(date: Long?, timeZone: TimeZone?): String {
        if (date != null) {
            val mDate = Date(date.toLong() * 1000)
            val sdf = SimpleDateFormat(
                DATE_FORMAT_d_MM_yyyy,
                Locale.ENGLISH
            )
            sdf.timeZone = marketTimeZone
            if (timeZone != null) sdf.timeZone = timeZone
            return sdf.format(mDate)
        }
        return ""
    }

    @JvmStatic
    fun getDateInDateMonthYearTimeFormat(date: Long?, timeZone: TimeZone?): String {
        if (date != null) {
            val mDate = Date(date.toLong() * 1000)
            val sdf = SimpleDateFormat(
                DATE_FORMAT_d_MMM_yyyy_HH_mm,
                Locale.ENGLISH
            )
            sdf.timeZone = marketTimeZone
            if (timeZone != null) sdf.timeZone = timeZone
            return sdf.format(mDate)
        }
        return ""
    }

    fun getDateInDateMonthYearTimeInBracketFormat(date: Long?, timeZone: TimeZone?): String {
        if (date != null) {
            val mDate = Date(date.toLong() * 1000)
            val sdf = SimpleDateFormat(
                DATE_FORMAT_d_MMM_yyyy_bracket_HH_mm,
                Locale.ENGLISH
            )
            sdf.timeZone = marketTimeZone
            if (timeZone != null) sdf.timeZone = timeZone
            return sdf.format(mDate)
        }
        return ""
    }

    /**
     * return the cut off time format
     * d MMM, hh:mm a
     *
     * @param dateTime
     * @return
     */
    fun getDateInDateMonthHourMinute(dateTime: Long?): String {
        return getDateInDateMonthHourMinute(dateTime, null)
    }

    fun getDateInDateMonthHourMinute(dateTime: Long?, timeZone: TimeZone?): String {
        if (dateTime != null) {
            val dateTimestring = java.lang.Long.toString(dateTime)
            val epoch = dateTimestring.toLong()
            val mDate = Date(epoch * 1000)
            val sdf = SimpleDateFormat(
                DATE_FORMAT_d_MMM_hh_mm_a,
                Locale.ENGLISH
            )
            sdf.timeZone = marketTimeZone
            if (timeZone != null) sdf.timeZone = timeZone
            return sdf.format(mDate)
        }
        return ""
    }

    /**
     * return the cut off time format
     * hh:mm aaa
     *
     * @param dateTime
     * @return
     */
    fun getDateInHourMinuteAMPM(dateTime: Long?): String {
        return getDateInHourMinuteAMPM(dateTime, null)
    }

    fun getDateInHourMinuteAMPM(dateTime: Long?, timeZone: TimeZone?): String {
        if (dateTime != null) {
            val dateTimestring = java.lang.Long.toString(dateTime)
            val epoch = dateTimestring.toLong()
            val mDate = Date(epoch * 1000)
            val sdf = SimpleDateFormat(
                DATE_FORMAT_hh_mm_aaa,
                Locale.ENGLISH
            )
            sdf.timeZone = marketTimeZone
            if (timeZone != null) sdf.timeZone = timeZone
            return sdf.format(mDate)
        }
        return ""
    }

    /**
     * return the epoch time for date in
     * format d MMM yyyy
     *
     * @param date
     * @return
     */
    @JvmOverloads
    fun returnEpochTimeSOD(date: String?, timeZone: TimeZone? = null): Long {
        var epochTime: Long = 0
        val sdf = SimpleDateFormat(DATE_FORMAT_d_MM_yyyy, Locale.ENGLISH)
        sdf.timeZone = marketTimeZone
        if (timeZone != null) sdf.timeZone = timeZone
        try {
            val mDate = sdf.parse(date)
            var cal = Calendar.getInstance(marketTimeZone)
            if (timeZone != null) cal = Calendar.getInstance(timeZone)
            epochTime = mDate.time
            cal.timeInMillis = epochTime
            setCalendarStartOfDayTime(cal)
            epochTime = cal.timeInMillis / 1000
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return epochTime
    }

    /**
     * takes format d MMM yyy and returns date object
     *
     * @param date
     * @return
     */
    @JvmOverloads
    fun toDate(date: String?, timeZone: TimeZone? = null): Date? {
        val sdf = SimpleDateFormat(DATE_FORMAT_d_MM_yyyy, Locale.ENGLISH)
        sdf.timeZone = marketTimeZone
        if (timeZone != null) sdf.timeZone = timeZone
        var formattedDate: Date? = null
        try {
            formattedDate = sdf.parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return formattedDate
    }

    /**
     * return the epoch time for date in d MMM yyyy format
     *
     * @param date
     * @return
     */
    @JvmOverloads
    fun returnEpochTimeEOD(date: String?, timeZone: TimeZone? = null): Long {
        var epochTime: Long = 0
        val sdf = SimpleDateFormat(DATE_FORMAT_d_MM_yyyy, Locale.ENGLISH)
        sdf.timeZone = marketTimeZone
        if (timeZone != null) sdf.timeZone = timeZone
        try {
            val mDate = sdf.parse(date)
            epochTime = mDate.time
            var cal = Calendar.getInstance(marketTimeZone)
            if (timeZone != null) cal = Calendar.getInstance(timeZone)
            cal.timeInMillis = epochTime
            setCalendarEndOfDayTime(cal)
            return cal.timeInMillis / 1000
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return epochTime
    }

    /**
     * @param week
     * @return
     */
    fun getWeekStartEndDate(week: WeekMonth): StartDateEndDate {
        return getWeekStartEndDate(week, null)
    }

    fun getWeekStartEndDate(week: WeekMonth, timeZone: TimeZone?): StartDateEndDate {
        var startDate =
            Calendar.getInstance(marketTimeZone)
        if (timeZone != null) startDate = Calendar.getInstance(timeZone)
        startDate[Calendar.DAY_OF_WEEK] = Calendar.MONDAY
        setCalendarStartOfDayTime(startDate)
        startDate.add(Calendar.DAY_OF_MONTH, -week.value)
        var endDate = Calendar.getInstance(marketTimeZone)
        if (timeZone != null) endDate = Calendar.getInstance(timeZone)
        endDate[Calendar.DAY_OF_WEEK] = Calendar.MONDAY
        setCalendarEndOfDayTime(endDate)
        endDate.add(Calendar.DAY_OF_MONTH, 6)
        endDate.add(Calendar.DAY_OF_MONTH, -week.value)
        return StartDateEndDate(startDate.timeInMillis / 1000, endDate.timeInMillis / 1000)
    }

    /**
     * @param month
     * @return
     */
    fun getMonthStartEndDate(month: WeekMonth): StartDateEndDate {
        return getMonthStartEndDate(month, null)
    }

    fun getMonthStartEndDate(month: WeekMonth, timeZone: TimeZone?): StartDateEndDate {
        var startDate =
            Calendar.getInstance(marketTimeZone)
        if (timeZone != null) startDate = Calendar.getInstance(timeZone)
        startDate.add(Calendar.MONTH, -month.value)
        startDate[Calendar.DAY_OF_MONTH] = 1
        setCalendarStartOfDayTime(startDate)
        var endDate = Calendar.getInstance(marketTimeZone)
        if (timeZone != null) endDate = Calendar.getInstance(timeZone)
        endDate.add(Calendar.MONTH, -month.value)
        endDate.add(Calendar.MONTH, 1)
        endDate[Calendar.DAY_OF_MONTH] = 1
        endDate.add(Calendar.DAY_OF_MONTH, -1)
        setCalendarEndOfDayTime(endDate)
        return StartDateEndDate(startDate.timeInMillis / 1000, endDate.timeInMillis / 1000)
    }

    /**
     * return the past start end date
     * startDate being the no of days ago and
     * end date being the current date
     *
     * @param daysAgo
     * @param isIncludeTodayDate
     * @return
     */
    @JvmOverloads
    fun pastDaysStartEndDate(
        daysAgo: Int,
        isIncludeTodayDate: Boolean,
        timeZone: TimeZone? = null
    ): StartDateEndDate {
        var startDate =
            Calendar.getInstance(marketTimeZone)
        if (timeZone != null) startDate = Calendar.getInstance(timeZone)
        startDate.add(Calendar.DAY_OF_MONTH, -daysAgo)
        setCalendarStartOfDayTime(startDate)
        var endDate = Calendar.getInstance(marketTimeZone)
        if (timeZone != null) endDate = Calendar.getInstance(timeZone)
        if (!isIncludeTodayDate) {
            endDate.add(Calendar.DAY_OF_MONTH, -1)
        }
        setCalendarEndOfDayTime(endDate)
        return StartDateEndDate(startDate.timeInMillis / 1000, endDate.timeInMillis / 1000)
    }

    /**
     * return the day of month
     *
     * @param date
     * @return
     */
    fun getDayOfMonthFromDate(date: Long): String {
        return getDayOfMonthFromDate(date, null)
    }

    fun getDayOfMonthFromDate(date: Long, timeZone: TimeZone?): String {
        var cal = Calendar.getInstance(marketTimeZone)
        if (timeZone != null) cal = Calendar.getInstance(timeZone)
        cal.timeInMillis = date * 1000
        val day = cal[Calendar.DAY_OF_MONTH]
        return if (day != 0) {
            Integer.toString(day)
        } else ""
    }

    /**
     * return the list of dates between start end in seconds
     *
     * @param start
     * @param end
     * @return
     */
    fun getAllDatesInPeriod(start: Long, end: Long): List<Long> {
        return getAllDatesInPeriod(start, end, null)
    }

    fun getAllDatesInPeriod(start: Long, end: Long, timeZone: TimeZone?): List<Long> {
        var calStart = Calendar.getInstance(marketTimeZone)
        if (timeZone != null) calStart = Calendar.getInstance(timeZone)
        calStart.timeInMillis = start * 1000
        var calEnd = Calendar.getInstance(marketTimeZone)
        if (timeZone != null) calEnd = Calendar.getInstance(timeZone)
        calEnd.timeInMillis = end * 1000
        setCalendarStartOfDayTime(calStart)
        setCalendarStartOfDayTime(calEnd)
        val datesList: MutableList<Long> = ArrayList()
        var calCurrent = Calendar.getInstance(marketTimeZone)
        if (timeZone != null) calCurrent = Calendar.getInstance(timeZone)
        calCurrent.timeInMillis = start * 1000
        setCalendarStartOfDayTime(calCurrent)
        calCurrent.add(Calendar.DAY_OF_MONTH, 1)
        datesList.add(calStart.timeInMillis / 1000)
        while (calCurrent.timeInMillis <= calEnd.timeInMillis) {
            datesList.add(calCurrent.timeInMillis / 1000)
            calCurrent.add(Calendar.DAY_OF_MONTH, 1)
        }
        return datesList
    }

    @JvmOverloads
    fun returnMonthYearString(
        noOfMonthsAgo: Int,
        timeZone: TimeZone? = null
    ): String {
        var cal = Calendar.getInstance(marketTimeZone)
        if (timeZone != null) cal = Calendar.getInstance(timeZone)
        cal.add(Calendar.MONTH, -noOfMonthsAgo)
        cal[Calendar.DAY_OF_MONTH] = 1
        return returnFullMonthYear(cal.timeInMillis / 1000)
    }

    fun convert24hrTo12hrsFormat(time: String?): String {
        var updatedTime = ""
        try {
            val sdf = SimpleDateFormat("H:mm")
            val dateObj = sdf.parse(time)
            updatedTime = SimpleDateFormat("h:mm a").format(dateObj)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return updatedTime
    }

    fun convert12hrTo24hrsFormat(time: String?): String {
        var updatedTime = ""
        try {
            val sdf = SimpleDateFormat("h:mm a")
            val dateObj = sdf.parse(time)
            updatedTime = SimpleDateFormat("H:mm").format(dateObj)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return updatedTime
    }

    @JvmStatic
    fun returnStartDateEndDateCustomRange(
        startDate: Calendar,
        endDate: Calendar
    ): StartDateEndDate {
        return StartDateEndDate(startDate.timeInMillis / 1000, endDate.timeInMillis / 1000)
    }

    fun setCalendarStartOfDayTime(calendar: Calendar) {
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
    }

    @JvmStatic
    fun setCalendarEndOfDayTime(calendar: Calendar) {
        calendar[Calendar.HOUR_OF_DAY] = 23
        calendar[Calendar.MINUTE] = 59
        calendar[Calendar.SECOND] = 59
        calendar[Calendar.MILLISECOND] = 999
    }

    fun getDaysBetweenDates(startDate: Long, endDate: Long): Int {
        return getDaysBetweenDates(startDate, endDate, null)
    }

    fun getDaysBetweenDates(startDate: Long, endDate: Long, timeZone: TimeZone?): Int {
        var days = 0
        var startDateCustom = Calendar.getInstance(marketTimeZone)
        if (timeZone != null) startDateCustom = Calendar.getInstance(timeZone)
        startDateCustom.timeInMillis = startDate
        setCalendarStartOfDayTime(startDateCustom)
        var endDateCustom = Calendar.getInstance(marketTimeZone)
        if (timeZone != null) endDateCustom = Calendar.getInstance(timeZone)
        endDateCustom.timeInMillis = endDate
        //endDateCustom.add(Calendar.DAY_OF_MONTH, 1);
        setCalendarStartOfDayTime(endDateCustom)
        days = TimeUnit.MILLISECONDS.toDays(
            Math.abs(endDateCustom.timeInMillis - startDateCustom.timeInMillis)
        ).toInt()
        return days
    }

    /**
     * return the date in format MMMM yyyy
     *
     * @param date
     * @return
     */
    @JvmOverloads
    fun returnFullMonthYear(date: Long, timeZone: TimeZone? = null): String {
        var ret = ""
        val sdf = SimpleDateFormat(DATE_FORMAT_MMMM_yyyy, Locale.ENGLISH)
        sdf.timeZone = marketTimeZone
        if (timeZone != null) sdf.timeZone = timeZone
        try {
            ret = sdf.format(Date(date * 1000L))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.d("Date returned", ret)
        return ret
    }

    fun getStandardDateFormat(value: Long): Date {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = value * 1000L
        return cal.time
    }

    /**
     * returns the date in d MMM HH:mm format
     *
     * @param date
     * @return
     */
    fun getDateInDateMonthHourMinuteFormat(date: Long): String {
        return getDateInDateMonthHourMinuteFormat(date, null)
    }

    fun getDateInDateMonthHourMinuteFormat(date: Long, timeZone: TimeZone?): String {
        var ret = ""
        val sdf = SimpleDateFormat(DATE_FORMAT_d_MMM_HH_mm, Locale.ENGLISH)
        sdf.timeZone = marketTimeZone
        if (timeZone != null) sdf.timeZone = timeZone
        try {
            ret = sdf.format(Date(date * 1000L))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.d("Date returned", ret)
        return ret
    }

    /**
     * return the date in EEE, d MMM yyyy format
     *
     * @param date
     * @return
     */
    fun getDateInDayDateMonthYearFormat(date: Long): String {
        return getDateInDayDateMonthYearFormat(date, null)
    }

    fun getDateInDayDateMonthYearFormat(date: Long, timeZone: TimeZone?): String {
        var ret = ""
        val sdf = SimpleDateFormat(DATE_FORMAT_EEE_d_MMM_yyyy, Locale.ENGLISH)
        sdf.timeZone = marketTimeZone
        if (timeZone != null) sdf.timeZone = timeZone
        try {
            ret = sdf.format(Date(date * 1000L))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.d("Date returned", ret)
        return ret
    }

    /**
     * get date in format EEE, d MMM yyyy
     *
     * @param date
     * @return
     */
    fun getDateInFullDayDateMonthYearFormat(date: Long): String {
        return getDateInFullDayDateMonthYearFormat(date, null)
    }

    fun getDateInFullDayDateMonthYearFormat(date: Long, timeZone: TimeZone?): String {
        var ret = ""
        val sdf = SimpleDateFormat(DATE_FORMAT_EEEE_d_MMM_yyyy, Locale.ENGLISH)
        sdf.timeZone = marketTimeZone
        if (timeZone != null) sdf.timeZone = timeZone
        try {
            ret = sdf.format(Date(date * 1000L))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.d("Date returned", ret)
        return ret
    }

    /**
     * @param date
     * @return
     */
    fun getDateInYearMonthDayHourMinSec(date: Long): String {
        return getDateInYearMonthDayHourMinSec(date, null)
    }

    fun getDateInYearMonthDayHourMinSec(date: Long, timeZone: TimeZone?): String {
        var ret = ""
        try {
            val sdf = SimpleDateFormat(DATE_FORMAT_yyyy_MM_dd_HH_mm_ss, Locale.ENGLISH)
            sdf.timeZone = marketTimeZone
            if (timeZone != null) sdf.timeZone = timeZone
            ret = sdf.format(Date(date * 1000L))
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        Log.d("Date returned", ret)
        return ret
    }

    fun getDateInHourMinFormat(dateTime: Long?): String {
        return getDateInHourMinFormat(dateTime, null)
    }

    @JvmStatic
    fun getDateInHourMinFormat(dateTime: Long?, timeZone: TimeZone?): String {
        if (dateTime != null) {
            val dateTimeString = java.lang.Long.toString(dateTime)
            val epoch = dateTimeString.toLong()
            val mDate = Date(epoch * 1000)
            val sdf =
                SimpleDateFormat(DATE_FORMAT_HH_mm, Locale.ENGLISH)
            sdf.timeZone = marketTimeZone
            if (timeZone != null) sdf.timeZone = timeZone
            return sdf.format(mDate)
        }
        return ""
    }

    /**
     * returns the weekDay, hours, minutes, seconds difference between two dates
     *
     * @param date1
     * @param date2
     * @return
     */
    fun computeDateDiff(date1: Date, date2: Date): Map<TimeUnit?, Long> {
        val diffInMillies = date2.time - date1.time
        val units: List<TimeUnit?> = ArrayList(
            EnumSet.allOf(
                TimeUnit::class.java
            )
        )
        Collections.reverse(units)
        val result: MutableMap<TimeUnit?, Long> = LinkedHashMap()
        var milliesRest = diffInMillies
        for (unit in units) {
            val diff = unit!!.convert(milliesRest, TimeUnit.MILLISECONDS)
            val diffInMilliesForUnit = unit.toMillis(diff)
            milliesRest = milliesRest - diffInMilliesForUnit
            result[unit] = diff
        }
        return result
    }

    fun getGivenDateStartEndTime(givenDate: Calendar): StartDateEndDate {
        val calDateStartOfDay = givenDate.clone() as Calendar
        val calDateEndOfDay = givenDate.clone() as Calendar
        setCalendarStartOfDayTime(calDateStartOfDay)
        setCalendarEndOfDayTime(calDateEndOfDay)
        return StartDateEndDate(
            calDateStartOfDay.timeInMillis / 1000,
            calDateEndOfDay.timeInMillis / 1000
        )
    }

    @JvmStatic
    fun getPaymentStartEndDate(paymentDue: PaymentDue): StartDateEndDate {
        val startDate =
            Calendar.getInstance(marketTimeZone)
        startDate.add(Calendar.DATE, paymentDue.beforeDays)
        val endDate = Calendar.getInstance(marketTimeZone)
        endDate.add(Calendar.DATE, paymentDue.afterDays)
        return StartDateEndDate(startDate.timeInMillis / 1000, endDate.timeInMillis / 1000)
    }

    enum class WeekMonth(val value: Int) {
        CURRENT_WEEK(0), LAST_WEEK(7), LAST_TO_LAST_WEEK(14), TWO_WEEKS_AGO(21), CURRENT_MONTH(0), LAST_MONTH(
            1
        ),
        LAST_TO_LAST_MONTH(2), TWO_MONTHS_AGO(3);

    }

    enum class PaymentDue(var beforeDays: Int, var afterDays: Int) {
        DEFAULT(-30, +29), OVER_DUE(-30, -1), DUE_IN_7DAYS(0, +6);

    }

    class StartDateEndDate(
        @field:Expose val startDateMillis: Long,
        @field:Expose val endDateMillis: Long
    ) {

        @Expose
        val startDateMonthYearString //format d MMM yyyy
                : String

        @Expose
        val endDateMonthYearString //format d MMMM yyyy
                : String

        @Expose
        val startDateMonthString //format d MMM
                : String

        @Expose
        val endDateMonthString //format d MMM
                : String

        @Expose
        val startEndRangeDateMonthFormat //d MMM - d MMM
                : String

        @Expose
        val startEndRangeDateMonthYearFormat //d MMM yyyy - d MMMM yyyy
                : String

        init {
            startDateMonthYearString = getDateInDateMonthYearFormat(startDateMillis)
            endDateMonthYearString = getDateInDateMonthYearFormat(endDateMillis)
            startDateMonthString = getDateInDateMonthFormat(startDateMillis)
            endDateMonthString = getDateInDateMonthFormat(endDateMillis)
            startEndRangeDateMonthFormat = "$startDateMonthString - $endDateMonthString"
            startEndRangeDateMonthYearFormat = "$startDateMonthYearString - $endDateMonthYearString"
        }
    }
}