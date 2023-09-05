package zeemart.asia.buyers.helper

import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.borax12.materialdaterangepicker.date.DatePickerDialog
import com.stripe.android.ApiResultCallback
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import com.stripe.android.model.Token
import com.stripe.android.view.CardInputListener
import com.stripe.android.view.CardInputWidget
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.adapter.*
import zeemart.asia.buyers.adapter.GRNUomSelectAdapter.SelectedUOMListener
import zeemart.asia.buyers.adapter.PaymentCardListAdapter.PayCardSelectedListener
import zeemart.asia.buyers.adapter.ProductsFilterListingAdapter.ProductListSelectedFilterCategoriesItemClickListeneremptydata
import zeemart.asia.buyers.adapter.ProductsFilterListingAdapter.ProductListSelectedFilterSupplierItemClickListener
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.customviews.CustomLoadingViewWhite
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.DeliveryDateSelectedListener
import zeemart.asia.buyers.interfaces.EditInvoiceListener
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.invoices.InQueueForUploadDataModel
import zeemart.asia.buyers.invoices.InvoiceCameraPreview
import zeemart.asia.buyers.models.*
import zeemart.asia.buyers.models.invoice.Invoice
import zeemart.asia.buyers.models.invoice.InvoiceUploadsListDataMgr
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel
import zeemart.asia.buyers.models.order.Orders
import zeemart.asia.buyers.models.order.Orders.Grn
import zeemart.asia.buyers.models.paymentimport.PaymentCardDetails
import zeemart.asia.buyers.models.paymentimport.PaymentStatus
import zeemart.asia.buyers.modelsimport.OrderSettingDeliveryPreferences
import zeemart.asia.buyers.network.*
import zeemart.asia.buyers.network.PaymentApi.CompanyCardDetailsListener
import zeemart.asia.buyers.orders.OrderDetailsActivity
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by saiful on 22/3/18.
 */
