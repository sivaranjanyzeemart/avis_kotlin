package zeemart.asia.buyers.reports.reportsummary

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.VolleyError
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.ZeemartBuyerApp.Companion.setTypefaceView
import zeemart.asia.buyers.constants.ServiceConstant
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.DateHelper.StartDateEndDate
import zeemart.asia.buyers.helper.FileHelper
import zeemart.asia.buyers.helper.FileHelper.DownloadFileTask
import zeemart.asia.buyers.helper.FileHelper.FileSavedListener
import zeemart.asia.buyers.helper.ReportApi
import zeemart.asia.buyers.helper.ReportApi.ExportCstDataListener
import zeemart.asia.buyers.models.invoice.Invoice.ExportCsvUrlData

class ExportReportCsvFileActivity : AppCompatActivity(), View.OnClickListener {
    private var txtShareReportCsvHeader: TextView? = null
    private var txtExportCsvFileSpendingRawData: TextView? = null
    private var txtExportCsvFileListInvoices: TextView? = null
    private var txtExportCsvFileSpendingSummary: TextView? = null
    private lateinit var imgExportCsvFileSpendingSummary: ImageView
    private lateinit var imgExportCsvFileListInvoices: ImageView
    private lateinit var imgExportCsvFileSpendingRawData: ImageView
    private lateinit var btnBack: ImageView
    private lateinit var lytExportCsvFileSpendingSummary: RelativeLayout
    private lateinit var lytExportCsvFileListInvoices: RelativeLayout
    private lateinit var lytExportCsvFileSpendingRawData: RelativeLayout
    private val totalSpendingData: String? = null
    private lateinit var startEndDateRange: StartDateEndDate
    private val csvToShare = 0
    private val csvToView = 1
    private var progressBar: ProgressBar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export_report_csv_file)
        txtShareReportCsvHeader = findViewById(R.id.share_report_csv_header)
        txtExportCsvFileSpendingSummary = findViewById(R.id.txt_export_csv_file_spending_summary)
        txtExportCsvFileSpendingRawData = findViewById(R.id.txt_export_csv_file_spending_raw_data)
        txtExportCsvFileListInvoices = findViewById(R.id.txt_export_csv_file_lst_invoices)
        imgExportCsvFileSpendingSummary =
            findViewById(R.id.img_export_csv_file_spending_summary_view)
        imgExportCsvFileSpendingRawData =
            findViewById(R.id.img_export_csv_file_spending_raw_data_view)
        imgExportCsvFileListInvoices = findViewById(R.id.img_export_csv_file_lst_invoices_view)
        lytExportCsvFileSpendingSummary = findViewById(R.id.lyt_export_csv_file_spending_summary)
        lytExportCsvFileSpendingRawData = findViewById(R.id.lyt_export_csv_file_spending_raw_data)
        lytExportCsvFileListInvoices = findViewById(R.id.lyt_export_csv_file_lst_invoices)
        progressBar = findViewById(R.id.progress_bar_export_csv)
        btnBack = findViewById(R.id.share_report_csv_image_button_move_back)
        btnBack.setOnClickListener(this)
        imgExportCsvFileSpendingSummary.setOnClickListener(this)
        imgExportCsvFileSpendingRawData.setOnClickListener(this)
        imgExportCsvFileListInvoices.setOnClickListener(this)
        lytExportCsvFileSpendingSummary.setOnClickListener(this)
        lytExportCsvFileSpendingRawData.setOnClickListener(this)
        lytExportCsvFileListInvoices.setOnClickListener(this)
        setTypefaceView(
            txtShareReportCsvHeader,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(
            txtExportCsvFileSpendingSummary,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(
            txtExportCsvFileSpendingRawData,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        setTypefaceView(
            txtExportCsvFileListInvoices,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey(ReportApi.START_END_DATE_RANGE)) {
                val startEndDateRangeString = bundle.getString(ReportApi.START_END_DATE_RANGE)
                startEndDateRange = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                    startEndDateRangeString,
                    StartDateEndDate::class.java
                )
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.lyt_export_csv_file_spending_summary -> {
                AnalyticsHelper.logAction(
                    this@ExportReportCsvFileActivity,
                    AnalyticsHelper.TAP_TOTAL_SPENDING_SPENDING_SUMMARY
                )
                exportToCSV(FileHelper.WRITE_SUMMARY, csvToShare)
            }
            R.id.lyt_export_csv_file_spending_raw_data -> {
                AnalyticsHelper.logAction(
                    this@ExportReportCsvFileActivity,
                    AnalyticsHelper.TAP_TOTAL_SPENDING_SPENDING_RAW_DATA
                )
                exportToCSV(FileHelper.WRITE_RAW, csvToShare)
            }
            R.id.lyt_export_csv_file_lst_invoices -> {
                AnalyticsHelper.logAction(
                    this@ExportReportCsvFileActivity,
                    AnalyticsHelper.TAP_TOTAL_SPENDING_LIST_OF_INVOICE
                )
                exportToCSV(FileHelper.WRITE_INVOICE, csvToShare)
            }
            R.id.img_export_csv_file_spending_summary_view -> {
                AnalyticsHelper.logAction(
                    this@ExportReportCsvFileActivity,
                    AnalyticsHelper.TAP_TOTAL_SPENDING_SPENDING_SUMMARY
                )
                exportToCSV(FileHelper.WRITE_SUMMARY, csvToView)
            }
            R.id.img_export_csv_file_spending_raw_data_view -> {
                AnalyticsHelper.logAction(
                    this@ExportReportCsvFileActivity,
                    AnalyticsHelper.TAP_TOTAL_SPENDING_SPENDING_RAW_DATA
                )
                exportToCSV(FileHelper.WRITE_RAW, csvToView)
            }
            R.id.img_export_csv_file_lst_invoices_view -> {
                AnalyticsHelper.logAction(
                    this@ExportReportCsvFileActivity,
                    AnalyticsHelper.TAP_TOTAL_SPENDING_LIST_OF_INVOICE
                )
                exportToCSV(FileHelper.WRITE_INVOICE, csvToView)
            }
            R.id.share_report_csv_image_button_move_back -> finish()
            else -> {}
        }
    }

    private fun exportToCSV(exportType: Int, source: Int) {
        if (ContextCompat.checkSelfPermission(
                this@ExportReportCsvFileActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            // Request for permission
            ActivityCompat.requestPermissions(
                this@ExportReportCsvFileActivity,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                exportType
            )
        } else {
            try {
                when (exportType) {
                    FileHelper.WRITE_RAW -> {
                        //display the progress bar
                        progressBar!!.visibility = View.VISIBLE
                        ReportApi.getCSVRawOrListOfInvoicesUrl(
                            this,
                            startEndDateRange,
                            true,
                            object : ExportCstDataListener {
                                override fun onExportCsvDataResponseSuccess(reportResponseData: ExportCsvUrlData?) {
                                    //hide progress bar
                                    val filename = reportResponseData?.data?.fileName
                                    val fileUrl = reportResponseData?.data?.fileUrl
                                    //save file and get Uri to share or display
                                    DownloadFileTask(
                                        this@ExportReportCsvFileActivity,
                                        filename!!,
                                        fileUrl!!,
                                        object : FileSavedListener {
                                            override fun getFileUri(uri: Uri?) {
                                                progressBar!!.visibility = View.INVISIBLE
                                                if (source == csvToShare) {
                                                    val sharingIntent = Intent(Intent.ACTION_SEND)
                                                    sharingIntent.type =
                                                        ServiceConstant.FILE_TYPE_EXCEL
                                                    sharingIntent.putExtra(Intent.EXTRA_STREAM, uri)
                                                    startActivity(
                                                        Intent.createChooser(
                                                            sharingIntent, resources.getText(
                                                                R.string.txt_send_to
                                                            )
                                                        )
                                                    )
                                                } else {
                                                    val viewingIntent = Intent()
                                                    viewingIntent.action = Intent.ACTION_VIEW
                                                    viewingIntent.flags =
                                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                    viewingIntent.setDataAndType(
                                                        uri,
                                                        ServiceConstant.FILE_TYPE_EXCEL
                                                    )
                                                    startActivity(
                                                        Intent.createChooser(
                                                            viewingIntent, resources.getText(
                                                                R.string.contact_supplier_choose_title
                                                            )
                                                        )
                                                    )
                                                }
                                            }

                                            override fun fileSaveError(message: String?) {
                                                progressBar!!.visibility = View.INVISIBLE
                                            }
                                        }).execute()
                                }

                                override fun onExportCsvDataResponseError(error: VolleyError?) {
                                    //hide progress bar
                                    progressBar!!.visibility = View.INVISIBLE
                                }
                            })
                    }
                    FileHelper.WRITE_INVOICE -> {
                        //display the progress bar
                        progressBar!!.visibility = View.VISIBLE
                        ReportApi.getCSVRawOrListOfInvoicesUrl(
                            this,
                            startEndDateRange,
                            false,
                            object : ExportCstDataListener {
                                override fun onExportCsvDataResponseSuccess(reportResponseData: ExportCsvUrlData?) {
                                    val filename = reportResponseData?.data?.fileName
                                    val fileUrl = reportResponseData?.data?.fileUrl
                                    //save file and get Uri to share or display
                                    DownloadFileTask(
                                        this@ExportReportCsvFileActivity,
                                        filename!!,
                                        fileUrl!!,
                                        object : FileSavedListener {
                                            override fun getFileUri(uri: Uri?) {
                                                progressBar!!.visibility = View.INVISIBLE
                                                if (source == csvToShare) {
                                                    val sharingIntent = Intent(Intent.ACTION_SEND)
                                                    sharingIntent.type =
                                                        ServiceConstant.FILE_TYPE_EXCEL
                                                    sharingIntent.putExtra(Intent.EXTRA_STREAM, uri)
                                                    startActivity(
                                                        Intent.createChooser(
                                                            sharingIntent, resources.getText(
                                                                R.string.txt_send_to
                                                            )
                                                        )
                                                    )
                                                } else {
                                                    val viewingIntent = Intent()
                                                    viewingIntent.action = Intent.ACTION_VIEW
                                                    viewingIntent.flags =
                                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                    viewingIntent.setDataAndType(
                                                        uri,
                                                        ServiceConstant.FILE_TYPE_EXCEL
                                                    )
                                                    startActivity(
                                                        Intent.createChooser(
                                                            viewingIntent, resources.getText(
                                                                R.string.contact_supplier_choose_title
                                                            )
                                                        )
                                                    )
                                                }
                                            }

                                            override fun fileSaveError(message: String?) {
                                                progressBar!!.visibility = View.INVISIBLE
                                            }
                                        }).execute()
                                }

                                override fun onExportCsvDataResponseError(error: VolleyError?) {
                                    //hide progress bar
                                    progressBar!!.visibility = View.INVISIBLE
                                }
                            })
                    }
                    FileHelper.WRITE_SUMMARY -> {

                        //display the progress bar
                        progressBar!!.visibility = View.VISIBLE
                        ReportApi.summaryDataForInvoicesExcelDownload(
                            this,
                            startEndDateRange,
                            object : ExportCstDataListener {
                                override fun onExportCsvDataResponseSuccess(reportResponseData: ExportCsvUrlData?) {
                                    //hide progress bar
                                    val filename = reportResponseData?.data?.fileName
                                    val fileUrl = reportResponseData?.data?.fileUrl
                                    //save file and get Uri to share or display
                                    DownloadFileTask(
                                        this@ExportReportCsvFileActivity,
                                        filename!!,
                                        fileUrl!!,
                                        object : FileSavedListener {
                                            override fun getFileUri(uri: Uri?) {
                                                progressBar!!.visibility = View.INVISIBLE
                                                if (source == csvToShare) {
                                                    val sharingIntent = Intent(Intent.ACTION_SEND)
                                                    sharingIntent.type =
                                                        ServiceConstant.FILE_TYPE_EXCEL
                                                    sharingIntent.putExtra(Intent.EXTRA_STREAM, uri)
                                                    startActivity(
                                                        Intent.createChooser(
                                                            sharingIntent, resources.getText(
                                                                R.string.txt_send_to
                                                            )
                                                        )
                                                    )
                                                } else {
                                                    val viewingIntent = Intent()
                                                    viewingIntent.action = Intent.ACTION_VIEW
                                                    viewingIntent.flags =
                                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                    viewingIntent.setDataAndType(
                                                        uri,
                                                        ServiceConstant.FILE_TYPE_EXCEL
                                                    )
                                                    startActivity(
                                                        Intent.createChooser(
                                                            viewingIntent, resources.getText(
                                                                R.string.contact_supplier_choose_title
                                                            )
                                                        )
                                                    )
                                                }
                                            }

                                            override fun fileSaveError(message: String?) {
                                                progressBar!!.visibility = View.INVISIBLE
                                            }
                                        }).execute()
                                }

                                override fun onExportCsvDataResponseError(error: VolleyError?) {
                                    //hide progress bar
                                    progressBar!!.visibility = View.INVISIBLE
                                }
                            })
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            FileHelper.WRITE_RAW, FileHelper.WRITE_SUMMARY, FileHelper.WRITE_INVOICE -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    exportToCSV(requestCode, csvToShare)
                } else {
                    // permission denied, boo! Disable the
                }
            }
            else -> {}
        }
    }
}