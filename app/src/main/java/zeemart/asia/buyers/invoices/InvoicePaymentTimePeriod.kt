package zeemart.asia.buyers.invoices

/**
 * Created by RajPrudhviMarella on 10/2/2020.
 */
enum class InvoicePaymentTimePeriod(val timePeriodValue: String) {
    DEFAULT("Default"), OVERDUE("Overdue"), DUEIN7DAYS("Due with in 7days"), CUSTOM("Custom");

}