object DialogHelper {
    fun GetDatePickerFromTo(
        ctx: DatePickerDialog.OnDateSetListener?,
        from: String?,
        to: String?,
    ): DatePickerDialog {
        val startDateCal = Calendar.getInstance(DateHelper.marketTimeZone)
        val endDateCal = Calendar.getInstance(DateHelper.marketTimeZone)
        try {
            val startDateLong = DateHelper.returnEpochTimeSOD(from)
            val endDateLong = DateHelper.returnEpochTimeEOD(to)
            startDateCal.timeInMillis = startDateLong * 1000
            endDateCal.timeInMillis = endDateLong * 1000
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return GetDatePickerFromTo(ctx, startDateCal, endDateCal)
    }

    fun GetDatePickerFromTo(
        ctx: DatePickerDialog.OnDateSetListener?,
        from: Calendar,
        to: Calendar,
    ): DatePickerDialog {
        return DatePickerDialog.newInstance(
            ctx,
            from[Calendar.YEAR],
            from[Calendar.MONTH],
            from[Calendar.DAY_OF_MONTH],
            to[Calendar.YEAR],
            to[Calendar.MONTH],
            to[Calendar.DAY_OF_MONTH]
        )
    }

    fun ShowRejectOrder(context: Context, callback: RejectOrderCallback) {
        val alertDialogBuilder = AlertDialog.Builder(context, R.style.Widget_ListView_White)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(zeemart.asia.buyers.R.layout.dialog_reject_order, null)
        alertDialogBuilder.setView(view)
        alertDialogBuilder.setCancelable(true)
        val dialog = alertDialogBuilder.create()
        val optionsLayout = view.findViewById<LinearLayout>(zeemart.asia.buyers.R.id.optionsLayout)
        val option_select_1 =
            view.findViewById<LinearLayout>(zeemart.asia.buyers.R.id.option_select_1)
        val option_img_1 = view.findViewById<ImageView>(zeemart.asia.buyers.R.id.option_img_1)
        val option_txt_1 = view.findViewById<TextView>(zeemart.asia.buyers.R.id.option_txt_1)
        ZeemartBuyerApp.setTypefaceView(
            option_txt_1,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val option_select_2 =
            view.findViewById<LinearLayout>(zeemart.asia.buyers.R.id.option_select_2)
        val option_img_2 = view.findViewById<ImageView>(zeemart.asia.buyers.R.id.option_img_2)
        val option_txt_2 = view.findViewById<TextView>(zeemart.asia.buyers.R.id.option_txt_2)
        ZeemartBuyerApp.setTypefaceView(
            option_txt_2,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val option_select_3 =
            view.findViewById<LinearLayout>(zeemart.asia.buyers.R.id.option_select_3)
        val option_img_3 = view.findViewById<ImageView>(zeemart.asia.buyers.R.id.option_img_3)
        val option_txt_3 = view.findViewById<TextView>(zeemart.asia.buyers.R.id.option_txt_3)
        ZeemartBuyerApp.setTypefaceView(
            option_txt_3,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val option_select_4 =
            view.findViewById<LinearLayout>(zeemart.asia.buyers.R.id.option_select_4)
        val option_txt_4a = view.findViewById<TextView>(zeemart.asia.buyers.R.id.option_txt_4a)
        ZeemartBuyerApp.setTypefaceView(
            option_txt_4a,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val option_txt_4b = view.findViewById<TextView>(zeemart.asia.buyers.R.id.option_txt_4b)
        ZeemartBuyerApp.setTypefaceView(
            option_txt_4b,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val remarkET = view.findViewById<EditText>(zeemart.asia.buyers.R.id.remarksET)
        ZeemartBuyerApp.setTypefaceView(
            remarkET,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        class OptionSelect {
            fun select(selectedLayout: LinearLayout?) {
                if (selectedLayout != null) {
                    optionsLayout.visibility = View.VISIBLE
                    remarkET.visibility = View.GONE
                    option_img_1.visibility = View.GONE
                    option_img_2.visibility = View.GONE
                    option_img_3.visibility = View.GONE
                    if (selectedLayout === option_select_1) {
                        option_img_1.visibility = View.VISIBLE
                    } else if (selectedLayout === option_select_2) {
                        option_img_2.visibility = View.VISIBLE
                    } else if (selectedLayout === option_select_3) {
                        option_img_3.visibility = View.VISIBLE
                    } else if (selectedLayout === option_select_4) {
                        optionsLayout.visibility = View.GONE
                        remarkET.visibility = View.VISIBLE
                        remarkET.requestFocus()
                        inputMethodManager.showSoftInput(remarkET, InputMethodManager.SHOW_IMPLICIT)
                    }
                } else {
                    optionsLayout.visibility = View.GONE
                    remarkET.visibility = View.VISIBLE
                    option_img_1.visibility = View.GONE
                    option_img_2.visibility = View.GONE
                    option_img_3.visibility = View.GONE
                }
            }
        }

        val optionSelect = OptionSelect()
        option_select_1.setOnClickListener { optionSelect.select(option_select_1) }
        option_select_2.setOnClickListener { optionSelect.select(option_select_2) }
        option_select_3.setOnClickListener { optionSelect.select(option_select_3) }
        option_select_4.setOnClickListener { optionSelect.select(option_select_4) }
        val titleTV = view.findViewById<TextView>(zeemart.asia.buyers.R.id.titleTV)
        ZeemartBuyerApp.setTypefaceView(
            titleTV,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        val noteTV = view.findViewById<TextView>(zeemart.asia.buyers.R.id.noteTV)
        ZeemartBuyerApp.setTypefaceView(
            noteTV,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val submitBtn = view.findViewById<Button>(zeemart.asia.buyers.R.id.submitBtn)
        ZeemartBuyerApp.setTypefaceView(
            submitBtn,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        val closeBtn = view.findViewById<ImageButton>(zeemart.asia.buyers.R.id.closeBtn)
        closeBtn.setOnClickListener {
            inputMethodManager.hideSoftInputFromWindow(
                view.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
            dialog.dismiss()
        }
        submitBtn.setOnClickListener {
            inputMethodManager.hideSoftInputFromWindow(
                view.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
            var remark = ""
            if (option_img_1.visibility == View.VISIBLE) {
                remark = option_txt_1.text.toString()
            } else if (option_img_2.visibility == View.VISIBLE) {
                remark = option_txt_2.text.toString()
            } else if (option_img_3.visibility == View.VISIBLE) {
                remark = option_txt_3.text.toString()
            } else {
                try {
                    remark = remarkET.text.toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            callback.rejectOrder(remark)
            dialog.dismiss()
        }
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        dialog.setOnDismissListener {
            inputMethodManager.hideSoftInputFromWindow(
                view.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
            callback.dismiss()
        }
        if (context is Activity && !context.isFinishing) {
            dialog.show()
        }
    }

    @JvmStatic
    fun ShowLanguageSelection(context: Context, callback: LanguageSelectionCallback) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(zeemart.asia.buyers.R.string.lang_select_language))
        val lang = arrayOf(
            context.getString(ZeemartAppConstants.Language.ENGLISH.resId),
            context.getString(ZeemartAppConstants.Language.CHINESE.resId),
            context.getString(ZeemartAppConstants.Language.BAHASA_INDONESIA.resId)
        )
        builder.setItems(lang) { dialog, which ->
            when (which) {
                0 -> {
                    callback.selected(
                        ZeemartAppConstants.Language.ENGLISH.locale,
                        ZeemartAppConstants.Language.ENGLISH.locLangCode
                    )
                    SharedPref.write(
                        SharedPref.SELECTED_LANGUAGE,
                        ZeemartAppConstants.Language.ENGLISH.langCode
                    )
                }
                1 -> {
                    callback.selected(
                        ZeemartAppConstants.Language.CHINESE.locale,
                        ZeemartAppConstants.Language.CHINESE.locLangCode
                    )
                    SharedPref.write(
                        SharedPref.SELECTED_LANGUAGE,
                        ZeemartAppConstants.Language.CHINESE.langCode
                    )
                }
                2 -> {
                    callback.selected(
                        ZeemartAppConstants.Language.BAHASA_INDONESIA.locale,
                        ZeemartAppConstants.Language.BAHASA_INDONESIA.locLangCode
                    )
                    SharedPref.write(
                        SharedPref.SELECTED_LANGUAGE,
                        ZeemartAppConstants.Language.BAHASA_INDONESIA.langCode
                    )
                }
                else -> {
                    callback.selected(
                        ZeemartAppConstants.Language.ENGLISH.locale,
                        ZeemartAppConstants.Language.ENGLISH.locLangCode
                    )
                    SharedPref.write(
                        SharedPref.SELECTED_LANGUAGE,
                        ZeemartAppConstants.Language.ENGLISH.langCode
                    )
                }
            }
            dialog.dismiss()
        }
        val dialog = builder.create()
        if (context is Activity && !context.isFinishing) {
            dialog.show()
        }
    }

    @JvmStatic
    fun ShowCancelOrder(context: Context, callback: CancelOrderCallback) {
        val alertDialogBuilder = AlertDialog.Builder(context, R.style.Widget_ListView_White)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(zeemart.asia.buyers.R.layout.dialog_cancel_order, null)
        alertDialogBuilder.setView(view)
        alertDialogBuilder.setCancelable(true)
        val dialog = alertDialogBuilder.create()
        val optionsLayout = view.findViewById<LinearLayout>(zeemart.asia.buyers.R.id.optionsLayout)
        val editreasonLayout =
            view.findViewById<LinearLayout>(zeemart.asia.buyers.R.id.lyt_edit_reason)
        val remarkET = view.findViewById<EditText>(zeemart.asia.buyers.R.id.remarksET)
        val submitButton = view.findViewById<Button>(zeemart.asia.buyers.R.id.submitBtn)
        val option_select_1 =
            view.findViewById<LinearLayout>(zeemart.asia.buyers.R.id.option_select_1)
        val option_img_1 = view.findViewById<ImageView>(zeemart.asia.buyers.R.id.option_img_1)
        val option_txt_1 = view.findViewById<TextView>(zeemart.asia.buyers.R.id.option_txt_1)
        ZeemartBuyerApp.setTypefaceView(
            option_txt_1,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val option_select_2 =
            view.findViewById<LinearLayout>(zeemart.asia.buyers.R.id.option_select_2)
        val option_img_2 = view.findViewById<ImageView>(zeemart.asia.buyers.R.id.option_img_2)
        val option_txt_2 = view.findViewById<TextView>(zeemart.asia.buyers.R.id.option_txt_2)
        ZeemartBuyerApp.setTypefaceView(
            option_txt_2,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val option_select_3 =
            view.findViewById<LinearLayout>(zeemart.asia.buyers.R.id.option_select_3)
        val option_txt_3a = view.findViewById<TextView>(zeemart.asia.buyers.R.id.option_txt_3a)
        ZeemartBuyerApp.setTypefaceView(
            option_txt_3a,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val option_txt_3b = view.findViewById<TextView>(zeemart.asia.buyers.R.id.option_txt_3b)
        ZeemartBuyerApp.setTypefaceView(
            option_txt_3b,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        @SuppressLint("MissingInflatedId", "LocalSuppress") val noteTV =
            view.findViewById<TextView>(zeemart.asia.buyers.R.id.noteTV)
        ZeemartBuyerApp.setTypefaceView(
            noteTV,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        class OptionSelect {
            fun select(selectedLayout: LinearLayout?) {
                if (selectedLayout != null) {
                    optionsLayout.visibility = View.VISIBLE
                    editreasonLayout.visibility = View.GONE
                    option_img_1.visibility = View.GONE
                    option_img_2.visibility = View.GONE
                    if (selectedLayout === option_select_1) {
                        option_img_1.visibility = View.VISIBLE
                        submitButton.isEnabled = true
                        submitButton.setBackgroundResource(zeemart.asia.buyers.R.color.pinky_red)
                    } else if (selectedLayout === option_select_2) {
                        option_img_2.visibility = View.VISIBLE
                        submitButton.isEnabled = true
                        submitButton.setBackgroundResource(zeemart.asia.buyers.R.color.pinky_red)
                    } else if (selectedLayout === option_select_3) {
                        remarkET.requestFocus()
                        inputMethodManager.showSoftInput(remarkET, InputMethodManager.SHOW_IMPLICIT)
                        optionsLayout.visibility = View.GONE
                        editreasonLayout.visibility = View.VISIBLE
                        submitButton.isEnabled = false
                        submitButton.setBackgroundResource(zeemart.asia.buyers.R.color.dark_grey)
                    }
                } else {
                    optionsLayout.visibility = View.GONE
                    editreasonLayout.visibility = View.VISIBLE
                    option_img_1.visibility = View.GONE
                    option_img_2.visibility = View.GONE
                }
            }
        }

        val optionSelect = OptionSelect()
        option_select_1.setOnClickListener { optionSelect.select(option_select_1) }
        option_select_2.setOnClickListener { optionSelect.select(option_select_2) }
        option_select_3.setOnClickListener { optionSelect.select(option_select_3) }
        submitButton.isEnabled = false
        submitButton.setBackgroundResource(zeemart.asia.buyers.R.color.dark_grey)
        remarkET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (remarkET.text.toString().trim { it <= ' ' }.length == 0) {
                    submitButton.isEnabled = false
                    submitButton.setBackgroundResource(zeemart.asia.buyers.R.color.dark_grey)
                } else {
                    submitButton.isEnabled = true
                    submitButton.setBackgroundResource(zeemart.asia.buyers.R.color.pinky_red)
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        submitButton.setOnClickListener {
            inputMethodManager.hideSoftInputFromWindow(
                view.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
            var remark = ""
            if (option_img_1.visibility == View.VISIBLE) {
                remark = option_txt_1.text.toString()
            } else if (option_img_2.visibility == View.VISIBLE) {
                remark = option_txt_2.text.toString()
            } else {
                try {
                    remark = remarkET.text.toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            editreasonLayout.visibility = View.GONE
            optionsLayout.visibility = View.VISIBLE
            callback.cancelOrder(remark)
            dialog.dismiss()
        }
        val closeBtn = view.findViewById<ImageButton>(zeemart.asia.buyers.R.id.closeBtn)
        closeBtn.setOnClickListener {
            inputMethodManager.hideSoftInputFromWindow(
                view.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
            dialog.dismiss()
        }
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        dialog.setOnDismissListener {
            inputMethodManager.hideSoftInputFromWindow(
                view.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
            callback.dismiss()
        }
        if (context is Activity && !context.isFinishing) {
            dialog.show()
        }
    }

    @JvmStatic
    fun receiveConfirmationDialog(
        context: Context,
        order: Orders,
        action: String?,
        receiveOrderListener: ReceiveOrderListener,
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(zeemart.asia.buyers.R.string.txt_mark_order_receive)
        builder.setPositiveButton(context.getString(zeemart.asia.buyers.R.string.txt_yes)) { dialog, which ->
            AnalyticsHelper.logAction(context, action, order)
            OrderHelper.receivedOrders(
                context,
                order.orderId,
                order.outlet!!,
                object : OrderHelper.RequestResponseListener {
                    override fun requestSuccessful() {
                        receiveOrderListener.onReceiveSuccessful()
                    }

                    override fun requestError() {
                        receiveOrderListener.onDialogDismiss()
                    }
                })
            dialog.dismiss()
        }
        builder.setNegativeButton(context.getString(zeemart.asia.buyers.R.string.txt_no)) { dialog, which -> }
        val d = builder.create()
        d.setOnDismissListener { receiveOrderListener.onDialogDismiss() }
        if (context is Activity && !context.isFinishing) {
            d.show()
        }
    }

    @JvmStatic
    fun displayErrorMessageDialog(context: Context, title: String?, message: String?): AlertDialog {
        val builder = AlertDialog.Builder(context)
        if (title != null) builder.setTitle(title)
        if (message != null) builder.setMessage(message)
        builder.setPositiveButton(context.getString(zeemart.asia.buyers.R.string.dialog_ok_button_text)) { dialog, which -> dialog.dismiss() }
        val d = builder.create()
        if (context is Activity && !context.isFinishing) {
            d.show()
        }
        return d
    }

    @JvmStatic
    fun alertDialogSmallSuccess(activity: Activity, successMessage: String?): AlertDialog {
        val DIALOG_LAYOUT_WIDTH = CommonMethods.dpToPx(150)
        val DIALOG_LAYOUT_HEIGHT = CommonMethods.dpToPx(150)
        val builder = AlertDialog.Builder(activity)
        val v =
            activity.layoutInflater.inflate(zeemart.asia.buyers.R.layout.small_dialog_success, null)
        val txtMessage = v.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_small_dialog_success)
        ZeemartBuyerApp.setTypefaceView(
            txtMessage,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        txtMessage.text = successMessage
        builder.setView(v)
        val dialog = builder.create()
        dialog.show()
        dialog.window!!.setLayout(DIALOG_LAYOUT_WIDTH, DIALOG_LAYOUT_HEIGHT)
        return dialog
    }

    @JvmStatic
    fun alertDialogSmallFailure(
        activity: Activity,
        failureHeading: String?,
        failureMessage: String?,
    ): AlertDialog {
        val DIALOG_LAYOUT_WIDTH = CommonMethods.dpToPx(150)
        val DIALOG_LAYOUT_HEIGHT = CommonMethods.dpToPx(180)
        val builder = AlertDialog.Builder(activity)
        val v =
            activity.layoutInflater.inflate(zeemart.asia.buyers.R.layout.small_dialog_failure, null)
        val txtHeading = v.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_small_dialog_failure)
        ZeemartBuyerApp.setTypefaceView(
            txtHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        txtHeading.text = failureHeading
        val txtMessage =
            v.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_small_dialog_failure_message)
        ZeemartBuyerApp.setTypefaceView(
            txtMessage,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        txtMessage.text = failureMessage
        builder.setView(v)
        val dialog = builder.create()
        dialog.show()
        dialog.window!!.setLayout(DIALOG_LAYOUT_WIDTH, DIALOG_LAYOUT_HEIGHT)
        return dialog
    }

    @JvmStatic
    fun setBasicAlertDialogWithTitleAndMessage(
        context: Context,
        title: String?,
        message: String?,
        negativeButtonText: String?,
        positiveButtonText: String?,
        listener: DialogButtonClickListener,
    ) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle(title)
        if (message != null) {
            alertDialogBuilder.setMessage(message)
        }
        alertDialogBuilder.setNegativeButton(negativeButtonText) { dialog, which -> listener.negativeButtonClicked() }
        alertDialogBuilder.setPositiveButton(positiveButtonText) { dialog, which -> listener.positiveButtonClicked() }
        alertDialogBuilder.setCancelable(true)
        val dialog = alertDialogBuilder.create()
        dialog.show()
        val negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        negativeButton.setTextColor(context.resources.getColor(zeemart.asia.buyers.R.color.pinky_red))
        val positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
        positiveButton.setTextColor(context.resources.getColor(zeemart.asia.buyers.R.color.color_azul_two))
    }

    @JvmStatic
    fun setThreeButtonAlertDialogWithTitleAndMessage(
        context: Context,
        title: String?,
        message: String?,
        negativeButtonText: String?,
        positiveButtonText: String?,
        neutralButtonText: String?,
        listener: DialogThreeButtonClickListener,
    ) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle(title)
        if (message != null) {
            alertDialogBuilder.setMessage(message)
        }
        alertDialogBuilder.setPositiveButton(positiveButtonText) { dialog, which -> listener.positiveButtonClicked() }
        alertDialogBuilder.setNegativeButton(negativeButtonText) { dialog, which -> listener.negativeButtonClicked() }
        alertDialogBuilder.setNeutralButton(neutralButtonText) { dialog, which -> listener.neutralButtonClicked() }
        alertDialogBuilder.setCancelable(true)
        val dialog = alertDialogBuilder.create()
        dialog.show()
        val negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        negativeButton.setTextColor(context.resources.getColor(zeemart.asia.buyers.R.color.pinky_red))
        val positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
        positiveButton.setTextColor(context.resources.getColor(zeemart.asia.buyers.R.color.color_azul_two))
        val nutralButton = dialog.getButton(DialogInterface.BUTTON_NEUTRAL)
        nutralButton.setTextColor(context.resources.getColor(zeemart.asia.buyers.R.color.color_azul_two))
    }

    @JvmStatic
    fun ShowGrnEmailSupplierDialog(context: Context, mGrn: Grn?) {
        val d = Dialog(context, zeemart.asia.buyers.R.style.CustomDialogForTagsTheme)
        d.setContentView(zeemart.asia.buyers.R.layout.layout_grn_show_email_supplier)
        d.window!!.setGravity(Gravity.BOTTOM)
        d.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        d.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        d.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        val dismissBG = d.findViewById<RelativeLayout>(zeemart.asia.buyers.R.id.dismiss_bg)
        dismissBG.setOnClickListener { d.dismiss() }
        val txtEmailSentTo = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_add_message)
        val txtSentBy = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_emails_list)
        val txtNotes = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_notes)
        ZeemartBuyerApp.setTypefaceView(
            txtEmailSentTo,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtSentBy,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNotes,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        if (mGrn != null) {
            if (mGrn.emailDate != null) {
                val datePeriodValue = DateHelper.getDateInDateMonthYearFormat(
                    mGrn.emailDate,
                    null
                ) + " " + DateHelper.getDateInHourMinFormat(mGrn.emailDate, null)
                txtEmailSentTo.text = String.format(
                    context.resources.getString(zeemart.asia.buyers.R.string.txt_email_sent_on),
                    datePeriodValue
                )
            }
            var name = ""
            if (mGrn.createdBy != null) {
                if (!StringHelper.isStringNullOrEmpty(mGrn.createdBy!!.firstName)) {
                    name = name + mGrn.createdBy!!.firstName
                } else if (!StringHelper.isStringNullOrEmpty(mGrn.createdBy!!.lastName)) {
                    name = name + mGrn.createdBy!!.lastName
                }
            }
            txtSentBy.text = String.format(
                context.resources.getString(zeemart.asia.buyers.R.string.txt_sent_by),
                name
            )
            if (!StringHelper.isStringNullOrEmpty(mGrn.noteToSupplier)) {
                txtNotes.text = "\"" + mGrn.noteToSupplier + "\""
            }
        }
        d.show()
    }

    @JvmStatic
    fun ShowEditGrnEmailSupplierDialog(
        context: Context,
        mOrder: Orders?,
        mGrn: Grn,
        mListener: onGrnEmailEditedListener,
    ) {
        val d = Dialog(context, zeemart.asia.buyers.R.style.CustomDialogForTagsTheme)
        d.setContentView(zeemart.asia.buyers.R.layout.layout_grn_edt_email_dialogue)
        d.window!!.setGravity(Gravity.BOTTOM)
        d.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        d.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        d.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        val dismissBG = d.findViewById<RelativeLayout>(zeemart.asia.buyers.R.id.dismiss_bg)
        val txtAddMessage = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_add_message)
        val txtEmailList = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_emails_list)
        val edtEmailSettings = d.findViewById<EditText>(zeemart.asia.buyers.R.id.edt_sku_name)
        val txtRemainingCharacters =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_characters_remaining)
        val btnSend = d.findViewById<Button>(zeemart.asia.buyers.R.id.btnchange_quantity_done)
        ZeemartBuyerApp.setTypefaceView(
            txtAddMessage,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            btnSend,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtEmailList,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtRemainingCharacters,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            edtEmailSettings,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        dismissBG.setOnClickListener {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(edtEmailSettings.windowToken, 0)
            d.dismiss()
        }
        if (mOrder != null && mOrder.emailStatusDetails != null && mOrder.emailStatusDetails!!.size > 0) {
            val emails = StringBuilder()
            for (i in mOrder.emailStatusDetails!!.indices) {
                if (i != mOrder.emailStatusDetails!!.size - 1) {
                    emails.append(mOrder.emailStatusDetails!![i].emailId).append(", ")
                } else {
                    emails.append(mOrder.emailStatusDetails!![i].emailId)
                }
            }
            mGrn.supplierNotificationEmail = emails.toString() + ""
            txtEmailList.text = String.format(
                context.resources.getString(zeemart.asia.buyers.R.string.txt_your_message_will_be_sent_to),
                emails
            )
        }
        btnSend.isClickable = false
        btnSend.isEnabled = false
        btnSend.alpha = 0.5f
        edtEmailSettings.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (!StringHelper.isStringNullOrEmpty(edtEmailSettings.text.toString()) && edtEmailSettings.text.length > 0) {
                    txtRemainingCharacters.text = String.format(
                        context.getString(zeemart.asia.buyers.R.string.txt_characters_remaining),
                        300 - edtEmailSettings.text.length
                    )
                    btnSend.isClickable = true
                    btnSend.isEnabled = true
                    btnSend.alpha = 1f
                } else {
                    btnSend.isClickable = false
                    btnSend.isEnabled = false
                    btnSend.alpha = 0.5f
                    txtRemainingCharacters.text = String.format(
                        context.getString(zeemart.asia.buyers.R.string.txt_characters_remaining),
                        300
                    )
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        btnSend.setOnClickListener {
            mListener.onSendSelected(
                edtEmailSettings.text.toString(),
                mGrn.supplierNotificationEmail
            )
            d.dismiss()
        }
        d.show()
    }

    @JvmStatic
    fun ShowAddNewCardDialog(
        context: Context, companyId: String?,
        mLis: onCardAddedListener,
    ) {
        val d = Dialog(context, zeemart.asia.buyers.R.style.CustomDialogForTags)
        d.setContentView(zeemart.asia.buyers.R.layout.activity_add_card)
        d.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        PaymentConfiguration.init(context, ZeemartBuyerApp.stripePublicKey)
        val cardInputWidget =
            d.findViewById<CardInputWidget>(zeemart.asia.buyers.R.id.cardInputWidget)
        val txtHeader = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_add_card_header)
        val txtByAddingCard = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_agree_terms)
        val txtTermsOfUse = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_terms_of_use)
        val btnSave = d.findViewById<Button>(zeemart.asia.buyers.R.id.btn_save_card)
        val isCardCompleted = booleanArrayOf(false)
        val isCVVCompleted = booleanArrayOf(false)
        val isExpiryMonthCompleted = booleanArrayOf(false)
        val threeDotLoader =
            d.findViewById<CustomLoadingViewWhite>(zeemart.asia.buyers.R.id.three_dot_loader)
        threeDotLoader.visibility = View.GONE
        val imgFinish = d.findViewById<ImageView>(zeemart.asia.buyers.R.id.btn_close_add_card)
        imgFinish.setOnClickListener { d.dismiss() }
        btnSave.setBackgroundColor(context.resources.getColor(zeemart.asia.buyers.R.color.grey_medium))
        btnSave.isClickable = false
        cardInputWidget.postalCodeEnabled = false
        cardInputWidget.setCvcNumberTextWatcher(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                btnSave.setBackgroundColor(context.resources.getColor(zeemart.asia.buyers.R.color.grey_medium))
                btnSave.isClickable = false
            }

            override fun afterTextChanged(s: Editable) {}
        })
        cardInputWidget.setExpiryDateTextWatcher(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                btnSave.setBackgroundColor(context.resources.getColor(zeemart.asia.buyers.R.color.grey_medium))
                btnSave.isClickable = false
            }

            override fun afterTextChanged(s: Editable) {}
        })
        cardInputWidget.setCardNumberTextWatcher(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                btnSave.setBackgroundColor(context.resources.getColor(zeemart.asia.buyers.R.color.grey_medium))
                btnSave.isClickable = false
            }

            override fun afterTextChanged(s: Editable) {}
        })
        cardInputWidget.setCardInputListener(object : CardInputListener {
            override fun onFocusChange(focusField: CardInputListener.FocusField) {}
            override fun onCardComplete() {
                isCardCompleted[0] = true
                if (isCardCompleted[0] && isCVVCompleted[0] && isExpiryMonthCompleted[0]) {
                    btnSave.setBackgroundColor(context.resources.getColor(zeemart.asia.buyers.R.color.green))
                    btnSave.isClickable = true
                } else {
                    btnSave.setBackgroundColor(context.resources.getColor(zeemart.asia.buyers.R.color.grey_medium))
                    btnSave.isClickable = false
                }
            }

            override fun onExpirationComplete() {
                isExpiryMonthCompleted[0] = true
                if (isCardCompleted[0] && isCVVCompleted[0] && isExpiryMonthCompleted[0]) {
                    btnSave.setBackgroundColor(context.resources.getColor(zeemart.asia.buyers.R.color.green))
                    btnSave.isClickable = true
                } else {
                    btnSave.setBackgroundColor(context.resources.getColor(zeemart.asia.buyers.R.color.grey_medium))
                    btnSave.isClickable = false
                }
            }

            override fun onCvcComplete() {
                isCVVCompleted[0] = true
                if (isCardCompleted[0] && isCVVCompleted[0] && isExpiryMonthCompleted[0]) {
                    btnSave.setBackgroundColor(context.resources.getColor(zeemart.asia.buyers.R.color.green))
                    btnSave.isClickable = true
                } else {
                    btnSave.setBackgroundColor(context.resources.getColor(zeemart.asia.buyers.R.color.grey_medium))
                    btnSave.isClickable = false
                }
            }
        })
        ZeemartBuyerApp.setTypefaceView(
            txtHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtByAddingCard,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtTermsOfUse,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            btnSave,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        btnSave.setOnClickListener {
            threeDotLoader.visibility = View.VISIBLE
            val cardParams = cardInputWidget.cardParams
            if (cardParams != null) {
                // Use the key from the server to initialize the Stripe instance.
                val stripe = Stripe(context, ZeemartBuyerApp.stripePublicKey)
                stripe.createCardToken(cardParams,null, null, object : ApiResultCallback<Token> {
                    override fun onSuccess(token: Token) {
                        val requestBody = AddCardRequestBody()
                        requestBody.cardLast4Digits = token.card!!.last4
                        requestBody.cardType = token.card!!.brand.displayName
                        requestBody.cardToken = token.id
                        requestBody.expMonth = token.card!!.expMonth
                        requestBody.expYear = token.card!!.expYear
                        requestBody.createdBy = SharedPref.currentUserDetail
                        requestBody.timeTokenGenerated = token.created.time
                        requestBody.companyId = companyId
                        PaymentApi.addPaymentCard(
                            context,
                            ZeemartBuyerApp.gsonExposeExclusive.toJson(requestBody),
                            object : GetRequestStatusResponseListener {
                                override fun onSuccessResponse(status: String?) {
                                    val addCardSuccessResponse =
                                        ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                                            status,
                                            AddCardSuccessResponse::class.java
                                        )
                                    ZeemartBuyerApp.getToastGreen("card added Successfully")
                                    val apiParamsHelper = ApiParamsHelper()
                                    apiParamsHelper.setCompanyId(companyId!!)
                                    PaymentApi.retrieveCompanyCardPaymentData(
                                        context,
                                        apiParamsHelper,
                                        SharedPref.defaultOutlet!!,
                                        object : CompanyCardDetailsListener {
                                            override fun onSuccessResponse(paymentCardDetails: PaymentCardDetails?) {
                                                for (i in paymentCardDetails?.data?.indices!!) {
                                                    if (paymentCardDetails.data!![i].status != null && paymentCardDetails.data!![i].status == PaymentStatus.ACTIVE.getmStatusName()) {
                                                        if (addCardSuccessResponse != null && addCardSuccessResponse.data != null) {
                                                            if (addCardSuccessResponse.data!!.customerId == paymentCardDetails.data!![i].customerCardData?.customerId) {
                                                                mLis.onCardSelected(
                                                                    paymentCardDetails.data!![i]
                                                                )
                                                                threeDotLoader.visibility =
                                                                    View.GONE
                                                                d.dismiss()
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            override fun onErrorResponse(error: VolleyErrorHelper?) {
                                                threeDotLoader.visibility = View.GONE
                                                d.dismiss()
                                            }
                                        })
                                }

                                override fun onErrorResponse(error: VolleyErrorHelper?) {
                                    threeDotLoader.visibility = View.GONE
                                    ZeemartBuyerApp.getToastRed("Didnt Successfully added card")
                                }
                            })
                    }

                    override fun onError(e: Exception) {
                        threeDotLoader.visibility = View.GONE
                    }
                })
            }
        }
        d.show()
    }

    @JvmStatic
    fun ShowUomFilterDialog(
        context: Context?,
        lstUnitSize: List<UnitSizeModel>, mLis: onUomSelectListener,
    ) {
        val d = Dialog(context!!, zeemart.asia.buyers.R.style.CustomDialogForTags)
        d.setContentView(zeemart.asia.buyers.R.layout.activity_grn_uoms_list)
        d.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        val txtHeader = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_add_card_header)
        val btnSave = d.findViewById<Button>(zeemart.asia.buyers.R.id.btn_save_card)
        val imgFinish = d.findViewById<ImageView>(zeemart.asia.buyers.R.id.btn_close_add_card)
        imgFinish.setOnClickListener { d.dismiss() }
        btnSave.setOnClickListener { d.dismiss() }
        ZeemartBuyerApp.setTypefaceView(
            txtHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            btnSave,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        val listView = d.findViewById<RecyclerView>(zeemart.asia.buyers.R.id.cardInputWidget)
        listView.layoutManager = LinearLayoutManager(context)
        listView.adapter = GRNUomSelectAdapter(context, lstUnitSize, object : SelectedUOMListener {
            override fun onSelectedUom(unitSize: String?) {
                mLis.onUomSelect(unitSize)
                d.dismiss()
            }
        })
        d.show()
    }

    @JvmStatic
    fun showOrderSettingsVerificationDialog(
        context: Context,
        outletName: String?, supplierName: String?,
        lstData: OrderSettingDeliveryPreferences.Data,
        mListener: onButtonSaveClickListener,
    ) {
        val d = Dialog(context, zeemart.asia.buyers.R.style.CustomDialogForTagsTheme)
        d.setContentView(zeemart.asia.buyers.R.layout.activity_order_settings_verification)
        d.window!!.setGravity(Gravity.BOTTOM)
        d.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val dismissBg = d.findViewById<RelativeLayout>(zeemart.asia.buyers.R.id.dismiss_bg)
        dismissBg.setOnClickListener { d.dismiss() }
        val txtOrderFrom = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_order_from)
        txtOrderFrom.text = String.format(
            context.resources.getString(zeemart.asia.buyers.R.string.txt_order_from_to),
            outletName
        )
        ZeemartBuyerApp.setTypefaceView(
            txtOrderFrom,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val txtOrderToTranslation =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_order_to_translation)
        ZeemartBuyerApp.setTypefaceView(
            txtOrderToTranslation,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val txtOrderTo = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_order_to)
        txtOrderTo.text = supplierName
        ZeemartBuyerApp.setTypefaceView(
            txtOrderTo,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD
        )
        val txtOrderShouldSent =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.lbl_order_notification_sent_to)
        ZeemartBuyerApp.setTypefaceView(
            txtOrderShouldSent,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD
        )
        val txtOrderDeliveryCutoff =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_deliveries_header)
        ZeemartBuyerApp.setTypefaceView(
            txtOrderDeliveryCutoff,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD
        )
        val lstDeliveryDates =
            d.findViewById<RecyclerView>(zeemart.asia.buyers.R.id.lst_delivery_date)
        lstDeliveryDates.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val adapter = DeliveryDatesForOrderSettingsAdapter(
            context,
            false,
            lstData.cutOffTimes,
            object : DeliveryDateSelectedListener {
                override fun deliveryDateSelected(date: String?, deliveryDate: Long?) {}
            }
        )
        lstDeliveryDates.adapter = adapter
        val lytEmail = d.findViewById<RelativeLayout>(zeemart.asia.buyers.R.id.lyt_receipt_email)
        val lytPhoneNumber =
            d.findViewById<RelativeLayout>(zeemart.asia.buyers.R.id.lyt_receipt_number)
        val lytWhatsApp = d.findViewById<RelativeLayout>(zeemart.asia.buyers.R.id.lyt_receipt_name)
        val txtEmailValue = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_email)
        ZeemartBuyerApp.setTypefaceView(
            txtEmailValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val btnSave = d.findViewById<Button>(zeemart.asia.buyers.R.id.login_btn)
        ZeemartBuyerApp.setTypefaceView(
            btnSave,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        if (!StringHelper.isStringNullOrEmpty(lstData.userNotifications?.email)) {
            txtEmailValue.text = lstData.userNotifications?.email
            ZeemartBuyerApp.setTypefaceView(
                txtEmailValue,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            lytEmail.visibility = View.VISIBLE
            btnSave.isClickable = true
        } else {
            txtEmailValue.text = "No email - tap to add"
            ZeemartBuyerApp.setTypefaceView(
                txtEmailValue,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_ITALICS
            )
            lytEmail.visibility = View.VISIBLE
            btnSave.isClickable = false
            btnSave.background =
                context.resources.getDrawable(zeemart.asia.buyers.R.drawable.btn_rounded_dark_grey)
        }
        val txtWhatsAppValue = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_whats_app)
        ZeemartBuyerApp.setTypefaceView(
            txtWhatsAppValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        //        if (!StringHelper.isStringNullOrEmpty(lstData.getUserNotifications().getWhatsapp())) {
//            txtWhatsAppValue.setText(lstData.getUserNotifications().getWhatsapp());
//            lytWhatsApp.setVisibility(View.VISIBLE);
//        } else {
//            txtWhatsAppValue.setText(" ");
//            lytWhatsApp.setVisibility(View.GONE);
//        }
        lytWhatsApp.visibility = View.GONE
        val txtPhoneNumberValue =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_phone_number)
        ZeemartBuyerApp.setTypefaceView(
            txtPhoneNumberValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        //        if (!StringHelper.isStringNullOrEmpty(lstData.getUserNotifications().getPhone())) {
//            txtPhoneNumberValue.setText(lstData.getUserNotifications().getPhone());
//            lytPhoneNumber.setVisibility(View.VISIBLE);
//        } else {
//            txtPhoneNumberValue.setText(" ");
//            lytPhoneNumber.setVisibility(View.GONE);
//        }
        lytPhoneNumber.visibility = View.GONE
        val lytEditEmailSection =
            d.findViewById<RelativeLayout>(zeemart.asia.buyers.R.id.lyt_email_edt_section)
        lytEditEmailSection.setOnClickListener {
            ShowEditMailSettings(context, lstData, object : onEmailEditDone {
                override fun onEmailEdtDone(
                    email: String?,
                    mobileNumber: String?,
                    whatsAppNumber: String?,
                ) {
                    lstData.userNotifications?.email = email
                    lstData.userNotifications?.phone = mobileNumber
                    lstData.userNotifications?.whatsapp = whatsAppNumber
                    if (!StringHelper.isStringNullOrEmpty(lstData.userNotifications?.email)) {
                        txtEmailValue.text = lstData.userNotifications?.email
                        lytEmail.visibility = View.VISIBLE
                        btnSave.isClickable = true
                        btnSave.background =
                            context.resources.getDrawable(zeemart.asia.buyers.R.drawable.btn_rounded_blue)
                    } else {
                        txtEmailValue.text = " "
                        lytEmail.visibility = View.GONE
                    }
                    if (!StringHelper.isStringNullOrEmpty(lstData.userNotifications?.whatsapp)) {
                        txtWhatsAppValue.text = lstData.userNotifications?.whatsapp
                        lytWhatsApp.visibility = View.VISIBLE
                        btnSave.isClickable = true
                        btnSave.background =
                            context.resources.getDrawable(zeemart.asia.buyers.R.drawable.btn_rounded_blue)
                    } else {
                        txtWhatsAppValue.text = " "
                        lytWhatsApp.visibility = View.GONE
                    }
                    if (!StringHelper.isStringNullOrEmpty(lstData.userNotifications?.phone)) {
                        txtPhoneNumberValue.text = lstData.userNotifications?.phone
                        lytPhoneNumber.visibility = View.VISIBLE
                        btnSave.isClickable = true
                        btnSave.background =
                            context.resources.getDrawable(zeemart.asia.buyers.R.drawable.btn_rounded_blue)
                    } else {
                        txtPhoneNumberValue.text = " "
                        lytPhoneNumber.visibility = View.GONE
                    }
                }
            })
        }
        val txtWarningHeader = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_wrong_details)
        val txtWarningSubHeader = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_tap_edit)
        ZeemartBuyerApp.setTypefaceView(
            txtWarningHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtWarningSubHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        btnSave.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setPositiveButton(zeemart.asia.buyers.R.string.txt_yes_save_settings) { dialog, which ->
                dialog.dismiss()
                d.dismiss()
                mListener.onButtonSaveClicked(lstData, lytWhatsApp.isShown, lytPhoneNumber.isShown)
            }
            builder.setNegativeButton(zeemart.asia.buyers.R.string.txt_cancel) { dialog, which -> dialog.dismiss() }
            val dialog = builder.create()
            dialog.setTitle(context.getString(zeemart.asia.buyers.R.string.txt_save_settings))
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
        }
        d.show()
    }

    @JvmStatic
    fun showOrderSettingsProductInfoDialog(
        context: Context,
        outletName: String?, supplierName: String?,
        lstData: OrderSettingDeliveryPreferences.Data,
        mListener: onButtonSaveClickListener?,
    ) {
        val d = Dialog(context, zeemart.asia.buyers.R.style.CustomDialogForTagsTheme)
        d.setContentView(zeemart.asia.buyers.R.layout.activity_order_settings_verification)
        d.window!!.setGravity(Gravity.BOTTOM)
        d.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val dismissBg = d.findViewById<RelativeLayout>(zeemart.asia.buyers.R.id.dismiss_bg)
        dismissBg.setOnClickListener { d.dismiss() }
        val txtOrderFrom = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_order_from)
        txtOrderFrom.text = String.format(
            context.resources.getString(zeemart.asia.buyers.R.string.txt_order_from_to),
            outletName
        )
        ZeemartBuyerApp.setTypefaceView(
            txtOrderFrom,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val txtOrderTo = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_order_to)
        txtOrderTo.text = supplierName
        ZeemartBuyerApp.setTypefaceView(
            txtOrderTo,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD
        )
        val txtOrderToTranslation =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_order_to_translation)
        ZeemartBuyerApp.setTypefaceView(
            txtOrderToTranslation,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val txtOrderShouldSent =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.lbl_order_notification_sent_to)
        ZeemartBuyerApp.setTypefaceView(
            txtOrderShouldSent,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD
        )
        val txtOrderDeliveryCutoff =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_deliveries_header)
        ZeemartBuyerApp.setTypefaceView(
            txtOrderDeliveryCutoff,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD
        )
        val lstDeliveryDates =
            d.findViewById<RecyclerView>(zeemart.asia.buyers.R.id.lst_delivery_date)
        lstDeliveryDates.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val adapter = DeliveryDatesForOrderSettingsAdapter(
            context,
            false,
            lstData.cutOffTimes,
            object : DeliveryDateSelectedListener {
                override fun deliveryDateSelected(date: String?, deliveryDate: Long?) {}
            }
        )
        lstDeliveryDates.adapter = adapter
        val lytEmail = d.findViewById<RelativeLayout>(zeemart.asia.buyers.R.id.lyt_receipt_email)
        val lytPhoneNumber =
            d.findViewById<RelativeLayout>(zeemart.asia.buyers.R.id.lyt_receipt_number)
        val lytWhatsApp = d.findViewById<RelativeLayout>(zeemart.asia.buyers.R.id.lyt_receipt_name)
        val txtEmailValue = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_email)
        ZeemartBuyerApp.setTypefaceView(
            txtEmailValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        if (!StringHelper.isStringNullOrEmpty(lstData.userNotifications?.email)) {
            txtEmailValue.text = lstData.userNotifications?.email
            lytEmail.visibility = View.VISIBLE
        } else {
            txtEmailValue.text = " "
            lytEmail.visibility = View.GONE
        }
        val txtWhatsAppValue = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_whats_app)
        ZeemartBuyerApp.setTypefaceView(
            txtWhatsAppValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        if (!StringHelper.isStringNullOrEmpty(lstData.userNotifications?.whatsapp)) {
            txtWhatsAppValue.text = lstData.userNotifications?.whatsapp
            lytWhatsApp.visibility = View.VISIBLE
        } else {
            txtWhatsAppValue.text = " "
            lytWhatsApp.visibility = View.GONE
        }
        val txtPhoneNumberValue =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_phone_number)
        ZeemartBuyerApp.setTypefaceView(
            txtPhoneNumberValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        if (!StringHelper.isStringNullOrEmpty(lstData.userNotifications?.phone)) {
            txtPhoneNumberValue.text = lstData.userNotifications?.phone
            lytPhoneNumber.visibility = View.VISIBLE
        } else {
            txtPhoneNumberValue.text = " "
            lytPhoneNumber.visibility = View.GONE
        }
        val txtWarningHeader = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_wrong_details)
        txtWarningHeader.text =
            context.resources.getString(zeemart.asia.buyers.R.string.txt_wrong_details_info)
        txtWarningHeader.setTextColor(context.resources.getColor(zeemart.asia.buyers.R.color.dark_grey))
        val txtWarningSubHeader = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_tap_edit)
        val imgInfo = d.findViewById<ImageView>(zeemart.asia.buyers.R.id.info_blue)
        txtWarningSubHeader.visibility = View.GONE
        imgInfo.visibility = View.GONE
        ZeemartBuyerApp.setTypefaceView(
            txtWarningHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtWarningSubHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val btnSave = d.findViewById<Button>(zeemart.asia.buyers.R.id.login_btn)
        btnSave.text = context.resources.getString(zeemart.asia.buyers.R.string.txt_done)
        btnSave.background =
            context.resources.getDrawable(zeemart.asia.buyers.R.drawable.btn_rounded_blue)
        ZeemartBuyerApp.setTypefaceView(
            btnSave,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        btnSave.setOnClickListener { d.dismiss() }
        d.show()
    }

    fun ShowEditMailSettings(
        context: Context, lstData: OrderSettingDeliveryPreferences.Data?,
        mLis: onEmailEditDone,
    ) {
        val d = Dialog(context, zeemart.asia.buyers.R.style.CustomDialogForTagsTheme)
        d.setContentView(zeemart.asia.buyers.R.layout.lyt_edit_email_settings)
        d.window!!.setGravity(Gravity.BOTTOM)
        d.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        val txtPhoneNumberHeader = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_sms_new)
        ZeemartBuyerApp.setTypefaceView(
            txtPhoneNumberHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        val txtHeader = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_order_from)
        ZeemartBuyerApp.setTypefaceView(
            txtHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD
        )
        //        TextView txtSubHeader = d.findViewById(R.id.txt_order_to);
//        ZeemartBuyerApp.setTypefaceView(txtSubHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR);
        val lytNumbers = d.findViewById<LinearLayout>(zeemart.asia.buyers.R.id.lyt_numbers)
        lytNumbers.visibility = View.GONE
        val txtWhatsAppNumberHeader =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_whats_app_new)
        ZeemartBuyerApp.setTypefaceView(
            txtWhatsAppNumberHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        val txtEmailHeader = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_email)
        ZeemartBuyerApp.setTypefaceView(
            txtEmailHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        val arrowIconImage = d.findViewById<ImageView>(zeemart.asia.buyers.R.id.arrow_icon)
        val txtEmailHeaderHint = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_email_hint)
        ZeemartBuyerApp.setTypefaceView(
            txtEmailHeaderHint,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val txtOtherNotification =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_other_notification)
        ZeemartBuyerApp.setTypefaceView(
            txtOtherNotification,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val txtOtherNotificationHint =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_other_notification_hint)
        ZeemartBuyerApp.setTypefaceView(
            txtOtherNotificationHint,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val edtPhoneNumber = d.findViewById<EditText>(zeemart.asia.buyers.R.id.edt_sms_new)
        ZeemartBuyerApp.setTypefaceView(
            edtPhoneNumber,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        edtPhoneNumber.visibility = View.GONE
        val edtWhatsAppNumber = d.findViewById<EditText>(zeemart.asia.buyers.R.id.edt_whatsapp_new)
        ZeemartBuyerApp.setTypefaceView(
            edtWhatsAppNumber,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        edtWhatsAppNumber.visibility = View.GONE
        val smsCheckBox = d.findViewById<CheckBox>(zeemart.asia.buyers.R.id.smsCheckBox)
        smsCheckBox.setOnClickListener {
            if (smsCheckBox.isChecked) {
                edtPhoneNumber.visibility = View.VISIBLE
            } else {
                edtPhoneNumber.visibility = View.GONE
            }
        }
        val whatsappCheckBox = d.findViewById<CheckBox>(zeemart.asia.buyers.R.id.whatsappCheckBox)
        whatsappCheckBox.setOnClickListener {
            if (whatsappCheckBox.isChecked) {
                edtWhatsAppNumber.visibility = View.VISIBLE
            } else {
                edtWhatsAppNumber.visibility = View.GONE
            }
        }
        txtOtherNotification.setOnClickListener {
            arrowIconImage.setImageBitmap(null)
            if (lytNumbers.isShown) {
                lytNumbers.visibility = View.GONE
                txtOtherNotificationHint.visibility = View.GONE
                arrowIconImage.setBackgroundResource(zeemart.asia.buyers.R.drawable.ic_arrow_down_blue)
            } else {
                lytNumbers.visibility = View.VISIBLE
                txtOtherNotificationHint.visibility = View.VISIBLE
                arrowIconImage.setBackgroundResource(zeemart.asia.buyers.R.drawable.ic_arrow_up_blue)
            }
        }
        val edtEmail = d.findViewById<EditText>(zeemart.asia.buyers.R.id.edt_email)
        ZeemartBuyerApp.setTypefaceView(
            edtEmail,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        if (lstData != null && lstData.userNotifications != null) {
            if (!StringHelper.isStringNullOrEmpty(lstData.userNotifications!!.whatsapp)) {
                edtWhatsAppNumber.setText(lstData.userNotifications!!.whatsapp)
                //                whatsappCheckBox.setChecked(true);
                edtWhatsAppNumber.visibility = View.VISIBLE
            }
            if (!StringHelper.isStringNullOrEmpty(lstData.userNotifications!!.email)) {
                edtEmail.setText(lstData.userNotifications!!.email)
            }
            if (!StringHelper.isStringNullOrEmpty(lstData.userNotifications!!.phone)) {
                edtPhoneNumber.setText(lstData.userNotifications!!.phone)
                //                smsCheckBox.setChecked(true);
                edtPhoneNumber.visibility = View.VISIBLE
            }
        }
        val dismissBg = d.findViewById<RelativeLayout>(zeemart.asia.buyers.R.id.dismiss_bg)
        dismissBg.setOnClickListener { d.dismiss() }
        val btnDone = d.findViewById<Button>(zeemart.asia.buyers.R.id.login_btn)
        ZeemartBuyerApp.setTypefaceView(
            btnDone,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        btnDone.setOnClickListener {
            if (isValid(edtEmail, edtPhoneNumber, edtWhatsAppNumber, context)) {
                if (!smsCheckBox.isChecked) {
                    edtPhoneNumber.setText("")
                }
                if (!whatsappCheckBox.isChecked) {
                    edtWhatsAppNumber.setText("")
                }
                mLis.onEmailEdtDone(
                    edtEmail.text.toString(),
                    edtPhoneNumber.text.toString(),
                    edtWhatsAppNumber.text.toString()
                )
                d.dismiss()
            }
        }
        d.show()
    }

    private fun isValid(
        edtEmail: EditText,
        edtPhoneNumber: EditText,
        edtWhatsAppNumber: EditText,
        context: Context,
    ): Boolean {
        var isValid = true
        if (!StringHelper.isStringNullOrEmpty(edtEmail.text.toString())) {
            val emailsList =
                edtEmail.text.toString().split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            for (i in emailsList.indices) {
                if (!Patterns.EMAIL_ADDRESS.matcher(emailsList[i]).matches()) {
                    edtEmail.error =
                        context.resources.getString(zeemart.asia.buyers.R.string.txt_invalid_mail)
                    isValid = false
                }
            }
        }
        if (StringHelper.isStringNullOrEmpty(edtEmail.text.toString()) && StringHelper.isStringNullOrEmpty(
                edtWhatsAppNumber.text.toString()
            ) && StringHelper.isStringNullOrEmpty(edtPhoneNumber.text.toString())
        ) {
            ZeemartBuyerApp.getToastRed("Please enter at least one method to notify supplier of your orders")
            isValid = false
        }
        if (!StringHelper.isStringNullOrEmpty(edtPhoneNumber.text.toString())) {
            val lstPhoneNumber =
                edtPhoneNumber.text.toString().split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            if (lstPhoneNumber.size > 1) {
                edtPhoneNumber.error =
                    context.resources.getString(zeemart.asia.buyers.R.string.txt_max_one_num_only)
                isValid = false
            }
            val reg = Regex("^[+][0-9]{10,14}$")
            for (i in lstPhoneNumber.indices) {
                if (!lstPhoneNumber[i].matches(reg)) {
                    edtPhoneNumber.error =
                        context.resources.getString(zeemart.asia.buyers.R.string.txt_incorrect_mobile_format)
                    isValid = false
                }
            }
        }
        if (!StringHelper.isStringNullOrEmpty(edtWhatsAppNumber.text.toString())) {
            val lstPhoneNumber = edtWhatsAppNumber.text.toString().split(",".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()
            if (lstPhoneNumber.size > 1) {
                edtWhatsAppNumber.error =
                    context.resources.getString(zeemart.asia.buyers.R.string.txt_max_one_num_only)
                isValid = false
            }
            val reg = Regex("^[+][0-9]{10,14}$")
            for (i in lstPhoneNumber.indices) {
                if (!lstPhoneNumber[i].matches(reg)) {
                    edtWhatsAppNumber.error =
                        context.resources.getString(zeemart.asia.buyers.R.string.txt_incorrect_mobile_format)
                    isValid = false
                }
            }
        }
        return isValid
    }

    @JvmStatic
    fun ShowEditDeliverySettings(
        context: Context,
        lstCutOfTimes: OrderSettingDeliveryPreferences.CutOffTimes?,
        mListener: onEditDeliverySettingsListener,
    ) {
        val d = Dialog(context, zeemart.asia.buyers.R.style.CustomDialogForTagsTheme)
        d.requestWindowFeature(Window.FEATURE_NO_TITLE)
        d.setContentView(zeemart.asia.buyers.R.layout.lyt_edt_delivery_settings)
        d.window!!.setGravity(Gravity.BOTTOM)
        d.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        val earlierCount = intArrayOf(0)
        val txtHeader = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_order_from)
        ZeemartBuyerApp.setTypefaceView(
            txtHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD
        )
        val txtSupplierDeliveryHeader =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_supplier_delivers_header)
        ZeemartBuyerApp.setTypefaceView(
            txtSupplierDeliveryHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val txtOrderMustPlacedHeader =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_order_must_placed_header)
        ZeemartBuyerApp.setTypefaceView(
            txtOrderMustPlacedHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val txtCutOfTimeHeader =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_cut_off_time_header)
        ZeemartBuyerApp.setTypefaceView(
            txtCutOfTimeHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val txtSupplierDeliveryValue =
            d.findViewById<SwitchCompat>(zeemart.asia.buyers.R.id.toggle_botton)
        val txtOrderMustPlacedValue =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_order_must_placed_value)
        ZeemartBuyerApp.setTypefaceView(
            txtOrderMustPlacedValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val txtCutOfTimeValue =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_cut_off_time_value)
        ZeemartBuyerApp.setTypefaceView(
            txtCutOfTimeValue,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        if (lstCutOfTimes != null && lstCutOfTimes.cutoffTime != null) {
            txtHeader.text = lstCutOfTimes.deliverOn
            if (lstCutOfTimes.status == "A") {
                txtSupplierDeliveryValue.isChecked = true
                var cutOffTimeText = ""
                cutOffTimeText = if (lstCutOfTimes.cutoffTime!!.days!! > 1) {
                    String.format(
                        context.getString(zeemart.asia.buyers.R.string.txt_days_earlier),
                        lstCutOfTimes.cutoffTime!!.days
                    )
                } else {
                    String.format(
                        context.getString(zeemart.asia.buyers.R.string.txt_day_earlier),
                        lstCutOfTimes.cutoffTime!!.days
                    )
                }
                earlierCount[0] = lstCutOfTimes.cutoffTime!!.days!!
                txtOrderMustPlacedValue.text = cutOffTimeText
            } else {
                var cutOffTimeText = ""
                if (lstCutOfTimes.cutoffTime!!.days!! > 1) {
                    cutOffTimeText = String.format(
                        context.getString(zeemart.asia.buyers.R.string.txt_days_earlier),
                        lstCutOfTimes.cutoffTime!!.days
                    )
                    earlierCount[0] = lstCutOfTimes.cutoffTime!!.days!!
                } else {
                    cutOffTimeText = String.format(
                        context.getString(zeemart.asia.buyers.R.string.txt_day_earlier),
                        1
                    )
                    earlierCount[0] = 1
                }
                txtOrderMustPlacedValue.text = cutOffTimeText
                txtSupplierDeliveryValue.isChecked = false
            }
            txtCutOfTimeValue.text = lstCutOfTimes.cutoffTime!!.time
        }
        txtOrderMustPlacedValue.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            val myLayout = LayoutInflater.from(context)
            val dialogView = myLayout.inflate(zeemart.asia.buyers.R.layout.lyt_edt_text, null)
            builder.setView(dialogView)
            val editText =
                dialogView.findViewById<View>(zeemart.asia.buyers.R.id.edtChangeQuantity) as EditText
            editText.setText(earlierCount[0].toString() + "")
            editText.setSelection(editText.text.length)
            editText.isFocusable = true
            builder.setTitle(context.getString(zeemart.asia.buyers.R.string.txt_enter_num_days))
            builder.setPositiveButton(context.resources.getString(zeemart.asia.buyers.R.string.dialog_ok_button_text)) { dialog, which ->
                val pickerValue = editText.text.toString().toInt()
                var cutOffTimeText = ""
                cutOffTimeText = if (pickerValue > 1) {
                    String.format(
                        context.getString(zeemart.asia.buyers.R.string.txt_days_earlier),
                        pickerValue
                    )
                } else {
                    String.format(
                        context.getString(zeemart.asia.buyers.R.string.txt_day_earlier),
                        pickerValue
                    )
                }
                earlierCount[0] = pickerValue
                txtOrderMustPlacedValue.text = cutOffTimeText
            }
            builder.setNegativeButton(context.resources.getString(zeemart.asia.buyers.R.string.dialog_cancel_button_text)) { dialog, which ->
                val imm =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(editText.windowToken, 0)
                dialog.cancel()
            }
            builder.setCancelable(false)
            builder.show()
            editText.requestFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }
        txtCutOfTimeValue.setOnClickListener { // TODO Auto-generated method stub
            val sdf = SimpleDateFormat("h:mm a")
            var date: Date? = null
            try {
                date = sdf.parse(txtCutOfTimeValue.text.toString())
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            val mcurrentTime = Calendar.getInstance()
            mcurrentTime.time = date
            val hour = mcurrentTime[Calendar.HOUR_OF_DAY]
            val minute = mcurrentTime[Calendar.MINUTE]
            val mTimePicker: TimePickerDialog
            mTimePicker = TimePickerDialog(context, { timePicker, selectedHour, selectedMinute ->
                val time = "$selectedHour:$selectedMinute"
                val fmt = SimpleDateFormat("HH:mm")
                var date: Date? = null
                try {
                    date = fmt.parse(time)
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
                val fmtOut = SimpleDateFormat("hh:mm aa")
                val formattedTime = fmtOut.format(date)
                txtCutOfTimeValue.text = formattedTime
            }, hour, minute, false) //Yes 24 hour time
            mTimePicker.setTitle("Select Time")
            mTimePicker.show()
        }
        val dismissBg = d.findViewById<RelativeLayout>(zeemart.asia.buyers.R.id.dismiss_bg)
        dismissBg.setOnClickListener { d.dismiss() }
        val btnDone = d.findViewById<Button>(zeemart.asia.buyers.R.id.login_btn)
        ZeemartBuyerApp.setTypefaceView(
            btnDone,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        btnDone.setOnClickListener {
            var status = ""
            var cutoffTime = ""
            var days = 0
            if (!txtSupplierDeliveryValue.isChecked) {
                status = "I"
                days = 1
                cutoffTime = "00:00 AM"
            } else {
                days = earlierCount[0]
                status = "A"
                cutoffTime = txtCutOfTimeValue.text.toString()
            }
            val cutoffTime1 = OrderSettingDeliveryPreferences.CutoffTime()
            cutoffTime1.days = days
            cutoffTime1.time = DateHelper.convert12hrTo24hrsFormat(cutoffTime)
            mListener.onEditedDeliverySettings(status, cutoffTime1)
            d.dismiss()
        }
        d.show()
    }

    @JvmStatic
    fun ShowCardListDialog(
        context: Context, lstCardJson: String?,
        companyId: String?, mListener: onCardSelectedListener,
    ) {
        val d = Dialog(context, zeemart.asia.buyers.R.style.CustomDialogForTagsTheme)
        d.setContentView(zeemart.asia.buyers.R.layout.dialog_payment_cards_list)
        d.window!!.setGravity(Gravity.BOTTOM)
        d.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setCompanyId(companyId!!)
        val cardsList: MutableList<PaymentCardDetails.PaymentResponse> = ArrayList()
        val txtSelectPaymentMethod =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_select_payment_method)
        ZeemartBuyerApp.setTypefaceView(
            txtSelectPaymentMethod,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        val txtAddCard = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_add_card)
        ZeemartBuyerApp.setTypefaceView(
            txtAddCard,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val recyclerView = d.findViewById<RecyclerView>(zeemart.asia.buyers.R.id.card_list)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val lytAddNewCard = d.findViewById<LinearLayout>(zeemart.asia.buyers.R.id.lyt_add_new_card)
        lytAddNewCard.setOnClickListener {
            ShowAddNewCardDialog(context, companyId, object : onCardAddedListener {
                override fun onCardSelected(selectedCardDetails: PaymentCardDetails.PaymentResponse) {
                    for (i in cardsList.indices) {
                        if (cardsList[i].isDefaultCard) {
                            cardsList[i].isDefaultCard = false
                        }
                    }
                    selectedCardDetails.isDefaultCard = true
                    cardsList.add(selectedCardDetails)
                    recyclerView.adapter = PaymentCardListAdapter(
                        context,
                        cardsList,
                        object : PayCardSelectedListener {
                            override fun onPayCardItemSelected(selectedCardDetails: PaymentCardDetails.PaymentResponse?) {
                                d.dismiss()
                                mListener.onCardSelected(selectedCardDetails)
                            }
                        })
                }
            })
        }
        PaymentApi.retrieveCompanyCardPaymentData(
            context,
            apiParamsHelper,
            SharedPref.defaultOutlet!!,
            object : CompanyCardDetailsListener {
                override fun onSuccessResponse(paymentCardDetails: PaymentCardDetails?) {
                    for (i in paymentCardDetails?.data?.indices!!) {
                        if (paymentCardDetails?.data!![i].status != null && paymentCardDetails.data!![i].status == PaymentStatus.ACTIVE.getmStatusName()) {
                            cardsList.add(paymentCardDetails?.data!![i])
                        }
                    }
                    recyclerView.adapter = PaymentCardListAdapter(
                        context,
                        cardsList,
                        object : PayCardSelectedListener {
                            override fun onPayCardItemSelected(selectedCardDetails: PaymentCardDetails.PaymentResponse?) {
                                d.dismiss()
                                for (i in cardsList.indices) {
                                    if (cardsList[i].isDefaultCard) {
                                        cardsList[i].isDefaultCard
                                    }
                                }
                                selectedCardDetails?.isDefaultCard!!
                                mListener.onCardSelected(selectedCardDetails)
                            }
                        })
                    d.show()
                }

                override fun onErrorResponse(error: VolleyErrorHelper?) {}
            })
        val dismissBg = d.findViewById<RelativeLayout>(zeemart.asia.buyers.R.id.dismiss_bg)
        dismissBg.setOnClickListener { d.dismiss() }
        d.setOnDismissListener {
            for (i in cardsList.indices) {
                if (cardsList[i].isDefaultCard) {
                    mListener.onCardSelected(cardsList[i])
                }
            }
        }
    }

    @JvmStatic
    fun ShowFilterSortByDialog(
        context: Context?,
        isSortFilterDeliveryDateApplies: Boolean,
        mListener: ProductListSelectedFilterItemClickListener,
    ) {
        val d = Dialog(context!!, zeemart.asia.buyers.R.style.CustomDialogForTagsTheme)
        d.setContentView(zeemart.asia.buyers.R.layout.dialog_sort_by_filter)
        d.window!!.setGravity(Gravity.BOTTOM)
        d.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val dismissBg = d.findViewById<RelativeLayout>(zeemart.asia.buyers.R.id.dismiss_bg)
        dismissBg.setOnClickListener { d.dismiss() }
        val imgDeliveryDate = d.findViewById<ImageView>(zeemart.asia.buyers.R.id.img_select_filter)
        val imgOrderDate =
            d.findViewById<ImageView>(zeemart.asia.buyers.R.id.img_select_order_date_filter)
        if (isSortFilterDeliveryDateApplies) {
            imgDeliveryDate.visibility = View.VISIBLE
            imgOrderDate.visibility = View.GONE
        } else {
            imgOrderDate.visibility = View.VISIBLE
            imgDeliveryDate.visibility = View.GONE
        }
        val txtHeader = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_add_on_order_heading)
        ZeemartBuyerApp.setTypefaceView(
            txtHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        val txtFilterDeliveryDate =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_filter_name)
        ZeemartBuyerApp.setTypefaceView(
            txtFilterDeliveryDate,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val txtOrderDate =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_filter_order_date_name)
        ZeemartBuyerApp.setTypefaceView(
            txtOrderDate,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val lytDeliveryDate =
            d.findViewById<RelativeLayout>(zeemart.asia.buyers.R.id.lyt_filter_delivery_date_row)
        lytDeliveryDate.setOnClickListener {
            imgDeliveryDate.visibility = View.VISIBLE
            imgOrderDate.visibility = View.GONE
            mListener.onFilterSelected(true)
            d.dismiss()
        }
        val lytOrderDate =
            d.findViewById<RelativeLayout>(zeemart.asia.buyers.R.id.lyt_filter_order_row)
        lytOrderDate.setOnClickListener {
            imgOrderDate.visibility = View.VISIBLE
            imgDeliveryDate.visibility = View.GONE
            mListener.onFilterSelected(false)
            d.dismiss()
        }
        d.show()
    }

    @JvmStatic
    fun ShowFilterRejectedOrUploadedDialog(
        context: Context?,
        isRejectedFilterApplies: Boolean,
        mListener: ProductListSelectedFilterItemClickListener,
    ) {
        val d = Dialog(context!!, zeemart.asia.buyers.R.style.CustomDialogForTagsTheme)
        d.setContentView(zeemart.asia.buyers.R.layout.dialog_rejected_uploads_filter)
        d.window!!.setGravity(Gravity.BOTTOM)
        d.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val dismissBg = d.findViewById<RelativeLayout>(zeemart.asia.buyers.R.id.dismiss_bg)
        dismissBg.setOnClickListener { d.dismiss() }
        val imgRejected = d.findViewById<ImageView>(zeemart.asia.buyers.R.id.img_select_filter)
        val imgAllOrders =
            d.findViewById<ImageView>(zeemart.asia.buyers.R.id.img_select_all_order_filter)
        if (isRejectedFilterApplies) {
            imgRejected.visibility = View.VISIBLE
            imgAllOrders.visibility = View.GONE
        } else {
            imgAllOrders.visibility = View.VISIBLE
            imgRejected.visibility = View.GONE
        }
        val txtHeader = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_add_on_order_heading)
        ZeemartBuyerApp.setTypefaceView(
            txtHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        val txtFilterRejectedBy = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_filter_name)
        ZeemartBuyerApp.setTypefaceView(
            txtFilterRejectedBy,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val txtAllOrders =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_filter_all_order_name)
        ZeemartBuyerApp.setTypefaceView(
            txtAllOrders,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val lytRejected =
            d.findViewById<RelativeLayout>(zeemart.asia.buyers.R.id.lyt_filter_reject_row)
        lytRejected.setOnClickListener {
            imgRejected.visibility = View.VISIBLE
            imgAllOrders.visibility = View.GONE
            mListener.onFilterSelected(true)
            d.dismiss()
        }
        val lytAllOrders =
            d.findViewById<RelativeLayout>(zeemart.asia.buyers.R.id.lyt_filter_all_order_row)
        lytAllOrders.setOnClickListener {
            imgAllOrders.visibility = View.VISIBLE
            imgRejected.visibility = View.GONE
            mListener.onFilterSelected(false)
            d.dismiss()
        }
        d.show()
    }

    fun markAsUplpadnotRequireddialogalert(
        context: Context?,
        mOrders: Orders,
        mlistner: InvoiceOrderDeleted,
    ) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setMessage(zeemart.asia.buyers.R.string.txt_upload_not_required_skipped)
        alertDialogBuilder.setPositiveButton(
            zeemart.asia.buyers.R.string.txt_remove_status
        ) { arg0, arg1 ->
            uploadRequired(
                context,
                SharedPref.allOutlets,
                mOrders.orderId,
                true,
                mlistner
            )
        }
        alertDialogBuilder.setNegativeButton(zeemart.asia.buyers.R.string.txt_cancel) { dialog, which -> dialog.dismiss() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
    }

    fun markAsUplpadnotRequireddialog(
        context: Context?,
        mOrders: Orders,
        mlistner: InvoiceOrderDeleted,
    ) {
        val d = Dialog(context!!, zeemart.asia.buyers.R.style.CustomDialogForTagsTheme1)
        d.setContentView(zeemart.asia.buyers.R.layout.upload_not_required_layout)
        d.window!!.setGravity(Gravity.BOTTOM)
        d.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val txt_remove_status = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_remove_status)
        val btnremove_cancel = d.findViewById<Button>(zeemart.asia.buyers.R.id.btn_cancel)
        d.show()
        txt_remove_status.setOnClickListener {
            uploadRequiredremove(
                context,
                SharedPref.allOutlets,
                mOrders.orderId,
                true,
                mlistner
            )
            d.dismiss()
        }
        btnremove_cancel.setOnClickListener { d.dismiss() }
    }

    /*  public static void markAsUplpadRequireddialog(final Context context,Orders mOrders,InvoiceOrderDeleted mlistner) {

          final Dialog d = new Dialog(context, R.style.CustomDialogForTagsTheme1);
          d.setContentView(R.layout.skip_upload_layout);
          d.getWindow().setGravity(Gravity.CENTER);
        //  d.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

          TextView txt_mark_as = d.findViewById(R.id.txt_mark_as);
          TextView btn_cancel  = d.findViewById(R.id.btn_cancel);

          d.show();

          txt_mark_as.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {

                  uploadRequired(context,SharedPref.getAllOutlets(), mOrders.getOrderId(),false,mlistner);
                  d.dismiss();

              }
          });

          btn_cancel.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  d.dismiss();
              }
          });


      }
  */
    fun markAsUplpadRequireddialogalert(
        context: Context?,
        mOrders: Orders,
        mlistner: InvoiceOrderDeleted,
    ) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setMessage(zeemart.asia.buyers.R.string.txt_not_required)
        alertDialogBuilder.setPositiveButton(
            zeemart.asia.buyers.R.string.txt_mark_as_not_required
        ) { arg0, arg1 ->
            uploadRequired(
                context,
                SharedPref.allOutlets,
                mOrders.orderId,
                false,
                mlistner
            )
        }
        alertDialogBuilder.setNegativeButton(zeemart.asia.buyers.R.string.txt_cancel) { dialog, which -> dialog.dismiss() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    fun uploadRequired(
        context: Context?, outlets: List<Outlet>?, orderId: String?, uploadRequired: Boolean,
        mlistner: InvoiceOrderDeleted,
    ) {
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOrderId(orderId!!)
        apiParamsHelper.setUploadRequired(uploadRequired)
        var uri = ServiceConstant.ENDPOINT_UPLOAD_REQUIRED
        uri = apiParamsHelper.getUrl(uri)
        Log.d("uri_print", uri)
        val repeatOrderRequest = ZeemartAPIRequest(context, Request.Method.PUT, uri,
            null, CommonMethods.getHeaderFromOutlets(outlets) as Map<String, String>, { response ->
                if (!StringHelper.isStringNullOrEmpty(response)) {
                    mlistner.OnSuccess()
                }
            }) { }
        VolleyRequest.getInstance(context)?.addToRequestQueue(repeatOrderRequest)
    }

    fun uploadRequiredremove(
        context: Context?, outlets: List<Outlet>?, orderId: String?, uploadRequired: Boolean,
        mlistner: InvoiceOrderDeleted,
    ) {
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setOrderId(orderId!!)
        apiParamsHelper.setUploadRequired(uploadRequired)
        var uri = ServiceConstant.ENDPOINT_UPLOAD_REQUIRED
        uri = apiParamsHelper.getUrl(uri)
        Log.d("uri_print", uri)
        val repeatOrderRequest = ZeemartAPIRequest(context, Request.Method.PUT, uri,
            null, CommonMethods.getHeaderFromOutlets(outlets) as Map<String, String>, { response ->
                if (!StringHelper.isStringNullOrEmpty(response)) {
                    mlistner.OnSuccess()
                }
            }) { }
        VolleyRequest.getInstance(context)?.addToRequestQueue(repeatOrderRequest)
    }

    @JvmStatic
    fun ShowInvoiceLinkedToOrderDetailsDialog(
        context: Context, mOrders: Orders, invoices: MutableList<Invoice?>?,
        mListener: InvoiceOrderDeleted,
    ) {
        val isDeleted = booleanArrayOf(false)
        val d = Dialog(context, zeemart.asia.buyers.R.style.CustomDialogForTagsTheme)
        d.setContentView(zeemart.asia.buyers.R.layout.dialog_inoice_linked_to_orders)
        d.window!!.setGravity(Gravity.BOTTOM)
        d.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val dismissBg = d.findViewById<ConstraintLayout>(zeemart.asia.buyers.R.id.dismiss_bg)
        dismissBg.setOnClickListener { d.dismiss() }
        val txtHeader = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_add_on_order_heading)
        val add_icon = d.findViewById<ImageView>(zeemart.asia.buyers.R.id.add_plus)
        val txt_skip_upload = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_skip_upload)
        val add_on = d.findViewById<TextView>(zeemart.asia.buyers.R.id.add_on)
        // ImageView tick_green = d.findViewById(R.id.tick_green);
        val txt_upload_not_required =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_upload_not_required)
        ZeemartBuyerApp.setTypefaceView(
            txtHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        val btnViewDetails = d.findViewById<Button>(zeemart.asia.buyers.R.id.btn_merge_invoice)
        txt_upload_not_required.setOnClickListener {
            markAsUplpadnotRequireddialogalert(context, mOrders, mListener)
            d.dismiss()
        }
        txt_skip_upload.setOnClickListener {
            markAsUplpadRequireddialogalert(context, mOrders, mListener)
            d.dismiss()
        }
        btnViewDetails.setOnClickListener {
            AnalyticsHelper.logAction(
                context,
                AnalyticsHelper.TAP_VIEW_ORDER_DETAILS_INVOICE_ORDER_ITEM
            )
            val newIntent = Intent(context, OrderDetailsActivity::class.java)
            newIntent.putExtra(ZeemartAppConstants.ORDER_ID, mOrders.orderId)
            newIntent.putExtra(
                ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID,
                mOrders.outlet?.outletId
            )
            context.startActivity(newIntent)
        }
        val btnUpload = d.findViewById<LinearLayout>(zeemart.asia.buyers.R.id.lyt_upload)
        btnUpload.setOnClickListener {
            AnalyticsHelper.logAction(context, AnalyticsHelper.TAP_UPLOAD_BYORDER_DETAIL_UPLOAD)
            //set the flash boolean true for edge detection
            SharedPref.writeBool(SharedPref.FLASH_INVOICE_UPLOAD, true)
            SharedPref.writeBool(SharedPref.AUTO_CROP_INVOICE_UPLOAD, true)
            val newIntent = Intent(context, InvoiceCameraPreview::class.java)
            newIntent.putExtra(
                ZeemartAppConstants.CALLED_FROM,
                ZeemartAppConstants.CALLED_FROM_ORDER_DETAILS
            )
            newIntent.putExtra(ZeemartAppConstants.ORDER_ID, mOrders.orderId)
            context.startActivity(newIntent)
            d.dismiss()
        }
        val txtUpload = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_upload)
        val txtLinkedToInvoices =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_linked_to_orders)
        if (mOrders.isAddOn) {
            add_icon.visibility = View.VISIBLE
            add_on.visibility = View.VISIBLE
            // txt_skip_upload.setVisibility(View.VISIBLE);
        } else {
            add_icon.visibility = View.GONE
            add_on.visibility = View.GONE
            //  txt_skip_upload.setVisibility(View.GONE);
        }
        if (mOrders.isUploadRequired) {
            // tick_green.setVisibility(View.GONE);
            txt_upload_not_required.visibility = View.GONE
            txt_skip_upload.visibility = View.VISIBLE
            if (mOrders.isLinkedToInvoice) {
                txt_skip_upload.visibility = View.GONE
            } else {
                txt_skip_upload.visibility = View.VISIBLE
            }
        } else {
            // tick_green.setVisibility(View.VISIBLE);
            txt_upload_not_required.visibility = View.VISIBLE
            txt_skip_upload.visibility = View.GONE
        }
        var count = 0
        if (mOrders.linkedInvoices != null && mOrders.linkedInvoices!!.size > 0) {
            for (i in mOrders.linkedInvoices!!.indices) {
                if (!StringHelper.isStringNullOrEmpty(mOrders.linkedInvoices!![i].invoiceNum)) {
                    count = count + 1
                }
            }
            if (count > 0) {
                txtLinkedToInvoices.visibility = View.VISIBLE
                txt_skip_upload.visibility = View.GONE
                txt_upload_not_required.visibility = View.GONE
                if (count > 1) {
                    txtLinkedToInvoices.text = String.format(
                        context.getString(zeemart.asia.buyers.R.string.txt_linked_invoices),
                        mOrders.linkedInvoices!!.size
                    )
                } else {
                    txtLinkedToInvoices.text = String.format(
                        context.getString(zeemart.asia.buyers.R.string.txt_linked_invoice),
                        mOrders.linkedInvoices!!.size
                    )
                }
            } else {
                txtLinkedToInvoices.visibility = View.GONE
                txt_skip_upload.visibility = View.GONE
            }
        } else {
            txtLinkedToInvoices.visibility = View.GONE
            // txt_skip_upload.setVisibility(View.VISIBLE);
        }
        txtLinkedToInvoices.setOnClickListener {
            AnalyticsHelper.logAction(
                context,
                AnalyticsHelper.TAP_VIEW_ORDER_DETAILS_INVOICE_ORDER_ITEM
            )
            val newIntent = Intent(context, OrderDetailsActivity::class.java)
            newIntent.putExtra(ZeemartAppConstants.ORDER_ID, mOrders.orderId)
            newIntent.putExtra(
                ZeemartAppConstants.SELECTED_ORDER_OUTLET_ID,
                mOrders.outlet?.outletId
            )
            context.startActivity(newIntent)
        }
        ZeemartBuyerApp.setTypefaceView(
            txtUpload,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            btnViewDetails,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtLinkedToInvoices,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txt_skip_upload,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txt_upload_not_required,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            add_on,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val txtDeliveryDate =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_order_delivery_date)
        ZeemartBuyerApp.setTypefaceView(
            txtDeliveryDate,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        var deliveryDate = ""
        if (mOrders.amount != null && mOrders.amount!!.total != null) {
            deliveryDate = mOrders.amount!!.total!!.displayValue
        }
        if (mOrders.timeDelivered != null) {
            deliveryDate =
                deliveryDate + " - " + context.getString(zeemart.asia.buyers.R.string.txt_delivery_date) + ": " + DateHelper.getDateInDateMonthFormat(
                    mOrders.timeDelivered
                )
        }
        if (count > 0) {
            deliveryDate = "$deliveryDate - "
        }
        txtDeliveryDate.text = deliveryDate
        val txtOrderId = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_order_id)
        var orderId = ""
        if (mOrders.orderId != null) {
            orderId =
                context.resources.getString(zeemart.asia.buyers.R.string.txt_order_with_hash_tag) + mOrders.orderId
        } else if (invoices != null && invoices.size > 0) {
            orderId =
                orderId + " (" + invoices.size + context.resources.getString(zeemart.asia.buyers.R.string.txt_uploads) + ")"
        }
        txtOrderId.text = orderId
        ZeemartBuyerApp.setTypefaceView(
            txtOrderId,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        val lstInvoices =
            d.findViewById<RecyclerView>(zeemart.asia.buyers.R.id.lst_filter_suppliers)
        lstInvoices.layoutManager = LinearLayoutManager(context)
        val mAdapterProcessed =
            InvoicesLinkedToOrderListingAdapter(context, invoices!!, object : EditInvoiceListener {
                override fun retryUploadOfFailedInvoices(
                    failedInvoice: List<InQueueForUploadDataModel?>?,
                    retryInvoicePosition: Int,
                ) {
                }

                override fun getSelectedInvoicesDeleteOrMerge(selectedInvoices: List<InvoiceUploadsListDataMgr?>?) {}
                override fun invoicesCountByStatus() {
                    isDeleted[0] = true
                }

                override fun onEditButtonClicked() {}
                override fun onListItemLongPressed() {}
            })
        if (invoices != null && invoices.size > 0) {
            lstInvoices.visibility = View.VISIBLE
            lstInvoices.adapter = mAdapterProcessed
        } else {
            lstInvoices.visibility = View.GONE
        }
        val uploadedInvoiceSwipeHelper: ItemTouchHelper.SimpleCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                private val background = ColorDrawable()
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder,
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    if (lstInvoices.adapter != null && lstInvoices.adapter is InvoicesLinkedToOrderListingAdapter) {
                        val mAdapter = lstInvoices.adapter as InvoicesLinkedToOrderListingAdapter?
                        if (mAdapter != null) {
                            val deleteItemPosition = viewHolder.adapterPosition
                            if (invoices != null && invoices[deleteItemPosition] != null) {
                                val builder = AlertDialog.Builder(context)
                                builder.setPositiveButton(context.getString(zeemart.asia.buyers.R.string.txt_yes_delete)) { dialog, which -> //remove the row from the adapter and notify the adapter call the API also to remove from the backend
                                    val json = InvoiceHelper.createDeleteJsonForInvoice(
                                        invoices[deleteItemPosition]!!.invoiceId
                                    )
                                    val outlet: MutableList<Outlet> = ArrayList()
                                    outlet.add(SharedPref.defaultOutlet!!)
                                    InvoiceHelper.deleteUnprocessedInvoice(
                                        context,
                                        outlet,
                                        object : GetRequestStatusResponseListener {
                                            override fun onSuccessResponse(status: String?) {
                                                isDeleted[0] = true
                                                //Remove from the list and update the adapter
                                                //invoices.remove(invoices[deleteItemPosition])

                                                invoices.removeAt(deleteItemPosition)
                                                mAdapter.notifyDataChanged(
                                                    invoices,
                                                    true,
                                                    false,
                                                    false,
                                                    deleteItemPosition
                                                )
                                            }

                                            override fun onErrorResponse(error: VolleyErrorHelper?) {
                                                ZeemartBuyerApp.getToastRed(
                                                    context.getString(
                                                        zeemart.asia.buyers.R.string.txt_delete_draft_fail
                                                    )
                                                )
                                            }
                                        },
                                        json
                                    )
                                    dialog.dismiss()
                                }
                                builder.setNegativeButton(context.getString(zeemart.asia.buyers.R.string.txt_cancel)) { dialog, which -> dialog.dismiss() }
                                val dialog = builder.create()
                                dialog.setOnDismissListener {
                                    mAdapter.notifyDataChanged(
                                        invoices,
                                        false,
                                        false,
                                        true,
                                        deleteItemPosition
                                    )
                                }
                                dialog.setCancelable(true)
                                dialog.setCanceledOnTouchOutside(true)
                                dialog.setTitle(context.getString(zeemart.asia.buyers.R.string.txt_delete_invoice))
                                dialog.setMessage(context.getString(zeemart.asia.buyers.R.string.txt_want_to_delete_the_invoice))
                                dialog.show()
                                val deleteBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                                deleteBtn.setTextColor(context.resources.getColor(zeemart.asia.buyers.R.color.chart_red))
                                deleteBtn.isAllCaps = false
                                val cancelBtn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                                cancelBtn.isAllCaps = false
                            }
                        }
                    }
                }

                override fun getSwipeDirs(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                ): Int {
                    return if (lstInvoices.adapter != null && lstInvoices.adapter is InvoicesLinkedToOrderListingAdapter) {
                        val mAdapter = lstInvoices.adapter as InvoicesLinkedToOrderListingAdapter?
                        if (mAdapter != null) {
                            val currentItemPosition = viewHolder.adapterPosition
                            if (invoices != null && invoices[currentItemPosition] != null) {
                                if (invoices[currentItemPosition]!!
                                        .isStatus(Invoice.Status.PENDING) || invoices[currentItemPosition]!!
                                        .isStatus(Invoice.Status.REJECTED)
                                ) {
                                    ItemTouchHelper.LEFT
                                } else {
                                    0
                                }
                            } else {
                                0
                            }
                        } else {
                            0
                        }
                    } else {
                        0
                    }
                }

                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean,
                ) {
                    if (lstInvoices.adapter != null && lstInvoices.adapter is InvoicesLinkedToOrderListingAdapter) {
                        val text = context.getString(zeemart.asia.buyers.R.string.txt_delete)
                        //InvoiceUploadsAdapter.ViewHolderItem mViewHolder = (InvoiceUploadsAdapter.ViewHolderItem) viewHolder;
                        val color =
                            context.resources.getColor(zeemart.asia.buyers.R.color.pinky_red)
                        background.color = color
                        background.setBounds(
                            viewHolder.itemView.right + dX.toInt(),
                            viewHolder.itemView.top,
                            viewHolder.itemView.right,
                            viewHolder.itemView.bottom
                        )
                        background.draw(c)
                        val p = Paint()
                        val buttonWidth = viewHolder.itemView.right - CommonMethods.dpToPx(70)
                        val rightButton = RectF(
                            buttonWidth.toFloat(),
                            viewHolder.itemView.top.toFloat(),
                            viewHolder.itemView.right.toFloat(),
                            viewHolder.itemView.bottom.toFloat()
                        )
                        p.color = color
                        c.drawRect(rightButton, p)
                        p.color = Color.WHITE
                        p.isAntiAlias = true
                        p.textSize = CommonMethods.dpToPx(14).toFloat()
                        val textWidth = p.measureText(text)
                        val bmp = BitmapFactory.decodeResource(
                            context.resources,
                            zeemart.asia.buyers.R.drawable.deletedraft
                        )
                        val bounds = Rect()
                        p.getTextBounds(text, 0, text.length, bounds)
                        val combinedHeight =
                            (bmp.height + CommonMethods.dpToPx(11) + bounds.height()).toFloat()
                        c.drawBitmap(
                            bmp,
                            rightButton.centerX() - bmp.width / 2,
                            rightButton.centerY() - combinedHeight / 2,
                            null
                        )
                        c.drawText(
                            text,
                            rightButton.centerX() - textWidth / 2,
                            rightButton.centerY() + combinedHeight / 2,
                            p
                        )
                        super.onChildDraw(
                            c,
                            recyclerView,
                            viewHolder,
                            dX,
                            dY,
                            actionState,
                            isCurrentlyActive
                        )
                    }
                }
            }
        val helperUploadInvoice = ItemTouchHelper(uploadedInvoiceSwipeHelper)
        helperUploadInvoice.attachToRecyclerView(lstInvoices)
        d.setOnDismissListener { if (isDeleted[0]) mListener.onDeleted() }
        d.show()
    }

    @JvmStatic
    fun ShowFilterSupplierDialog(
        context: Context?,
        lstSupplier: List<DetailSupplierDataModel>,
        mListener: InvoicesSupplierFilterItemClickListener,
    ) {
        val d = Dialog(context!!, zeemart.asia.buyers.R.style.CustomDialogForTagsTheme)
        d.setContentView(zeemart.asia.buyers.R.layout.dialog_suppliers_filter)
        d.window!!.setGravity(Gravity.BOTTOM)
        d.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val dismissBg = d.findViewById<ConstraintLayout>(zeemart.asia.buyers.R.id.dismiss_bg)
        d.setCanceledOnTouchOutside(false)
        dismissBg.setOnClickListener { d.dismiss() }
        val txtHeader = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_add_on_order_heading)
        ZeemartBuyerApp.setTypefaceView(
            txtHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        val lstSuppliers =
            d.findViewById<RecyclerView>(zeemart.asia.buyers.R.id.lst_filter_suppliers)
        lstSuppliers.layoutManager = LinearLayoutManager(context)
        lstSuppliers.adapter = FilterInvoiceByOrderSupplier(object :
            FilterInvoiceByOrderSupplier.SelectedInvoiceFiltersListener {
            override fun onFilterSelected() {}
            override fun onFilterDeselected() {}
        }, context, lstSupplier)
        val btnSave = d.findViewById<Button>(zeemart.asia.buyers.R.id.btn_merge_invoice)
        val btnRemoveFilter = d.findViewById<Button>(zeemart.asia.buyers.R.id.btn_delete_invoice)
        ZeemartBuyerApp.setTypefaceView(
            btnSave,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            btnRemoveFilter,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        btnSave.setOnClickListener {
            val selectedSuppliers = ArrayList<DetailSupplierDataModel>()
            for (i in lstSupplier.indices) {
                if (lstSupplier[i].isSupplierSelected) {
                    selectedSuppliers.add(lstSupplier[i])
                }
            }
            mListener.onFilterSelected(selectedSuppliers)
            d.dismiss()
        }
        btnRemoveFilter.setOnClickListener {
            FilterInvoiceByOrderSupplier.resetFilterInvoiceList()
            lstSuppliers.adapter = FilterInvoiceByOrderSupplier(object :
                FilterInvoiceByOrderSupplier.SelectedInvoiceFiltersListener {
                override fun onFilterSelected() {}
                override fun onFilterDeselected() {}
            }, context, lstSupplier)
            mListener.onFilterSelected(ArrayList())
            d.dismiss()
        }
        d.show()
    }

    @JvmStatic
    fun ShowAddOnOrderDescription(context: Context?) {
        val d = Dialog(context!!, zeemart.asia.buyers.R.style.CustomDialogForTagsTheme)
        d.setContentView(zeemart.asia.buyers.R.layout.dialog_add_on_order_description)
        d.window!!.setGravity(Gravity.BOTTOM)
        d.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val dismissBg = d.findViewById<RelativeLayout>(zeemart.asia.buyers.R.id.dismiss_bg)
        dismissBg.setOnClickListener { d.dismiss() }
        val txtAddOnOrderHeader =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_add_on_order_heading)
        ZeemartBuyerApp.setTypefaceView(
            txtAddOnOrderHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        val txtAddOnOrderContent =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_add_on_order_content)
        ZeemartBuyerApp.setTypefaceView(
            txtAddOnOrderContent,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        d.show()
    }

    @JvmStatic
    fun ShowFilterTagsDialog(
        context: Context,
        isCalledFromEssentialOrDeals: Boolean,
        isCalledFromSupplierlist: Boolean,
        selectedFilterCatageryCounter: Int,
        clearselectedcategory: Int,
        selectedFilterTagCounter: Int,
        selectedFilterCertificationCounter: Int,
        selectedFiltersupplierCounter: Int,
        outletTagsList: List<OutletTags>?,
        outletCategoriesList: List<OutletTags>,
        outletCertificationList: List<OutletTags>,
        outletSupplierList: List<OutletTags>,
        mTagsListener: ProductsFilterListingAdapter.ProductListSelectedFilterItemClickListner,
        mCategoriesListener: ProductsFilterListingAdapter.ProductListSelectedFilterCategoriesItemClickListener,
        mCertificationListener: ProductsFilterListingAdapter.ProductListSelectedFilterCertificationItemClickListener,
        mSupplierListner: ProductListSelectedFilterSupplierItemClickListener,
        mlistneremptydata: ProductListSelectedFilterCategoriesItemClickListeneremptydata?,
    ) {
        val d = Dialog(context, zeemart.asia.buyers.R.style.CustomDialogForTagsTheme)
        val iscliked = booleanArrayOf(false)
        d.setContentView(zeemart.asia.buyers.R.layout.dialog_filter_by_tags_sku)
        d.window!!.setGravity(Gravity.BOTTOM)
        d.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val dismissBg = d.findViewById<RelativeLayout>(zeemart.asia.buyers.R.id.dismiss_bg)
        val txtTags = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_tags)
        val txtCategories = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_category)
        val txtCertifications = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_certification)
        val txtTagsHighLight =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_btn_tags_highlighter)
        val txtCategoriesHighLight =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_btn_category_highlighter)
        val txtCertificationsHighLight =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_btn_certification_highlighter)
        val txtTagsCount = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_number_of_tags)
        val txt_supplier = d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_supplier)
        val txtCategoriesCount =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_number_of_categories)
        val txtCertificationsCount =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_number_of_certifications)
        val txt_number_of_supplier =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_number_of_supplier)
        val lstFilterTags = d.findViewById<RecyclerView>(zeemart.asia.buyers.R.id.lst_filter_tags)
        val lstFilterCategories =
            d.findViewById<RecyclerView>(zeemart.asia.buyers.R.id.lst_filter_categories)
        val lst_filter_suppliers =
            d.findViewById<RecyclerView>(zeemart.asia.buyers.R.id.lst_filter_suppliers)
        val lstFilterCertificates =
            d.findViewById<RecyclerView>(zeemart.asia.buyers.R.id.lst_filter_certifications)
        val lytNoResultTags =
            d.findViewById<RelativeLayout>(zeemart.asia.buyers.R.id.lyt_tags_no_result)
        val btn_reset = d.findViewById<Button>(zeemart.asia.buyers.R.id.btn_reset)
        val btn_save = d.findViewById<Button>(zeemart.asia.buyers.R.id.btn_save)
        val filtermarket_cardview =
            d.findViewById<CardView>(zeemart.asia.buyers.R.id.filtermarket_cardview)
        lytNoResultTags.visibility = View.GONE
        val lytNoResultCategories =
            d.findViewById<RelativeLayout>(zeemart.asia.buyers.R.id.lyt_categories_certification_no_result)
        lytNoResultTags.visibility = View.GONE
        val txtNoResultTagsHeader =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_no_result_tags_header)
        val txtNoResultTagsContent =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_no_result_tags_content)
        val txtNoResultCategories =
            d.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_no_result_categories)
        ZeemartBuyerApp.setTypefaceView(
            txtTags,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            btn_reset,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            btn_save,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtCategories,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtCertifications,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txt_supplier,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtTagsCount,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtCategoriesCount,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtCertificationsCount,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txt_number_of_supplier,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNoResultTagsContent,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNoResultTagsHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNoResultCategories,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        val lytTags = d.findViewById<RelativeLayout>(zeemart.asia.buyers.R.id.lyt_tags)
        val lyt_supplier = d.findViewById<RelativeLayout>(zeemart.asia.buyers.R.id.lyt_supplier)
        val lytCategories = d.findViewById<RelativeLayout>(zeemart.asia.buyers.R.id.lyt_category)
        val lytCertifications =
            d.findViewById<RelativeLayout>(zeemart.asia.buyers.R.id.lyt_certification)
        val filtermarket_cardview2 =
            d.findViewById<CardView>(zeemart.asia.buyers.R.id.filtermarket_cardview2)
        val filtermarket_cardview3 =
            d.findViewById<CardView>(zeemart.asia.buyers.R.id.filtermarket_cardview3)
        val filtermarket_cardview1 =
            d.findViewById<CardView>(zeemart.asia.buyers.R.id.filtermarket_cardview1)
        if (isCalledFromSupplierlist) {
            lyt_supplier.visibility = View.VISIBLE
            filtermarket_cardview1.visibility = View.VISIBLE
        } else {
            lyt_supplier.visibility = View.GONE
            filtermarket_cardview1.visibility = View.GONE
        }
        if (isCalledFromEssentialOrDeals) {
            lytTags.visibility = View.GONE
            filtermarket_cardview2.visibility = View.GONE
            filtermarket_cardview1.visibility = View.GONE
            txtTagsCount.visibility = View.GONE
        } else {
            lytTags.visibility = View.VISIBLE
            filtermarket_cardview2.visibility = View.VISIBLE
            if (selectedFilterTagCounter != 0) {
                txtTagsCount.visibility = View.VISIBLE
                txtTagsCount.text = selectedFilterTagCounter.toString() + ""
            } else {
                txtTagsCount.visibility = View.GONE
            }
        }
        if (clearselectedcategory != 0) {
            txtCategoriesCount.visibility = View.GONE
            txtCategoriesCount.text = selectedFilterCatageryCounter.toString() + ""
        } else {
            txtCategoriesCount.visibility = View.VISIBLE
        }
        if (selectedFilterCatageryCounter != 0) {
            txtCategoriesCount.visibility = View.VISIBLE
            txtCategoriesCount.text = selectedFilterCatageryCounter.toString() + ""
        } else {
            txtCategoriesCount.visibility = View.GONE
        }
        if (selectedFilterCertificationCounter != 0) {
            txtCertificationsCount.visibility = View.VISIBLE
            txtCertificationsCount.text = selectedFilterCertificationCounter.toString() + ""
        } else {
            txtCertificationsCount.visibility = View.GONE
        }
        if (selectedFiltersupplierCounter != 0) {
            txt_number_of_supplier.visibility = View.VISIBLE
            txt_number_of_supplier.text = selectedFiltersupplierCounter.toString() + ""
        } else {
            txt_number_of_supplier.visibility = View.GONE
        }
        txtCategories.setTextColor(context.resources.getColor(zeemart.asia.buyers.R.color.text_blue))
        // txtCategoriesHighLight.setVisibility(View.VISIBLE);
        txtTags.setTextColor(context.resources.getColor(zeemart.asia.buyers.R.color.black))
        //  txtTagsHighLight.setVisibility(View.GONE);
        txtCertifications.setTextColor(context.resources.getColor(zeemart.asia.buyers.R.color.black))
        // txtCertificationsHighLight.setVisibility(View.GONE);
        lstFilterTags.visibility = View.GONE
        lstFilterCertificates.visibility = View.GONE
        lst_filter_suppliers.visibility = View.GONE
        if (outletCategoriesList != null && outletCategoriesList.size > 0) {
            lstFilterCategories.visibility = View.VISIBLE
            lytNoResultCategories.visibility = View.GONE
            lytNoResultTags.visibility = View.GONE
        } else {
            lstFilterCategories.visibility = View.GONE
            lytNoResultCategories.visibility = View.VISIBLE
            lytNoResultTags.visibility = View.GONE
        }
        btn_reset.setOnClickListener {
            mCategoriesListener.onResetPressed()
            d.dismiss()
        }
        btn_save.setOnClickListener {
            iscliked[0] = true
            mCategoriesListener.onSavePressed()
            d.dismiss()
        }
        lytTags.setOnClickListener {
            if (lytTags.isClickable) {
                lytTags.background = ContextCompat.getDrawable(
                    context,
                    zeemart.asia.buyers.R.drawable.light_blue_rounded_corner
                )
                filtermarket_cardview2.background = ContextCompat.getDrawable(
                    context,
                    zeemart.asia.buyers.R.drawable.light_blue_rounded_corner
                )
                lytCategories.background = ContextCompat.getDrawable(
                    context,
                    zeemart.asia.buyers.R.drawable.white_round_corner_dashboard
                )
                filtermarket_cardview.background = ContextCompat.getDrawable(
                    context,
                    zeemart.asia.buyers.R.drawable.white_round_corner_dashboard
                )
                lyt_supplier.background = ContextCompat.getDrawable(
                    context,
                    zeemart.asia.buyers.R.drawable.white_round_corner_dashboard
                )
                filtermarket_cardview1.background = ContextCompat.getDrawable(
                    context,
                    zeemart.asia.buyers.R.drawable.white_round_corner_dashboard
                )
                lytCertifications.background = ContextCompat.getDrawable(
                    context,
                    zeemart.asia.buyers.R.drawable.white_round_corner_dashboard
                )
                filtermarket_cardview3.background = ContextCompat.getDrawable(
                    context,
                    zeemart.asia.buyers.R.drawable.white_round_corner_dashboard
                )
            }
            txtTags.setTextColor(context.resources.getColor(zeemart.asia.buyers.R.color.text_blue))
            // txtTagsHighLight.setVisibility(View.VISIBLE);
            txtCategories.setTextColor(context.resources.getColor(zeemart.asia.buyers.R.color.black))
            // txtCategoriesHighLight.setVisibility(View.GONE);
            txtCertifications.setTextColor(context.resources.getColor(zeemart.asia.buyers.R.color.black))
            txt_supplier.setTextColor(context.resources.getColor(zeemart.asia.buyers.R.color.black))
            // txtCertificationsHighLight.setVisibility(View.GONE);
            lstFilterCategories.visibility = View.GONE
            lstFilterCertificates.visibility = View.GONE
            lst_filter_suppliers.visibility = View.GONE
            if (outletTagsList != null && outletTagsList.size > 0) {
                lstFilterTags.visibility = View.VISIBLE
                lytNoResultCategories.visibility = View.GONE
                lytNoResultTags.visibility = View.GONE
            } else {
                lstFilterTags.visibility = View.GONE
                lytNoResultCategories.visibility = View.GONE
                lytNoResultTags.visibility = View.VISIBLE
            }
        }
        lyt_supplier.setOnClickListener {
            if (lyt_supplier.isClickable) {
                lyt_supplier.background = ContextCompat.getDrawable(
                    context,
                    zeemart.asia.buyers.R.drawable.light_blue_rounded_corner
                )
                filtermarket_cardview1.background = ContextCompat.getDrawable(
                    context,
                    zeemart.asia.buyers.R.drawable.light_blue_rounded_corner
                )
                lytCategories.background = ContextCompat.getDrawable(
                    context,
                    zeemart.asia.buyers.R.drawable.white_round_corner_dashboard
                )
                filtermarket_cardview.background = ContextCompat.getDrawable(
                    context,
                    zeemart.asia.buyers.R.drawable.white_round_corner_dashboard
                )
                lytTags.background = ContextCompat.getDrawable(
                    context,
                    zeemart.asia.buyers.R.drawable.white_round_corner_dashboard
                )
                filtermarket_cardview2.background = ContextCompat.getDrawable(
                    context,
                    zeemart.asia.buyers.R.drawable.white_round_corner_dashboard
                )
                lytCertifications.background = ContextCompat.getDrawable(
                    context,
                    zeemart.asia.buyers.R.drawable.white_round_corner_dashboard
                )
                filtermarket_cardview3.background = ContextCompat.getDrawable(
                    context,
                    zeemart.asia.buyers.R.drawable.white_round_corner_dashboard
                )
            }
            txt_supplier.setTextColor(context.resources.getColor(zeemart.asia.buyers.R.color.text_blue))
            // txtTagsHighLight.setVisibility(View.VISIBLE);
            txtCategories.setTextColor(context.resources.getColor(zeemart.asia.buyers.R.color.black))
            // txtCategoriesHighLight.setVisibility(View.GONE);
            txtCertifications.setTextColor(context.resources.getColor(zeemart.asia.buyers.R.color.black))
            txtTags.setTextColor(context.resources.getColor(zeemart.asia.buyers.R.color.black))
            // txtCertificationsHighLight.setVisibility(View.GONE);
            lstFilterCategories.visibility = View.GONE
            lstFilterCertificates.visibility = View.GONE
            lstFilterTags.visibility = View.GONE
            if (outletSupplierList != null && outletSupplierList.size > 0) {
                lst_filter_suppliers.visibility = View.VISIBLE
                lytNoResultCategories.visibility = View.GONE
                lytNoResultTags.visibility = View.GONE
            } else {
                lst_filter_suppliers.visibility = View.GONE
                lytNoResultCategories.visibility = View.GONE
                lytNoResultTags.visibility = View.VISIBLE
            }
        }
        lytCategories.background = ContextCompat.getDrawable(
            context,
            zeemart.asia.buyers.R.drawable.light_blue_rounded_corner
        )
        filtermarket_cardview.background = ContextCompat.getDrawable(
            context,
            zeemart.asia.buyers.R.drawable.light_blue_rounded_corner
        )
        lyt_supplier.background = ContextCompat.getDrawable(
            context,
            zeemart.asia.buyers.R.drawable.white_round_corner_dashboard
        )
        filtermarket_cardview1.background = ContextCompat.getDrawable(
            context,
            zeemart.asia.buyers.R.drawable.white_round_corner_dashboard
        )
        lytTags.background = ContextCompat.getDrawable(
            context,
            zeemart.asia.buyers.R.drawable.white_round_corner_dashboard
        )
        filtermarket_cardview2.background = ContextCompat.getDrawable(
            context,
            zeemart.asia.buyers.R.drawable.white_round_corner_dashboard
        )
        lytCertifications.background = ContextCompat.getDrawable(
            context,
            zeemart.asia.buyers.R.drawable.white_round_corner_dashboard
        )
        filtermarket_cardview3.background = ContextCompat.getDrawable(
            context,
            zeemart.asia.buyers.R.drawable.white_round_corner_dashboard
        )
        lytCategories.setOnClickListener {
            if (lytCategories.isClickable) {
                lytCategories.background = ContextCompat.getDrawable(
                    context,
                    zeemart.asia.buyers.R.drawable.light_blue_rounded_corner
                )
                filtermarket_cardview.background = ContextCompat.getDrawable(
                    context,
                    zeemart.asia.buyers.R.drawable.light_blue_rounded_corner
                )
                lyt_supplier.background = ContextCompat.getDrawable(
                    context,
                    zeemart.asia.buyers.R.drawable.white_round_corner_dashboard
                )
                filtermarket_cardview1.background = ContextCompat.getDrawable(
                    context,
                    zeemart.asia.buyers.R.drawable.white_round_corner_dashboard
                )
                lytTags.background = ContextCompat.getDrawable(
                    context,
                    zeemart.asia.buyers.R.drawable.white_round_corner_dashboard
                )
                filtermarket_cardview2.background = ContextCompat.getDrawable(
                    context,
                    zeemart.asia.buyers.R.drawable.white_round_corner_dashboard
                )
                lytCertifications.background = ContextCompat.getDrawable(
                    context,
                    zeemart.asia.buyers.R.drawable.white_round_corner_dashboard
                )
                filtermarket_cardview3.background = ContextCompat.getDrawable(
                    context,
                    zeemart.asia.buyers.R.drawable.white_round_corner_dashboard
                )
            }
            txtCategories.setTextColor(context.resources.getColor(zeemart.asia.buyers.R.color.text_blue))
            // txtCategoriesHighLight.setVisibility(View.VISIBLE);
            txtTags.setTextColor(context.resources.getColor(zeemart.asia.buyers.R.color.black))
            //  txtTagsHighLight.setVisibility(View.GONE);
            txtCertifications.setTextColor(context.resources.getColor(zeemart.asia.buyers.R.color.black))
            txt_supplier.setTextColor(context.resources.getColor(zeemart.asia.buyers.R.color.black))
            // txtCertificationsHighLight.setVisibility(View.GONE);
            lstFilterTags.visibility = View.GONE
            lstFilterCertificates.visibility = View.GONE
            lst_filter_suppliers.visibility = View.GONE
            if (outletCategoriesList != null && outletCategoriesList.size > 0) {
                lstFilterCategories.visibility = View.VISIBLE
                lytNoResultCategories.visibility = View.GONE
                lytNoResultTags.visibility = View.GONE
            } else {
                lstFilterCategories.visibility = View.GONE
                lytNoResultCategories.visibility = View.VISIBLE
                lytNoResultTags.visibility = View.GONE
            }
        }
        lytCertifications.setOnClickListener {
            if (lytCertifications.isClickable) {
                lytCertifications.background = ContextCompat.getDrawable(
                    context,
                    zeemart.asia.buyers.R.drawable.light_blue_rounded_corner
                )
                filtermarket_cardview3.background = ContextCompat.getDrawable(
                    context,
                    zeemart.asia.buyers.R.drawable.light_blue_rounded_corner
                )
                lytCategories.background = ContextCompat.getDrawable(
                    context,
                    zeemart.asia.buyers.R.drawable.white_round_corner_dashboard
                )
                filtermarket_cardview.background = ContextCompat.getDrawable(
                    context,
                    zeemart.asia.buyers.R.drawable.white_round_corner_dashboard
                )
                lyt_supplier.background = ContextCompat.getDrawable(
                    context,
                    zeemart.asia.buyers.R.drawable.white_round_corner_dashboard
                )
                filtermarket_cardview1.background = ContextCompat.getDrawable(
                    context,
                    zeemart.asia.buyers.R.drawable.white_round_corner_dashboard
                )
                lytTags.background = ContextCompat.getDrawable(
                    context,
                    zeemart.asia.buyers.R.drawable.white_round_corner_dashboard
                )
                filtermarket_cardview2.background = ContextCompat.getDrawable(
                    context,
                    zeemart.asia.buyers.R.drawable.white_round_corner_dashboard
                )
            }
            txtCategories.setTextColor(context.resources.getColor(zeemart.asia.buyers.R.color.black))
            txt_supplier.setTextColor(context.resources.getColor(zeemart.asia.buyers.R.color.black))
            // txtCategoriesHighLight.setVisibility(View.GONE);
            txtTags.setTextColor(context.resources.getColor(zeemart.asia.buyers.R.color.black))
            // txtTagsHighLight.setVisibility(View.GONE);
            txtCertifications.setTextColor(context.resources.getColor(zeemart.asia.buyers.R.color.text_blue))
            // txtCertificationsHighLight.setVisibility(View.VISIBLE);
            lstFilterTags.visibility = View.GONE
            lstFilterCategories.visibility = View.GONE
            lst_filter_suppliers.visibility = View.GONE
            if (outletCertificationList != null && outletCertificationList.size > 0) {
                lstFilterCertificates.visibility = View.VISIBLE
                lytNoResultCategories.visibility = View.GONE
                lytNoResultTags.visibility = View.GONE
            } else {
                lstFilterCertificates.visibility = View.GONE
                lytNoResultCategories.visibility = View.VISIBLE
                lytNoResultTags.visibility = View.GONE
            }
        }
        lstFilterTags.isNestedScrollingEnabled = false
        val flexboxLayoutManager = LinearLayoutManager(context)
        lstFilterTags.layoutManager = flexboxLayoutManager
        //  lstFilterTags.addItemDecoration(new TagsItemDecoration(15));
        if (outletTagsList != null && outletTagsList.size > 0) lstFilterTags.adapter =
            ProductsFilterListingAdapter(object :
                ProductsFilterListingAdapter.ProductListSelectedFilterItemClickListner {
                override fun onFilterSelected(outletTags: OutletTags?, textView: TextView?) {
                    mTagsListener.onFilterSelected(outletTags, txtTagsCount)
                }

                override fun onFilterDeselected(outletTags: OutletTags?, textView: TextView?) {
                    mTagsListener.onFilterDeselected(outletTags, txtTagsCount)
                }
            }, context, outletTagsList)
        lstFilterCategories.isNestedScrollingEnabled = false
        val flexboxLayoutManagerCategories = LinearLayoutManager(context)
        lstFilterCategories.layoutManager = flexboxLayoutManagerCategories
        //   lstFilterCategories.addItemDecoration(new TagsItemDecoration(15));
        lstFilterCategories.adapter = ProductsFilterListingAdapter(object :
            ProductsFilterListingAdapter.ProductListSelectedFilterItemClickListner {
            override fun onFilterSelected(outletTags: OutletTags?, textView: TextView?) {
                mCategoriesListener.onFilterSelected(outletTags, txtCategoriesCount)
            }

            override fun onFilterDeselected(outletTags: OutletTags?, textView: TextView?) {
                mCategoriesListener.onFilterDeselected(outletTags, txtCategoriesCount)
            }
        }, context, outletCategoriesList)
        lstFilterCertificates.isNestedScrollingEnabled = false
        val flexboxLayoutManagerCertification = LinearLayoutManager(context)
        lstFilterCertificates.layoutManager = flexboxLayoutManagerCertification
        //  lstFilterCertificates.addItemDecoration(new TagsItemDecoration(15));
        lstFilterCertificates.adapter = ProductsFilterListingAdapter(object :
            ProductsFilterListingAdapter.ProductListSelectedFilterItemClickListner {
            override fun onFilterSelected(outletTags: OutletTags?, textView: TextView?) {
                mCertificationListener.onFilterSelected(outletTags, txtCertificationsCount)
            }

            override fun onFilterDeselected(outletTags: OutletTags?, textView: TextView?) {
                mCertificationListener.onFilterDeselected(outletTags, txtCertificationsCount)
            }
        }, context, outletCertificationList)
        lst_filter_suppliers.isNestedScrollingEnabled = false
        val flexboxLayoutManagersupplier = LinearLayoutManager(context)
        lst_filter_suppliers.layoutManager = flexboxLayoutManagersupplier
        //lst_filter_suppliers.addItemDecoration(new TagsItemDecoration(15));
        lst_filter_suppliers.adapter = ProductsFilterListingAdapter(object :
            ProductsFilterListingAdapter.ProductListSelectedFilterItemClickListner {
            override fun onFilterSelected(outletTags: OutletTags?, textView: TextView?) {
                mSupplierListner.onFilterSelected(outletTags, txt_number_of_supplier)
            }

            override fun onFilterDeselected(outletTags: OutletTags?, textView: TextView?) {
                mSupplierListner.onFilterDeselected(outletTags, txt_number_of_supplier)
            }
        }, context, outletSupplierList)

        /* d.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

                if (d != null){
                    mCategoriesListener.onClear();

                }
                mCategoriesListener.onClear();
                d.dismiss();
                if (!iscliked[0]){
                    if (!d.isShowing()){
                        mCategoriesListener.onClear();
                        d.dismiss();
                    }

                }
            }
        });*/dismissBg.setOnClickListener { d.dismiss() }
        if (context is Activity && !context.isFinishing) {
            d.show()
        }
    }

    @JvmStatic
    fun alertDialogPaymentSuccess(activity: Activity, successMessage: String?): AlertDialog {
        val DIALOG_LAYOUT_WIDTH = CommonMethods.dpToPx(180)
        val DIALOG_LAYOUT_HEIGHT = CommonMethods.dpToPx(180)
        val builder = AlertDialog.Builder(activity)
        val v =
            activity.layoutInflater.inflate(zeemart.asia.buyers.R.layout.small_dialog_success, null)
        val txtMessage = v.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_small_dialog_success)
        ZeemartBuyerApp.setTypefaceView(
            txtMessage,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        txtMessage.text = successMessage
        builder.setView(v)
        val dialog = builder.create()
        dialog.show()
        dialog.window!!.setLayout(DIALOG_LAYOUT_WIDTH, DIALOG_LAYOUT_HEIGHT)
        return dialog
    }

    @JvmStatic
    fun alertDialogPaymentFailure(
        activity: Activity,
        failureHeading: String?,
        failureMessage: String?,
    ): AlertDialog {
        val DIALOG_LAYOUT_WIDTH = CommonMethods.dpToPx(180)
        val builder = AlertDialog.Builder(activity)
        val v =
            activity.layoutInflater.inflate(zeemart.asia.buyers.R.layout.small_dialog_failure, null)
        val txtHeading = v.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_small_dialog_failure)
        ZeemartBuyerApp.setTypefaceView(
            txtHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        txtHeading.text = failureHeading
        val txtMessage =
            v.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_small_dialog_failure_message)
        ZeemartBuyerApp.setTypefaceView(
            txtMessage,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        txtMessage.text = failureMessage
        builder.setView(v)
        val dialog = builder.create()
        dialog.show()
        dialog.window!!.setLayout(DIALOG_LAYOUT_WIDTH, WindowManager.LayoutParams.WRAP_CONTENT)
        return dialog
    }

    @JvmStatic
    fun alertPaymentDialogSmallSuccess(
        activity: Activity,
        successHeading: String?,
        successMessage: String?,
    ): AlertDialog {
        val DIALOG_LAYOUT_WIDTH = CommonMethods.dpToPx(250)
        val builder = AlertDialog.Builder(activity)
        val v = activity.layoutInflater.inflate(
            zeemart.asia.buyers.R.layout.small_payment_success,
            null
        )
        val txtHeading = v.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_small_dialog_success)
        txtHeading.text = successHeading
        val txtMessage =
            v.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_small_dialog_success_message)
        txtMessage.text = successMessage
        ZeemartBuyerApp.setTypefaceView(
            txtHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtMessage,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        builder.setView(v)
        val dialog = builder.create()
        dialog.show()
        dialog.window!!.setLayout(DIALOG_LAYOUT_WIDTH, WindowManager.LayoutParams.WRAP_CONTENT)
        return dialog
    }

    @JvmStatic
    fun alertPaymentDialogSmallFailure(
        activity: Activity,
        failureHeading: String?,
        failureMessage: String?,
    ): AlertDialog {
        val DIALOG_LAYOUT_WIDTH = CommonMethods.dpToPx(150)
        val builder = AlertDialog.Builder(activity)
        val v =
            activity.layoutInflater.inflate(zeemart.asia.buyers.R.layout.small_dialog_failure, null)
        val txtHeading = v.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_small_dialog_failure)
        txtHeading.text = failureHeading
        val txtMessage =
            v.findViewById<TextView>(zeemart.asia.buyers.R.id.txt_small_dialog_failure_message)
        txtMessage.text = failureMessage
        ZeemartBuyerApp.setTypefaceView(
            txtHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtMessage,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        builder.setView(v)
        val dialog = builder.create()
        dialog.show()
        dialog.window!!.setLayout(DIALOG_LAYOUT_WIDTH, WindowManager.LayoutParams.WRAP_CONTENT)
        return dialog
    }

    interface RejectOrderCallback {
        fun rejectOrder(remarks: String?)
        fun dismiss()
    }

    interface CancelOrderCallback {
        fun cancelOrder(remarks: String?)
        fun dismiss()
    }

    interface LanguageSelectionCallback {
        fun selected(locale: Locale?, language: String?)
        fun dismiss()
    }

    interface ReceiveOrderListener {
        fun onReceiveSuccessful()
        fun onReceiveError()
        fun onDialogDismiss()
    }

    interface DialogButtonClickListener {
        fun positiveButtonClicked()
        fun negativeButtonClicked()
    }

    interface DialogThreeButtonClickListener {
        fun positiveButtonClicked()
        fun negativeButtonClicked()
        fun neutralButtonClicked()
    }

    interface onCardSelectedListener {
        fun onCardSelected(selectedCardDetails: PaymentCardDetails.PaymentResponse?)
    }

    interface onCardAddedListener {
        fun onCardSelected(selectedCardDetails: PaymentCardDetails.PaymentResponse)
    }

    interface onGrnEmailEditedListener {
        fun onSendSelected(edtNotes: String?, supplierEmails: String?)
    }

    interface onUomSelectListener {
        fun onUomSelect(unitSize: String?)
    }

    interface onButtonSaveClickListener {
        fun onButtonSaveClicked(
            lstData: OrderSettingDeliveryPreferences.Data?,
            isWhatsapp: Boolean?,
            isSms: Boolean?,
        )
    }

    interface onEmailEditDone {
        fun onEmailEdtDone(email: String?, mobileNumber: String?, whatsAppNumber: String?)
    }

    interface onEditDeliverySettingsListener {
        fun onEditedDeliverySettings(status: String?, cutoffTime: OrderSettingDeliveryPreferences.CutoffTime?)
    }

    interface ProductListSelectedFilterItemClickListener {
        fun onFilterSelected(isDefaultSelected: Boolean)
    }

    interface InvoicesSupplierFilterItemClickListener {
        fun onFilterSelected(selectedSuppliers: List<DetailSupplierDataModel>?)
    }

    interface InvoiceOrderDeleted {
        fun onDeleted()
        fun OnSuccess()
    }
}