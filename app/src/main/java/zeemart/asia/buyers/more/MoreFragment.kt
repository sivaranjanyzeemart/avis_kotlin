package zeemart.asia.buyers.more

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.android.volley.VolleyError
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import io.intercom.android.sdk.Intercom
import io.intercom.android.sdk.IntercomSpace
import io.intercom.android.sdk.UnreadConversationCountListener
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import me.leolin.shortcutbadger.ShortcutBadger
import zeemart.asia.buyers.R
import zeemart.asia.buyers.modelsimport.MultiPartImageUploadResponse
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.activities.BaseNavigationActivity
import zeemart.asia.buyers.companies.CompaniesListActivity
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.*
import zeemart.asia.buyers.helper.ImageRotationHelper
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.interfaces.GetRequestStatusResponseListener
import zeemart.asia.buyers.interfaces.PermissionCallback
import zeemart.asia.buyers.inventory.InventorySettingsActivity
import zeemart.asia.buyers.models.Companies
import zeemart.asia.buyers.models.RetrieveCompaniesResponse
import zeemart.asia.buyers.models.UserDetails
import zeemart.asia.buyers.models.UserPermission
import zeemart.asia.buyers.network.CompaniesApi
import zeemart.asia.buyers.network.ImageUploadHelper
import zeemart.asia.buyers.network.UsersApi
import zeemart.asia.buyers.network.VolleyErrorHelper
import zeemart.asia.buyers.notifications.NotificationSettings
import zeemart.asia.buyers.reports.reportsummary.ReportsDashboardActivity
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [MoreFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MoreFragment : Fragment() {
    private lateinit var buyerImage: ImageView
    private lateinit var txtVersion: TextView
    private var txtheaderhome: TextView? = null
    private lateinit var txtUserName: TextView
    private lateinit var txtUserZeemartId: TextView
    private var txtLanguageOptions: TextView? = null
    private var txtChangePassword: TextView? = null
    private var txtTerms: TextView? = null
    private var txtPrivacy: TextView? = null
    private var txtLogout: TextView? = null
    private var txtHelp: TextView? = null
    private var txtAsk: TextView? = null
    private var txtLanguage: TextView? = null
    private var txtSendFeedback: TextView? = null
    private var txtReports: TextView? = null
    private lateinit var lytChangePassword: RelativeLayout
    private lateinit var lytPreference: RelativeLayout
    private lateinit var lytTerms: RelativeLayout
    private lateinit var lytCompanies: RelativeLayout
    private lateinit var txtUnverified: TextView
    private lateinit var iconUnverified: ImageView
    private lateinit var lytPrivacy: RelativeLayout
    private lateinit var lytLogout: RelativeLayout
    private lateinit var lytHelp: RelativeLayout
    private lateinit var lytAsk: RelativeLayout
    private lateinit var lytLanguage: RelativeLayout
    private lateinit var lytSendFeedback: RelativeLayout
    private lateinit var lytReports: RelativeLayout
    var outPutfileUri: Uri? = null
    private var isFragmentAttached = false
    private var progressBar: ProgressBar? = null
    private lateinit var txtIntercomUnread: TextView
    private lateinit var lytPromoCode: RelativeLayout
    private var txtPromoCode: TextView? = null
    private var txtPreference: TextView? = null
    private lateinit var lytReferFriend: RelativeLayout
    private var txtReferFriend: TextView? = null
    private var txtCompanies: TextView? = null
    private lateinit var lytNotifications: RelativeLayout
    private var txtNotifications: TextView? = null
    override fun onResume() {
        super.onResume()
        //test for intercom unread messages
        val unreadIntercomChats: Int = Intercom.client().unreadConversationCount
        if (unreadIntercomChats == 0) {
            (activity as BaseNavigationActivity?)?.removeBadge(ZeemartAppConstants.MORE_TAB_ID)
            txtIntercomUnread?.setVisibility(View.GONE)
        } else {
            (activity as BaseNavigationActivity?)?.setUnreadBadgeOnTab(
                unreadIntercomChats,
                ZeemartAppConstants.MORE_TAB_ID
            )
            txtIntercomUnread?.setVisibility(View.VISIBLE)
            txtIntercomUnread?.setText(unreadIntercomChats.toString() + "")
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        isFragmentAttached = true
    }

    override fun onDetach() {
        super.onDetach()
        isFragmentAttached = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v: View = inflater.inflate(R.layout.layout_more_root, container, false)
        progressBar = v.findViewById<ProgressBar>(R.id.progressBarMore)
        txtVersion = v.findViewById<TextView>(R.id.txt_version)
        txtheaderhome = v.findViewById<TextView>(R.id.txt_header_home)
        txtIntercomUnread = v.findViewById<TextView>(R.id.txt_intercom_unread)
        txtIntercomUnread.setVisibility(View.GONE)
        ZeemartBuyerApp.setTypefaceView(
            txtheaderhome,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        try {
            val pInfo = requireActivity().packageManager.getPackageInfo(
                requireActivity().packageName, 0
            )
            val version = pInfo.versionName
            val year = "" + Calendar.getInstance()[Calendar.YEAR]
            txtVersion.setText(String.format(getString(R.string.format_txt_version), version, year))
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        lytReports = v.findViewById<RelativeLayout>(R.id.lyt_reports)
        lytReports.setOnClickListener(View.OnClickListener {
            val newIntent = Intent(activity, ReportsDashboardActivity::class.java)
            startActivity(newIntent)
        })

        /* if (UserPermission.HasViewReport()) {
            lytReports.setVisibility(View.VISIBLE);
        } else {
            lytReports.setVisibility(View.GONE);
        }*/txtReports = v.findViewById<TextView>(R.id.txt_reports)
        txtHelp = v.findViewById<TextView>(R.id.txt_help)
        txtAsk = v.findViewById<TextView>(R.id.txt_ask_zeemart)
        txtLanguageOptions = v.findViewById<TextView>(R.id.txt_language_options)
        txtLanguage = v.findViewById<TextView>(R.id.txt_language)
        txtTerms = v.findViewById<TextView>(R.id.txt_terms)
        txtPrivacy = v.findViewById<TextView>(R.id.txt_privacy)
        txtChangePassword = v.findViewById<TextView>(R.id.txt_change_password)
        txtLogout = v.findViewById<TextView>(R.id.txt_sign_out)
        txtSendFeedback = v.findViewById<TextView>(R.id.txt_send_feedback)
        txtUserName = v.findViewById<TextView>(R.id.txt_buyer_name)
        txtUserName.setText(
            SharedPref.read(SharedPref.USER_FIRST_NAME, "") + " " + SharedPref.read(SharedPref.USER_LAST_NAME, "")
        )
        txtUserZeemartId = v.findViewById<TextView>(R.id.txt_buyer_id)
        txtUserZeemartId.setText(SharedPref.read(SharedPref.USERNAME, ""))
        buyerImage = v.findViewById(R.id.buyerImage)
        lytPromoCode = v.findViewById<RelativeLayout>(R.id.lyt_promo_code)
        txtPromoCode = v.findViewById<TextView>(R.id.txt_promo_code)
        txtPreference = v.findViewById<TextView>(R.id.txt_settings)
        if (!SharedPref.read(SharedPref.USER_IMAGE_URL, "")?.isEmpty()!!) {
            Picasso.get().load(SharedPref.read(SharedPref.USER_IMAGE_URL, ""))
                .transform(CircleBorderTransformation())
                .fit()
                .centerCrop()
                .into(buyerImage)
        } else {
            Picasso.get().load(R.drawable.placeholder_user)
                .transform(CircleBorderTransformation())
                .fit()
                .centerCrop()
                .into(buyerImage)
        }
        buyerImage.setOnClickListener(View.OnClickListener { imagePickerDialog() })
        lytHelp = v.findViewById<RelativeLayout>(R.id.lyt_help)
        lytHelp.setOnClickListener(View.OnClickListener {
            AnalyticsHelper.logAction(activity, AnalyticsHelper.TAP_MORE_TAB_HELP)
            Intercom.client().present(IntercomSpace.HelpCenter)
        })
        lytAsk = v.findViewById<RelativeLayout>(R.id.lyt_ask_zeemart)
        lytAsk.setOnClickListener(View.OnClickListener {
            AnalyticsHelper.logAction(activity, AnalyticsHelper.TAP_MORE_TAB_ASK_ZEEMART)
            Intercom.client().present()
        })
        lytLanguage = v.findViewById<RelativeLayout>(R.id.lyt_language)
        lytLanguage.setOnClickListener(View.OnClickListener {
            AnalyticsHelper.logAction(activity, AnalyticsHelper.TAP_MORE_TAB_LANGUAGE)
            activity?.let { it1 ->
                DialogHelper.ShowLanguageSelection(it1, object :
                    DialogHelper.LanguageSelectionCallback {
                    override fun selected(locale: Locale?, language: String?) {
                        editUserLanguage(language!!, activity)
                        ZeemartBuyerApp.ChangeLanguageLocale(activity!!, locale)
                        BaseNavigationActivity.reloadTab(activity!!, ZeemartAppConstants.MORE_TAB_ID)
                    }

                    override fun dismiss() {}
                })
            }
        })
        lytCompanies = v.findViewById<RelativeLayout>(R.id.lyt_companies)
        txtUnverified = v.findViewById<TextView>(R.id.txt_unverified)
        iconUnverified = v.findViewById(R.id.icon_unverified)
        txtCompanies = v.findViewById<TextView>(R.id.txt_companies)
        lytCompanies.setOnClickListener(View.OnClickListener {
            val newIntent = Intent(activity, CompaniesListActivity::class.java)
            startActivity(newIntent)
        })
        if (ZeemartAppConstants.Market.`this`.isMarket(ZeemartAppConstants.Market.SINGAPORE)) {
            if (UserPermission.HasEditCompanies()) {
                lytCompanies.setVisibility(View.VISIBLE)
                txtUnverified.setVisibility(View.GONE)
                iconUnverified.setVisibility(View.GONE)
                callCompaniesApi()
            } else {
                lytCompanies.setVisibility(View.GONE)
            }
        } else {
            lytCompanies.setVisibility(View.GONE)
        }
        lytTerms = v.findViewById<RelativeLayout>(R.id.lyt_terms)
        lytTerms.setOnClickListener(View.OnClickListener {
            AnalyticsHelper.logAction(activity, AnalyticsHelper.TAP_MORE_TAB_TERMS_OF_USE)
            val uri = Uri.parse(ServiceConstant.ENDPOINT_ZEEMART_TERMS)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        })
        lytPrivacy = v.findViewById<RelativeLayout>(R.id.lyt_privacy)
        lytPrivacy.setOnClickListener(View.OnClickListener {
            AnalyticsHelper.logAction(activity, AnalyticsHelper.TAP_MORE_TAB_PRIVACY_POLICY)
            val uri = Uri.parse(ServiceConstant.ENDPOINT_ZEEMART_PRIVACY)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        })
        lytChangePassword = v.findViewById<RelativeLayout>(R.id.lyt_change_password)
        lytChangePassword.setOnClickListener(View.OnClickListener {
            AnalyticsHelper.logAction(activity, AnalyticsHelper.TAP_MORE_TAB_CHANGE_PASSWORD)
            requireActivity().startActivity(Intent(activity, ChangePasswordSettingActivity::class.java))
        })
        lytPreference = v.findViewById<RelativeLayout>(R.id.lyt_settings)
        lytPreference.setOnClickListener(View.OnClickListener { preferenceSelect() })
        lytLogout = v.findViewById<RelativeLayout>(R.id.lyt_sign_out)
        lytLogout.setOnClickListener(View.OnClickListener {
            AnalyticsHelper.logAction(activity, AnalyticsHelper.TAP_MORE_TAB_SIGN_OUT)
            dialogConfirmLogout()
        })
        lytSendFeedback = v.findViewById<RelativeLayout>(R.id.lyt_send_feedback)
        lytSendFeedback.setOnClickListener(View.OnClickListener {
            AnalyticsHelper.logAction(activity, AnalyticsHelper.TAP_MORE_TAB_SEND_FEEDBACK)
            EmailHelper.SendFeedback(requireContext())
        })
        Intercom.client()
            .addUnreadConversationCountListener(object : UnreadConversationCountListener {
                override fun onCountUpdate(i: Int) {
                    if (i == 0) {
                        txtIntercomUnread.setVisibility(View.GONE)
                    } else {
                        txtIntercomUnread.setVisibility(View.VISIBLE)
                        txtIntercomUnread.setText(i.toString() + "")
                    }
                }
            })
        lytPromoCode.setOnClickListener(View.OnClickListener {
            AnalyticsHelper.logAction(activity, AnalyticsHelper.TAP_MORE_TAB_PROMO_CODE)
            val newIntent = Intent(activity, AllPromoCodesActivity::class.java)
            startActivity(newIntent)
        })
        lytReferFriend = v.findViewById<RelativeLayout>(R.id.lyt_refer)
        txtReferFriend = v.findViewById<TextView>(R.id.txt_refer_friend)
        lytReferFriend.setOnClickListener(View.OnClickListener {
            val uri = Uri.parse(ServiceConstant.ENDPOINT_ZEEMART_REFER_URL)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        })
        if (ZeemartAppConstants.Market.`this`.isMarket(ZeemartAppConstants.Market.SINGAPORE)) {
            lytReferFriend.setVisibility(View.VISIBLE)
        } else {
            lytReferFriend.setVisibility(View.GONE)
        }
        lytNotifications = v.findViewById<RelativeLayout>(R.id.lyt_notifications)
        txtNotifications = v.findViewById<TextView>(R.id.txt_notifications)
        lytNotifications.setOnClickListener(View.OnClickListener {
            AnalyticsHelper.logAction(
                activity,
                AnalyticsHelper.TAP_NOTIFICATIONS_NOTIFICATION_SETTINGS
            )
            val newIntent = Intent(activity, NotificationSettings::class.java)
            startActivity(newIntent)
        })
        setFonts()
        // Inflate the layout_more_root for this fragment
        return v
    }

    private fun preferenceSelect() {
        val i = Intent(activity, InventorySettingsActivity::class.java)
        startActivityForResult(i, LANGUAGE_SELECTION)
        //        getActivity().startActivity(new Intent(getActivity(), InventorySettingsActivity.class));
    }

    private fun setFonts() {
        ZeemartBuyerApp.setTypefaceView(
            txtUserName,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtUserZeemartId,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtVersion,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtheaderhome,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtChangePassword,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtHelp,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtAsk,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtLanguage,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtLanguageOptions,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtLogout,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtPrivacy,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtReports,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtTerms,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtSendFeedback,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtIntercomUnread,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            txtPromoCode,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtPreference,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtReferFriend,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtUnverified,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtCompanies,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            txtNotifications,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
    }

    private fun callCompaniesApi() {
        val companyIds: String = CommonMethods.commaSepratedCompanyList ?: return
        val apiParamsHelper = ApiParamsHelper()
        apiParamsHelper.setCompanyId(companyIds)
        CompaniesApi.retrieveCompanies(
            context,
            apiParamsHelper,
            SharedPref.allOutlets,
            object : CompaniesApi.CompaniesResponseListener {
                override fun onSuccessResponse(response: RetrieveCompaniesResponse.Response?) {
                    if (response != null && response.data != null && response.data!!.companies != null && response.data!!.companies!!.size > 0) {
                        val companies: List<Companies>? = response.data!!.companies
                        for (i in companies!!.indices) {
                            val company: Companies = companies[i]
                            if (company.verificationStatus == "unVerified") {
                                txtUnverified.setVisibility(View.VISIBLE)
                                iconUnverified!!.visibility = View.VISIBLE
                                break
                            }
                        }
                    }
                }

                override fun onErrorResponse(error: VolleyError?) {}
            })
    }

    private fun dialogConfirmLogout() {
        val builder = AlertDialog.Builder(activity)
        builder.setPositiveButton(
            R.string.dialog_ok_button_text,
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    CommonMethods.unregisterLogout(requireContext())
                    ShortcutBadger.applyCount(context, 0)
                    dialog.dismiss()
                }
            })
        builder.setNegativeButton(
            R.string.dialog_cancel_button_text,
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    dialog.dismiss()
                }
            })
        val dialog = builder.create()
        dialog.setTitle(getString(R.string.txt_logout_title))
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setMessage(getString(R.string.txt_logout_confirmation))
        dialog.show()
    }

    private fun imagePickerDialog() {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(R.string.txt_select_photo_from)
        builder.setPositiveButton(R.string.txt_camera, object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, id: Int) {
                dialog.dismiss()
                if (activity is BaseNavigationActivity) {
                    (activity as BaseNavigationActivity?)?.requestPermission(
                        PermissionCallback.TAKE_IMAGE,
                        object : PermissionCallback {
                            override fun denied(requestCode: Int) {}
                            override fun allowed(requestCode: Int) {
                                imagePicker(PICK_FROM_CAMERA)
                            }
                        })
                }
            }
        })
        builder.setNegativeButton(R.string.txt_gallery, object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, id: Int) {
                dialog.dismiss()
                if (activity is BaseNavigationActivity) {
                    (activity as BaseNavigationActivity?)?.requestPermission(
                        PermissionCallback.WRITE_IMAGE,
                        object : PermissionCallback {
                            override fun denied(requestCode: Int) {}
                            override fun allowed(requestCode: Int) {
                                imagePicker(PICK_FROM_GALLERY)
                            }
                        })
                }
            }
        })

        // Create the AlertDialog
        val dialog = builder.create()
        dialog.show()
    }

    private fun imagePicker(source: Int) {
        if (source == PICK_FROM_CAMERA) {
            val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val auth =
                requireActivity().applicationContext.packageName + ".zeemart.asia.buyers.fileprovider"
            try {
                outPutfileUri = activity?.let { FileProvider.getUriForFile(it, auth, createImageFile()) }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outPutfileUri)
            captureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivityForResult(captureIntent, PICK_FROM_CAMERA)
        } else if (source == PICK_FROM_GALLERY) {
            val captureIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            captureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivityForResult(captureIntent, PICK_FROM_GALLERY)
        }
    }

    //    public boolean verifyStoragePermissions(Activity activity) {
    //        // Check if we have write permission
    //        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
    //
    //        if (permission != PackageManager.PERMISSION_GRANTED) {
    //            // We don't have permission so prompt the user
    //            this.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
    //            return false;
    //        }
    //        return true;
    //    }
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir =
            requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            imageFileName,  /* prefix */
            ServiceConstant.FILE_EXT_JPEG,  /* suffix */
            storageDir /* directory */
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
            if (outPutfileUri != null) {
                try {
                    val bitmap: Bitmap = ImageRotationHelper.handleSamplingAndRotationBitmap(
                        requireContext(), outPutfileUri
                    )!!
                    uploadImage(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else if (requestCode == PICK_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                try {
                    val selectedImage: Uri? = data.getData()
                    val filePathColumn = arrayOf<String>(MediaStore.Images.Media.DATA)

                    // Get the cursor
                    val cursor = selectedImage?.let {
                        requireActivity().contentResolver.query(
                            it,
                            filePathColumn,
                            null,
                            null,
                            null
                        )
                    }
                    // Move to first row
                    cursor!!.moveToFirst()
                    val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                    val imgDecodableString = cursor.getString(columnIndex)
                    cursor.close()
                    //Bitmap bitmap = BitmapFactory.decodeFile(imgDecodableString);
                    val bitmap: Bitmap = ImageRotationHelper.handleSamplingAndRotationBitmap(
                        requireContext(), Uri.fromFile(File(imgDecodableString))
                    )!!
                    uploadImage(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else if (requestCode == LANGUAGE_SELECTION && resultCode == 217) {
            if (data != null) {
//                Locale locale = data.getStringExtra("LOCALE");
                val language: String? = data.getStringExtra("LANGUAGE")
                if (language != null) {
                    Log.d("LANG====>", language)
                }
                var locale: Locale = ZeemartAppConstants.Language.ENGLISH.locale
                when (language) {
                    "zh_CN" -> locale = ZeemartAppConstants.Language.CHINESE.locale
                    "id_ID" -> locale = ZeemartAppConstants.Language.BAHASA_INDONESIA.locale
                    "en_GB" -> locale = ZeemartAppConstants.Language.ENGLISH.locale
                    "ms_MY" -> locale = ZeemartAppConstants.Language.BAHASA_MELAYU.locale
                    "vi_VN" -> locale = ZeemartAppConstants.Language.VIETNAMESE.locale
                    "hi_IN" -> locale = ZeemartAppConstants.Language.HINDI.locale
                }
                if (language != null) {
                    editUserLanguage(language, activity)
                }
                activity?.let { ZeemartBuyerApp.ChangeLanguageLocale(it, locale) }
                activity?.let { BaseNavigationActivity.reloadTab(it, ZeemartAppConstants.MORE_TAB_ID) }
            }
        }
    }

    private fun uploadImage(bitmap: Bitmap) {
        progressBar?.setVisibility(View.VISIBLE)
        val currentDate: String =
            DateHelper.getDateInYearMonthDayHourMinSec(Calendar.getInstance().timeInMillis / 1000)
        val filename = currentDate + "_" + SharedPref.read(SharedPref.USER_ID, "")
        activity?.let {
            ImageUploadHelper.UploadFileMultipart(
                it,
                bitmap,
                null,
                "PROFILE",
                filename,
                object : ImageUploadHelper.MultiPartImageUploaded {

                    override fun result(imageUploads: MultiPartImageUploadResponse?) {
                        if (isFragmentAttached) {
                            if (imageUploads?.data != null && imageUploads.data!!.files.isNotEmpty() && !StringHelper.isStringNullOrEmpty(
                                    imageUploads.data!!.files[0].fileUrl
                                )
                            ) {
                                val url: String? = imageUploads.data!!.files.get(0).fileUrl
                                val userDetails: UserDetails = SharedPref.currentUserDetail
                                userDetails.imageURL = url
                                UsersApi.editSingleUser(
                                    activity,
                                    userDetails,
                                    object : GetRequestStatusResponseListener {
                                        override fun onSuccessResponse(status: String?) {
                                            if (isFragmentAttached) {
    //                                    if (status.contains("success")) {
                                                progressBar?.setVisibility(View.INVISIBLE)
                                                //update the URL in shared pref
                                                SharedPref.write(SharedPref.USER_IMAGE_URL, url)
                                                Picasso.get().load(
                                                    SharedPref.read(
                                                        SharedPref.USER_IMAGE_URL,
                                                        ""
                                                    )
                                                )
                                                    .fit()
                                                    .centerCrop()
                                                    .transform(CropCircleTransformation())
                                                    .into(buyerImage, object : Callback {
                                                        override fun onSuccess() {
                                                            Log.d(
                                                                "PROFILE PIC",
                                                                "image set succesfully"
                                                            )
                                                        }

                                                        override fun onError(e: Exception) {
                                                            Log.d(
                                                                "PROFILE PIC",
                                                                "image set unsuccessful"
                                                            )
                                                        }
                                                    })
                                                //                                    }
                                            }
                                        }

                                        override fun onErrorResponse(error: VolleyErrorHelper?) {
                                            progressBar?.setVisibility(View.INVISIBLE)
                                        }
                                    })
                            } else {
                                progressBar?.setVisibility(View.INVISIBLE)
                                ZeemartBuyerApp.getToastRed(getString(R.string.txt_image_upload_fail))
                            }
                        }
                    }
                })
        }
    }

    companion object {
        private const val PICK_FROM_CAMERA = 1
        private const val PICK_FROM_GALLERY = 2
        private const val LANGUAGE_SELECTION = 69
        private const val READ_EXTERNAL_STORAGE = 3

        /**
         * Use this factory method to create a new instance
         *
         * @return A new instance of fragment MoreFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(): MoreFragment {
            val fragment = MoreFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }

        fun editUserLanguage(language: String, context: Context?) {
            val userDetails: UserDetails = SharedPref.currentUserDetail
            userDetails.language = language
            UsersApi.editSingleUser(
                context,
                userDetails,
                object : GetRequestStatusResponseListener {
                    override fun onSuccessResponse(status: String?) {
                        Log.d("LANG===>", "onSuccessResponse: $status")
                    }

                    override fun onErrorResponse(error: VolleyErrorHelper?) {
                        Log.d("LANG===>", "onErrorResponse: " + error.toString())
                    }
                })
        }
        /**
         * return the RequestBody For Notification Status String
         *
         * @param language
         * @return
         */
        //    public static String createRequestBodyForLanguageSetting(String language) {
        //        UserDetails userDetails = SharedPref.getCurrentUserDetail();
        //        userDetails.setLanguage(language);
        //        List<UserDetails> userList = new ArrayList();
        //        userList.add(userDetails);
        //        return ZeemartBuyerApp.gsonExposeExclusive.toJson(userList);
        //    }
        //
        //    private String createRequestBodyForUploadProfile(String imageUrl) {
        //
        //        UserDetails userDetails = SharedPref.getCurrentUserDetail();
        //        userDetails.setImageURL(imageUrl);
        //        List<UserDetails> userList = new ArrayList();
        //        userList.add(userDetails);
        //        return ZeemartBuyerApp.gsonExposeExclusive.toJson(userList);
        //
        ////        HashMap<String, String> userDetails = new HashMap<>();
        ////        userDetails.put("id", SharedPref.read(SharedPref.USER_ID, ""));
        ////        userDetails.put("imageURL", imageUrl);
        ////
        ////        String requestBody = ZeemartBuyerApp.gson.toJson(userDetails);
        ////        requestBody = "[" + requestBody + "]";
        ////        return requestBody;
        //    }
    }
}