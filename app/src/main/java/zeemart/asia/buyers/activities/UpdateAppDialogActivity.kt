package zeemart.asia.buyers.activities

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import zeemart.asia.buyers.R
import zeemart.asia.buyers.constants.MarketConstants
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.SharedPref

class UpdateAppDialogActivity : AppCompatActivity() {
    private lateinit var calledFrom: String
    private lateinit var updateDialog: Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_app_dialog)
        if (intent.extras != null && intent.extras!!.containsKey(ZeemartAppConstants.UPDATE_DIALOG_TYPE)) {
            calledFrom = intent.extras!!.getString(ZeemartAppConstants.UPDATE_DIALOG_TYPE).toString()
            createAlertDialogUpdate()
        }
    }

    override fun onBackPressed() {

        //do nothing just disable the back button
    }

    private fun createAlertDialogUpdate() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.txt_new_version_available))
        builder.setPositiveButton(getString(R.string.dialog_ok_button_text)) { dialog, which ->
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(MarketConstants.PLAY_STORE_URL)
            this@UpdateAppDialogActivity.startActivity(i)
            SharedPref.writeBool(ZeemartAppConstants.DISPLAY_FORCE_UPDATE_DIALOG_IS_VISIBLE, false)
            SharedPref.writeBool(ZeemartAppConstants.DISPLAY_UPDATE_DIALOG_IS_VISIBLE, false)
            dialog.dismiss()
            finish()
        }
        if (calledFrom != ZeemartAppConstants.FORCE_UPDATE) {
            builder.setNegativeButton(getString(R.string.txt_next_time)) { dialog, which ->
                SharedPref.writeBool(
                    ZeemartAppConstants.DISPLAY_FORCE_UPDATE_DIALOG_IS_VISIBLE,
                    false
                )
                SharedPref.writeBool(ZeemartAppConstants.DISPLAY_UPDATE_DIALOG_IS_VISIBLE, false)
                dialog.dismiss()
                finish()
            }
        }
        updateDialog = builder.create()
        updateDialog.setCancelable(false)
        updateDialog.setTitle(getString(R.string.txt_update_zeemart))
        updateDialog.setCanceledOnTouchOutside(false)
        updateDialog.show()
    }
}