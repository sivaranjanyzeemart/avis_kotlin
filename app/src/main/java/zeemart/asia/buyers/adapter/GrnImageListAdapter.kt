package zeemart.asia.buyers.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.invoices.InQueueForUploadDataModel
import zeemart.asia.buyers.invoices.InvoiceDataManager
import java.io.File

/**
 * Created by RajPrudhviMarella on 15/June/2021.
 */
class GrnImageListAdapter(
    private val context: Context,
    var invoiceDataList: ArrayList<InQueueForUploadDataModel>,
    private val onNewCameraClicked: onNewCameraClicked_,
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_TYPE_ADD) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.lyt_grn_add_image_item, parent, false)
            ViewHolderAdder(v)
        } else if (viewType == ITEM_TYPE_PDF) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.lyt_grn_pdf_item, parent, false)
            ViewHolderPDF(v)
        } else {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.lyt_grn_image_item, parent, false)
            ViewHolderImage(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = invoiceDataList[position]
        if (item.isNewAdded) {
            val viewHolder = holder as ViewHolderAdder
            viewHolder.lytRow.setOnClickListener { onNewCameraClicked.onNewCameraClicked() }
        } else if (!item.isInvoiceSelectedIsImage) {
            val viewHolder = holder as ViewHolderPDF
            viewHolder.lytRow.setOnClickListener {
                onNewCameraClicked.onViewImageClicked(
                    invoiceDataList[position]
                )
            }
        } else {
            val viewHolder = holder as ViewHolderImage
            val bitmap = InvoiceDataManager(context).decodeSampledBitmapFromImageUri(
                Uri.fromFile(
                    File(
                        invoiceDataList[position].imageFilePath
                    )
                ), 60, 60
            )
            viewHolder.imgGRN.setImageBitmap(bitmap)
            viewHolder.lytRow.setOnClickListener {
                onNewCameraClicked.onViewImageClicked(
                    invoiceDataList[position]
                )
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        super.getItemViewType(position)
        val item = invoiceDataList[position]
        return if (item.isNewAdded) {
            ITEM_TYPE_ADD
        } else if (!item.isInvoiceSelectedIsImage) {
            ITEM_TYPE_PDF
        } else {
            ITEM_TYPE_IMAGE
        }
    }

    override fun getItemCount(): Int {
        return invoiceDataList.size
    }

    inner class ViewHolderPDF(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgGRN: ImageView
        val lytRow: RelativeLayout

        init {
            imgGRN = itemView.findViewById(R.id.grn_image)
            lytRow = itemView.findViewById(R.id.lyt_row)
        }
    }

    class ViewHolderImage(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgGRN: ImageView
        val lytRow: RelativeLayout

        init {
            imgGRN = itemView.findViewById(R.id.grn_image)
            lytRow = itemView.findViewById(R.id.lyt_row)
        }
    }

    inner class ViewHolderAdder(v: View?) : RecyclerView.ViewHolder(
        v!!
    ) {
        private val imgGRN: ImageView
        val lytRow: RelativeLayout

        init {
            imgGRN = itemView.findViewById(R.id.grn_image)
            lytRow = itemView.findViewById(R.id.lyt_row)
        }
    }

    interface onNewCameraClicked_ {

        fun onNewCameraClicked()

        fun onViewImageClicked(inQueueForUploadDataModel: InQueueForUploadDataModel?)
    }

    companion object {
        private const val ITEM_TYPE_ADD = 0
        private const val ITEM_TYPE_PDF = 1
        private const val ITEM_TYPE_IMAGE = 2
    }
}