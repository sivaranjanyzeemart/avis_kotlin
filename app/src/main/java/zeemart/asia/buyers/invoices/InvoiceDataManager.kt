package zeemart.asia.buyers.invoices

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.helper.DateHelper
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.models.invoice.Invoice.InvoiceDateCompare
import zeemart.asia.buyers.models.invoice.InvoiceMgr
import zeemart.asia.buyers.models.invoice.InvoiceUploadsListDataMgr
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by ParulBhandari on 6/22/2018.
 */
class InvoiceDataManager     //public static final String IncoiceDirectoryName = "zeemart_invoice_directory";
    (private val context: Context) {
    private val rand = Random()

    /**
     * return the correct orientation rotated bitmap from byteArray
     *
     * @param data
     * @param reqWidth
     * @param reqHeight
     * @param rotationAngle
     * @return
     */
    fun decodeSampledBitmapFromByteArray(
        data: ByteArray,
        reqWidth: Int,
        reqHeight: Int,
        rotationAngle: Int,
    ): Bitmap {
        //write the byte Array into a temp file android
        val rotationAngleImage = getImageRotationAngle(data, rotationAngle)

        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(data, 0, data.size, options)

        // Calculate inSampleSize
        options.inSampleSize =
            calculateInSampleSize(options, reqWidth, reqHeight)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        options.inPreferredConfig = Bitmap.Config.RGB_565
        options.inDither = true
        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size, options)
        return rotateBitmap(bitmap, rotationAngleImage.toFloat())
    }

    fun getImageRotationAngle(data: ByteArray?, rotationAngle: Int): Int {
        var tempFile: File? = null
        var rotate = 0
        var fos: FileOutputStream? = null
        try {
            tempFile = File.createTempFile("cam_image", "zeemart", null)
            fos = FileOutputStream(tempFile)
            fos.write(data)
            fos.close()
            val exifInterface = ExifInterface(tempFile.absolutePath)
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )
            rotate = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_NORMAL -> 0
                else -> rotationAngle
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (fos != null) {
                try {
                    fos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return rotate
    }

    /**
     * return the rotated bitmap as per the current orientaion of the selected image from the image Uri
     *
     * @param uri
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    fun decodeSampledBitmapFromImageUri(uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        var rotationInDegrees = 0
        try {
            val exif = ExifInterface(uri.path!!)
            val rotation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            rotationInDegrees = exifToDegrees(rotation)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        return try {
            val ims = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(ims, null, options)

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            Log.d("BITMAP SZ B4 UPLOADING", options.inSampleSize.toString() + "***")
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.RGB_565
            options.inDither = true
            ims!!.close()
            val imsNew = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(imsNew, null, options)
            var rotatedBitmap: Bitmap? = null
            if (bitmap != null) {
                rotatedBitmap = rotateBitmap(bitmap, rotationInDegrees.toFloat())
            }
            rotatedBitmap
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * save the bitmap to internal storage in the specified folder
     *
     * @param bitmap
     * @param outletId
     * @return
     */
    fun saveInInternalStorage(
        bitmap: Bitmap,
        outletId: String?,
        filename: String?,
    ): InternalStorageFileDataModel {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val directoryName = timeStamp + "_" + randomString(3)
        //String imageFileName = "JPEG_" + timeStamp + "_"+randomString(2);
        val folder = context.filesDir
        val invoiceDirectory = File(folder, outletId)
        invoiceDirectory.mkdir()
        val newInvoiceDirectory = File(invoiceDirectory, directoryName)
        newInvoiceDirectory.mkdir()
        val file = File(newInvoiceDirectory, filename)
        try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.close()
        } catch (e: FileNotFoundException) {
            Log.d("", "File not found: " + e.message)
        } catch (e: IOException) {
            Log.d("", "Error accessing file: " + e.message)
        }
        val filePath = InternalStorageFileDataModel()
        filePath.directoryPath = newInvoiceDirectory.absolutePath
        filePath.filePath = file.absolutePath
        return filePath
    }

    /**
     * save the pdf file to internal storage in the specified folder
     *
     * @param uri
     * @param outletId
     * @param filename
     * @return
     */
    fun savePdfInInternalStorage(
        outletId: String?,
        filename: String?,
        uri: Uri?,
    ): InternalStorageFileDataModel {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val directoryName = timeStamp + "_" + randomString(3)
        val folder = context.filesDir
        val invoiceDirectory = File(folder, outletId)
        invoiceDirectory.mkdir()
        val newInvoiceDirectory = File(invoiceDirectory, directoryName)
        newInvoiceDirectory.mkdir()
        val file = File(newInvoiceDirectory, filename)
        val baos = ByteArrayOutputStream()
        val fis: InputStream?
        try {
            fis = context.contentResolver.openInputStream(uri!!)
            val buf = ByteArray(1024)
            var n: Int
            while (-1 != fis!!.read(buf).also { n = it }) baos.write(buf, 0, n)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val bytes = baos.toByteArray()
        var fileOutputStream: FileOutputStream? = null
        try {
            fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(bytes)
            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        val filePath = InternalStorageFileDataModel()
        filePath.directoryPath = newInvoiceDirectory.absolutePath
        filePath.filePath = file.absolutePath
        return filePath
    }

    /**
     * generate a random string of specified length
     *
     * @param len
     * @return
     */
    fun randomString(len: Int): String {
        val DATA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890"
        val sb = StringBuilder(len)
        for (i in 0 until len) {
            sb.append(DATA[rand.nextInt(DATA.length)])
        }
        return sb.toString()
    }

    /**
     * Deletes the invoice directory
     *
     * @param file
     * @return
     */
    fun deleteDirectory(file: File): Boolean {
        if (file.exists()) {
            if (file.isDirectory) {
                val files = file.listFiles()
                for (i in files.indices) {
                    if (files[i].isDirectory) {
                        deleteDirectory(files[i])
                    } else {
                        files[i].delete()
                    }
                }
            }
            Log.d("INVOICE UPLOAD", "file deleted")
            return file.delete()
        }
        return false
    }

    /**
     * save the image info file to the correct path
     *
     * @param object
     * @return
     */
    fun saveInfoFileToInternalStorage(`object`: InQueueForUploadDataModel): String? {
        /**
         * set the outlet to which the invoice belong as the invoice outlet
         * before its saved to phone internal memory
         */
        `object`.outletId = SharedPref.defaultOutlet?.outletId
        `object`.outletName = SharedPref.defaultOutlet?.outletName
        val infoFile: File
        infoFile = if (`object`.objectInfoFilePath != null) {
            File(`object`.objectInfoFilePath)
        } else {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val infoFileName = "INFO_" + timeStamp + "_" + randomString(4)
            val directoryFile = File(`object`.imageDirectoryPath)
            File(directoryFile, infoFileName)
        }
        var fos: FileOutputStream? = null
        var oos: ObjectOutputStream? = null
        try {
            fos = FileOutputStream(infoFile)
            oos = ObjectOutputStream(fos)
            oos.writeObject(ZeemartBuyerApp.gsonExposeExclusive.toJson(`object`))
            oos.close()
            fos.close()
        } catch (e: FileNotFoundException) {
            Log.e("InvoiceDataManager", "File not found: " + e.message)
            return null
        } catch (e: IOException) {
            Log.e("InvoiceDataManager", "Error accessing file: " + e.message)
            return null
        } finally {
            try {
                fos?.close()
                oos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return infoFile.absolutePath
    }

    /**
     * get all the invoices in the internal storage which were not deleted
     *
     * @param outletId
     * @return
     */
    fun getAlltheInQueueInvoiceList(outletId: String): List<InQueueForUploadDataModel> {
        val invoicesOnInternalStorage = ArrayList<InQueueForUploadDataModel>()
        val file = context.filesDir
        val invoiceImageDirectory = File(file.absolutePath + "/" + outletId)
        val fileArray = invoiceImageDirectory.listFiles()
        if (fileArray != null) {
            for (i in fileArray.indices) {
                val imageInfoFiles = fileArray[i].listFiles()
                for (j in imageInfoFiles.indices) {
                    if (imageInfoFiles[j].absolutePath.contains("INFO_")) {
                        var invoiceDataModel: InQueueForUploadDataModel
                        var fis: FileInputStream? = null
                        var ois: ObjectInputStream? = null
                        try {
                            fis = FileInputStream(imageInfoFiles[j])
                            ois = ObjectInputStream(fis)
                            invoiceDataModel = ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                                ois.readObject() as String,
                                InQueueForUploadDataModel::class.java
                            )
                            ois.close()
                            fis.close()
                            invoicesOnInternalStorage.add(invoiceDataModel)
                        } catch (e: FileNotFoundException) {
                            Log.e("InvoiceDataManager", "File not found: " + e.message)
                        } catch (e: IOException) {
                            Log.e("InvoiceDataManager", "Error accessing file: " + e.message)
                        } catch (e: ClassNotFoundException) {
                            Log.e("InvoiceDataManager", "Class not found: " + e.message)
                        } finally {
                            try {
                                fis?.close()
                                ois?.close()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }
        return invoicesOnInternalStorage
    }

    /**
     * checks if the outlet is corresponding to the selected outlet
     * so that it does not get uploaded for wrong outlet
     *
     * @param invoiceUploadsListDataMgr
     * @return
     */
    open fun checkIfInvoiceIsForSelectedOutlet(invoiceUploadsListDataMgr: InvoiceUploadsListDataMgr): Boolean {
        val selectedOutlet: String = SharedPref.defaultOutlet?.outletId!!
        if (invoiceUploadsListDataMgr.inQueueForUploadInvoice != null) {
            val directoryPathArray =
                invoiceUploadsListDataMgr.inQueueForUploadInvoice!!.imageDirectoryPath!!.split("/".toRegex())
                    .dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            for (i in directoryPathArray.indices) {
                if (directoryPathArray[i] == selectedOutlet) {
                    return true
                }
            }
        } else if (invoiceUploadsListDataMgr.inQueueForUploadMergedInvoice != null && invoiceUploadsListDataMgr.inQueueForUploadMergedInvoice!!.size > 0) {
            val directoryPathArray =
                invoiceUploadsListDataMgr.inQueueForUploadMergedInvoice!![0].imageDirectoryPath!!.split(
                    "/".toRegex()
                ).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            for (i in directoryPathArray.indices) {
                if (directoryPathArray[i] == selectedOutlet) {
                    return true
                }
            }
        }
        return false
    }

    fun createListWithHeadersData(
        superList: MutableList<InvoiceMgr>,
        invoiceList: MutableList<InvoiceMgr>,
    ): List<InvoiceMgr> {
        if (invoiceList.size > 0) {
            Collections.sort(invoiceList, InvoiceDateCompare())
            var item = InvoiceMgr()
            item.isHeader = true
            val date = DateHelper.getDateInDateMonthYearFormat(invoiceList[0].invoiceDate)
            val day = DateHelper.getDateIn3LetterDayFormat(invoiceList[0].invoiceDate)
            var headerDate: String = date
            item.headerDate = date
            item.headerDay = day
            item.invoiceDate = invoiceList[0].invoiceDate
            if (superList.size > 0) {
                val lastDataSuperList = superList[superList.size - 1]
                val lastInvoiceDate =
                    DateHelper.getDateInDateMonthYearFormat(lastDataSuperList.invoiceDate)
                if (!headerDate.equals(lastInvoiceDate, ignoreCase = true)) {
                    invoiceList.add(0, item)
                    headerDate = item.headerDate
                }
            } else {
                invoiceList.add(0, item)
            }
            for (i in invoiceList.indices) {
                item = InvoiceMgr()
                item.isHeader = true
                val dateHeader = DateHelper.getDateInDateMonthYearFormat(invoiceList[i].invoiceDate)
                val dayHeader = DateHelper.getDateIn3LetterDayFormat(invoiceList[i].invoiceDate)
                if (!headerDate.equals(dateHeader, ignoreCase = true)) {
                    headerDate = dateHeader
                    item.headerDate = dateHeader
                    item.headerDay = dayHeader
                    item.invoiceDate = invoiceList[i].invoiceDate
                    invoiceList.add(i, item)
                }
            }
        }
        superList.addAll(invoiceList)
        return superList
    }

    companion object {
        const val UPDATE_IN_QUEUE_LIST = "UPDATE_IN_QUEUE_LIST"
        const val UPDATE_UPLOADED_LIST = "UPDATE_UPLOADED_LIST"
        const val TOTAL_NO_OF_FILES_TO_BE_UPLOADED_AT_ONCE = 5
        fun calculateInSampleSize(
            options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int,
        ): Int {
            // Raw height and width of image
            val height = options.outHeight
            val width = options.outWidth
            var inSampleSize = 1
            if (height > reqHeight || width > reqWidth) {
                val halfHeight = height / 2
                val halfWidth = width / 2

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while (halfHeight / inSampleSize >= reqHeight
                    && halfWidth / inSampleSize >= reqWidth
                ) {
                    inSampleSize *= 2
                }
            }
            Log.d("B4 sample size", "$inSampleSize*********")
            return inSampleSize
        }

        fun saveCroppedBitmapToFilePath(croppedBitmap: Bitmap, imageFilePath: String?) {
            val file = File(imageFilePath)
            try {
                val fos = FileOutputStream(file)
                croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.close()
            } catch (e: FileNotFoundException) {
                Log.d("", "File not found: " + e.message)
            } catch (e: IOException) {
                Log.d("", "Error accessing file: " + e.message)
            }
        }

        /**
         * return the rotated bitmap as per the rotation angle
         *
         * @param source
         * @param angle
         * @return
         */
        fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
            val matrix = Matrix()
            matrix.postRotate(angle)
            return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        }

        /**
         * return the current rotation of and image
         *
         * @param exifOrientation
         * @return
         */
        private fun exifToDegrees(exifOrientation: Int): Int {
            if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
                return 90
            } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
                return 180
            } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
                return 270
            }
            return 0
        }
    }
}