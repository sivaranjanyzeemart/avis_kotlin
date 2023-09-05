package zeemart.asia.buyers.helper

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.*
import java.net.MalformedURLException
import java.net.URL
import java.util.*

/**
 * Created by saiful on 12/4/18.
 */
object FileHelper {
    const val WRITE_SUMMARY = 1001
    const val WRITE_RAW = 1002
    const val WRITE_INVOICE = 1003
    const val NEW_LINE = "\n"
    const val SEPARATOR = ","
    fun savePdfToDocuments(context: Context, pdfBytes: ByteArray?): Uri {
        val name = DateHelper.getDateInYearMonthDayHourMinSec(Date().time / 1000)
        val dir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + "/Zeemart")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, "$name.pdf")
        var input: InputStream? = null
        var output: BufferedOutputStream? = null
        try {
            input = ByteArrayInputStream(pdfBytes)
            output = BufferedOutputStream(FileOutputStream(file))
            val data = ByteArray(1024)
            var total: Long = 0
            var count: Int
            while (input.read(data).also { count = it } != -1) {
                total += count.toLong()
                output.write(data, 0, count)
            }
            output.flush()
            output.close()
            input.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                input?.close()
                output?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        val auth = context.applicationContext.packageName + ".zeemart.asia.buyers.fileprovider"
        return FileProvider.getUriForFile(context, auth, file)
    }

    fun getBitmapUri(context: Context, bitmap: Bitmap): Uri {
        val name = DateHelper.getDateInYearMonthDayHourMinSec(Date().time / 1000)
        val dir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + "/Zeemart")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, "$name.jpg")
        try {
            val bytes = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bytes)
            val input: InputStream = ByteArrayInputStream(bytes.toByteArray())
            val output = BufferedOutputStream(FileOutputStream(file))
            val data = ByteArray(1024)
            var total: Long = 0
            var count: Int
            while (input.read(data).also { count = it } != -1) {
                total += count.toLong()
                output.write(data, 0, count)
            }
            output.flush()
            output.close()
            input.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val auth = context.applicationContext.packageName + ".zeemart.asia.buyers.fileprovider"
        return FileProvider.getUriForFile(context, auth, file)

//        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
//        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, title, null);
//        return Uri.parse(path);
    }

    interface FileSavedListener {
        fun getFileUri(uri: Uri?)
        fun fileSaveError(message: String?)
    }

    class DownloadFileTask(
        var context: Context,
        var fileName: String,
        var fileUrl: String,
        var listener: FileSavedListener
    ) : AsyncTask<Void?, Void?, Uri?>() {
        protected override fun doInBackground(vararg voids: Void?): Uri? {
            var count: Int
            var uri: Uri? = null
            var inputStream: InputStream? = null
            var output: OutputStream? = null
            try {
                val url = URL(fileUrl)
                val connection = url.openConnection()
                connection.connect()
                inputStream = BufferedInputStream(url.openStream())
                val dir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                val file = File(dir, fileName)
                output = FileOutputStream(file)
                val data = ByteArray(1024)
                while (inputStream.read(data).also { count = it } != -1) {
                    output.write(data, 0, count)
                }
                // flushing output
                output.flush()

                // closing streams
                output.close()
                inputStream.close()
                val auth =
                    context.applicationContext.packageName + ".zeemart.asia.buyers.fileprovider"
                uri = FileProvider.getUriForFile(context, auth, file)
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    inputStream?.close()
                    output?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return uri
        }

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun onPostExecute(uri: Uri?) {
            listener.getFileUri(uri)
        }
    }
}