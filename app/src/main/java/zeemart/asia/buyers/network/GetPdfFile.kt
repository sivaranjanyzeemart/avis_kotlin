package zeemart.asia.buyers.network

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.android.volley.*
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by ParulBhandari on 1/19/2018.
 */
class GetPdfFile     // TODO Auto-generated catch block
    (var invoiceFileURL: String, var context: Context) {
    // TODO handle the response
    val invoice: Unit
        get() {
            val request = InputStreamVolleyRequest(
                Request.Method.GET, invoiceFileURL,
                { response ->
                    // TODO handle the response
                    var bos: BufferedOutputStream? = null
                    try {
                        if (response != null) {
                            val pdfFile = createPdfFile()
                            bos = BufferedOutputStream(FileOutputStream(pdfFile))
                            bos.write(response)
                            bos.flush()
                            bos.close()
                            showPDF()
                        }
                    } catch (e: Exception) {
                        // TODO Auto-generated catch block
                        Log.d("KEY_ERROR", "UNABLE TO DOWNLOAD FILE")
                        e.printStackTrace()
                    } finally {
                        try {
                            bos?.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }, { error -> Log.d("PDF error", "error loading PDF" + error.message) }, null
            )
            VolleyRequest.Companion.getInstance(context)!!.addToRequestQueue<ByteArray>(request)
        }

    @Throws(IOException::class)
    private fun createPdfFile(): File {
        // Create an image file name
        val pdfFileName = INVOICE_ZEEMART
        val storageDir =
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val pdf = File(storageDir, pdfFileName)
        pdf.mkdir()
        return File(pdf, "$ORDER_FILE_ZEEMART.pdf")
    }

    private fun showPDF() {
        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                .toString() + "/" + INVOICE_ZEEMART + "/" + ORDER_FILE_ZEEMART + ".pdf"
        )
        Log.d("FILENAME", file.absolutePath + "*****")
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        //Uri uriPDF = Uri.fromFile(file);
        val auth = context.applicationContext.packageName + ".zeemart.asia.buyers.fileprovider"
        val uriPDF = FileProvider.getUriForFile(context.applicationContext, auth, file)
        intent.setDataAndType(uriPDF, "application/pdf")
        val chooser = Intent.createChooser(intent, "select application")

        // Verify the intent will resolve to at least one activity
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(chooser)
        } else {
            context.startActivity(intent)
        }
    }

    companion object {
        const val INVOICE_ZEEMART = "INVOICE_ZEEMART"
        const val ORDER_FILE_ZEEMART = "orderFileZeemart"
    }
}