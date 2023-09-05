package zeemart.asia.buyers.helper

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import zeemart.asia.buyers.R
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.models.invoice.Invoice
import zeemart.asia.buyers.models.order.Orders
import java.util.*

/**
 * Created by saiful on 3/7/18.
 */
object EmailHelper {
    fun SendFeedback(context: Context) {
        val emailIntent =
            Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + ServiceConstant.EMAIL_SUPPORT))
        //        emailIntent.setType(ServiceConstant.FILE_TYPE_PLAIN_TEXT);
//        emailIntent.setData(Uri.parse("mailto:"));
//        emailIntent.setType("message/rfc822");

//        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{ServiceConstant.EMAIL_SUPPORT});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.email_title_feedback))
        var body = ""
        val release = Build.VERSION.RELEASE
        val sdkVersion = Build.VERSION.SDK_INT
        val osVersion = "$sdkVersion ($release)"
        var appVersion = "-"
        val device = Build.BRAND.uppercase(Locale.getDefault()) + " " + Build.MODEL
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            appVersion = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        body += String.format(
            context.getString(R.string.email_fields_name),
            SharedPref.currentUserDetail.firstName + " " + SharedPref.currentUserDetail.lastName
        )
        body += String.format(
            context.getString(R.string.email_fields_user_id),
            SharedPref.read(SharedPref.USERNAME, "")
        )
        body += String.format(context.getString(R.string.email_fields_os_version), osVersion)
        body += String.format(context.getString(R.string.email_fields_app_version), appVersion)
        body += String.format(context.getString(R.string.email_fields_device), device)
        emailIntent.putExtra(Intent.EXTRA_TEXT, body)
        try {
            context.startActivity(emailIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun SendFeedback(context: Context, mOrder: Orders) {
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(ServiceConstant.EMAIL_SUPPORT))
        emailIntent.putExtra(
            Intent.EXTRA_SUBJECT,
            String.format(
                context.getString(R.string.email_title_issue_mistake_order),
                mOrder.orderId
            )
        )
        emailIntent.type = ServiceConstant.FILE_TYPE_PLAIN_TEXT
        var body = ""
        val release = Build.VERSION.RELEASE
        val sdkVersion = Build.VERSION.SDK_INT
        val osVersion = "$sdkVersion ($release)"
        var appVersion = "-"
        val device = Build.BRAND.uppercase(Locale.getDefault()) + " " + Build.MODEL
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            appVersion = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        body += String.format(context.getString(R.string.email_fields_order_id), mOrder.orderId)
        body += String.format(
            context.getString(R.string.email_fields_supplier),
            mOrder.supplier!!.supplierName
        )
        body += context.getString(R.string.email_fields_desc_mistake)
        body += String.format(
            context.getString(R.string.email_fields_name),
            SharedPref.currentUserDetail.firstName + " " + SharedPref.currentUserDetail.lastName
        )
        body += String.format(
            context.getString(R.string.email_fields_user_id),
            SharedPref.read(SharedPref.USERNAME, "")
        )
        body += String.format(context.getString(R.string.email_fields_os_version), osVersion)
        body += String.format(context.getString(R.string.email_fields_app_version), appVersion)
        body += String.format(context.getString(R.string.email_fields_device), device)
        emailIntent.putExtra(Intent.EXTRA_TEXT, body)
        try {
            context.startActivity(emailIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun SendFeedback(context: Context, invoice: Invoice) {
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(ServiceConstant.EMAIL_SUPPORT))
        emailIntent.putExtra(
            Intent.EXTRA_SUBJECT,
            String.format(
                context.getString(R.string.email_title_issue_mistake_invoice),
                invoice.invoiceNum
            )
        )
        emailIntent.type = ServiceConstant.FILE_TYPE_PLAIN_TEXT
        var body = ""
        val release = Build.VERSION.RELEASE
        val sdkVersion = Build.VERSION.SDK_INT
        val osVersion = "$sdkVersion ($release)"
        var appVersion = "-"
        val device = Build.BRAND.uppercase(Locale.getDefault()) + " " + Build.MODEL
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            appVersion = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        body += String.format(
            context.getString(R.string.email_fields_invoice_num),
            invoice.invoiceNum
        )
        body += String.format(
            context.getString(R.string.email_fields_supplier),
            invoice.supplier!!.supplierName
        )
        body += context.getString(R.string.email_fields_desc_mistake)
        body += String.format(
            context.getString(R.string.email_fields_name),
            SharedPref.currentUserDetail.firstName + " " + SharedPref.currentUserDetail.lastName
        )
        body += String.format(
            context.getString(R.string.email_fields_user_id),
            SharedPref.read(SharedPref.USERNAME, "")
        )
        body += String.format(context.getString(R.string.email_fields_os_version), osVersion)
        body += String.format(context.getString(R.string.email_fields_app_version), appVersion)
        body += String.format(context.getString(R.string.email_fields_device), device)
        emailIntent.putExtra(Intent.EXTRA_TEXT, body)
        try {
            context.startActivity(emailIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}