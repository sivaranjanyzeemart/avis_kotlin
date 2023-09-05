package zeemart.asia.buyers.invoices

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.StringHelper

class ReviewInvoiceAdapter(
    private val context: Context,
    isForMerge: Boolean,
    private val imageUris: List<InQueueForUploadDataModel>,
    private val mListener: OnInvoiceImageClick
) : RecyclerView.Adapter<ReviewInvoiceAdapter.ViewHolder?>() {
    private var isForMerge = false

    init {
        this.isForMerge = isForMerge
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView: View = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.layout_review_image, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.lytInvoicePdfView.setVisibility(View.GONE)
        holder.imageViewProduct.visibility = View.GONE
        Log.e(":came here", "onBindViewHolder: ")
        if (StringHelper.isStringNullOrEmpty(imageUris[position].imageFilePath)) {
            holder.lytInvoicePdfView.setVisibility(View.GONE)
            holder.imageViewNoImage.visibility = View.VISIBLE
            holder.imageViewProduct.visibility = View.GONE
            holder.txtNoImage.setVisibility(View.VISIBLE)
            holder.lytImage.setVisibility(View.GONE)
            holder.pdfMultipleIcon.visibility = View.GONE
            holder.imageMultipleIcon.visibility = View.GONE
            holder.lytImageSelect.setVisibility(View.GONE)
            holder.lytPdfSelect.setVisibility(View.GONE)
        } else {
            holder.imageViewProduct.visibility = View.GONE
            holder.lytInvoicePdfView.setVisibility(View.GONE)
            holder.imageViewNoImage.visibility = View.GONE
            holder.txtNoImage.setVisibility(View.GONE)
            holder.lytImage.setVisibility(View.GONE)
            holder.pdfMultipleIcon.visibility = View.GONE
            holder.imageMultipleIcon.visibility = View.GONE
            if (imageUris[position].isInvoiceSelectedIsImage) {
                holder.lytImage.setVisibility(View.VISIBLE)
                if (isForMerge) {
                    holder.lytImageSelect.setVisibility(View.VISIBLE)
                    if (!imageUris[position].isInvoiceSelectedForDeleteOrMerge) {
                        holder.txtImageSelected.setBackground(context.resources.getDrawable(R.drawable.white_round_circle))
                        holder.txtImageSelected.setText(" ")
                    } else {
                        holder.txtImageSelected.setBackground(context.resources.getDrawable(R.drawable.blue_round_circle))
                        holder.txtImageSelected.setTextColor(context.resources.getColor(R.color.white))
                    }
                } else {
                    holder.lytImageSelect.setVisibility(View.GONE)
                }
                if (imageUris[position].imagesCount > 1) {
                    holder.imageMultipleIcon.visibility = View.VISIBLE
                } else {
                    holder.imageMultipleIcon.visibility = View.GONE
                }
                holder.lytPdfSelect.setVisibility(View.GONE)
                holder.imageViewProduct.visibility = View.VISIBLE
                val uri = imageUris[position].imageFilePath
                val bitmap: Bitmap = BitmapFactory.decodeFile(uri)
                holder.imageViewProduct.setImageBitmap(bitmap)
            } else {
                holder.lytImage.setVisibility(View.GONE)
                if (isForMerge) {
                    holder.lytPdfSelect.setVisibility(View.VISIBLE)
                    if (!imageUris[position].isInvoiceSelectedForDeleteOrMerge) {
                        holder.txtPdfSelected.setBackground(context.resources.getDrawable(R.drawable.white_round_circle))
                        holder.txtPdfSelected.setText(" ")
                    } else {
                        holder.txtPdfSelected.setBackground(context.resources.getDrawable(R.drawable.blue_round_circle))
                        holder.txtPdfSelected.setTextColor(context.resources.getColor(R.color.white))
                    }
                } else {
                    holder.lytPdfSelect.setVisibility(View.GONE)
                }
                if (imageUris[position].imagesCount > 1) {
                    holder.pdfMultipleIcon.visibility = View.VISIBLE
                } else {
                    holder.pdfMultipleIcon.visibility = View.GONE
                }
                holder.imageViewNoImage.visibility = View.GONE
                holder.txtNoImage.setVisibility(View.GONE)
                holder.txtImageSelected.setVisibility(View.GONE)
                holder.lytImageSelect.setVisibility(View.GONE)
                holder.lytInvoicePdfView.setVisibility(View.VISIBLE)
                holder.txtInvoicePdfDisplayName.setText(imageUris[position].selectedPdfOriginalName)
                holder.txtNoPreviewForPdf.setAlpha(0.3f)
            }
        }
        holder.lytInvoicePdfView.setOnClickListener(View.OnClickListener {
            mListener.onImageClicked(
                imageUris[position]
            )
        })
        holder.imageViewProduct.setOnClickListener { mListener.onImageClicked(imageUris[position]) }
        if (imageUris[position].countOfMerged != 0) {
            holder.txtPdfSelected.setText(imageUris[position].countOfMerged.toString() + "")
            holder.txtImageSelected.setText(imageUris[position].countOfMerged.toString() + "")
        }
        holder.lytPdfSelect.setOnClickListener(View.OnClickListener {
            if (imageUris[position].isInvoiceSelectedForDeleteOrMerge) {
                holder.txtPdfSelected.setBackground(context.resources.getDrawable(R.drawable.white_round_circle))
                holder.txtPdfSelected.setText(" ")
                imageUris[position].isInvoiceSelectedForDeleteOrMerge = false
                mListener.onFilterDeselected(imageUris[position])
            } else {
                holder.txtPdfSelected.setBackground(context.resources.getDrawable(R.drawable.blue_round_circle))
                var counter = 0
                for (i in imageUris.indices) {
                    if (imageUris[i].isInvoiceSelectedForDeleteOrMerge) {
                        counter++
                    }
                }
                imageUris[position].countOfMerged = counter + 1
                holder.txtPdfSelected.setText(imageUris[position].countOfMerged.toString() + "")
                holder.txtPdfSelected.setTextColor(context.resources.getColor(R.color.white))
                imageUris[position].isInvoiceSelectedForDeleteOrMerge = true
                mListener.onFilterSelected(imageUris[position])
            }
        })
        holder.lytImageSelect.setOnClickListener(View.OnClickListener {
            if (imageUris[position].isInvoiceSelectedForDeleteOrMerge) {
                holder.txtImageSelected.setBackground(context.resources.getDrawable(R.drawable.white_round_circle))
                holder.txtImageSelected.setText(" ")
                imageUris[position].isInvoiceSelectedForDeleteOrMerge = false
                mListener.onFilterDeselected(imageUris[position])
            } else {
                holder.txtImageSelected.setBackground(context.resources.getDrawable(R.drawable.blue_round_circle))
                var counter = 0
                for (i in imageUris.indices) {
                    if (imageUris[i].isInvoiceSelectedForDeleteOrMerge) {
                        counter++
                    }
                }
                imageUris[position].countOfMerged = counter + 1
                holder.txtImageSelected.setText(imageUris[position].countOfMerged.toString() + "")
                holder.txtImageSelected.setTextColor(context.resources.getColor(R.color.white))
                imageUris[position].isInvoiceSelectedForDeleteOrMerge = true
                mListener.onFilterSelected(imageUris[position])
            }
        })
    }

    override fun getItemCount(): Int {
        return imageUris.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageViewProduct: ImageView
        var imageViewNoImage: ImageView
        var txtNoImage: TextView
        var txtInvoicePdfDisplayName: TextView
        var txtNoPreviewForPdf: TextView
        var lytInvoicePdfView: CardView
        var txtPdfSelected: TextView
        var txtImageSelected: TextView
        var lytImage: CardView
        var imageMultipleIcon: ImageView
        var pdfMultipleIcon: ImageView
        var lytPdfSelect: RelativeLayout
        var lytImageSelect: RelativeLayout

        init {
            imageViewProduct = itemView.findViewById(R.id.image_product_product_details)
            imageViewNoImage = itemView.findViewById(R.id.image_no_image)
            txtNoImage = itemView.findViewById<TextView>(R.id.txt_no_image)
            txtInvoicePdfDisplayName = itemView.findViewById<TextView>(R.id.txt_invoice_pdf_name)
            txtNoPreviewForPdf = itemView.findViewById<TextView>(R.id.txt_no_preview_for_pdf)
            lytInvoicePdfView = itemView.findViewById<CardView>(R.id.lyt_invoice_pdf_view)
            txtPdfSelected = itemView.findViewById<TextView>(R.id.txt_pdf_selected)
            txtImageSelected = itemView.findViewById<TextView>(R.id.txt_image_selected)
            lytImage = itemView.findViewById<CardView>(R.id.lyt_image)
            imageMultipleIcon = itemView.findViewById(R.id.image_multiple_image)
            pdfMultipleIcon = itemView.findViewById(R.id.image_multiple_pdf)
            lytPdfSelect = itemView.findViewById<RelativeLayout>(R.id.lyt_pdf_select)
            lytImageSelect = itemView.findViewById<RelativeLayout>(R.id.lyt_image_select)
            ZeemartBuyerApp.setTypefaceView(
                txtInvoicePdfDisplayName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
            ZeemartBuyerApp.setTypefaceView(
                txtNoPreviewForPdf,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtImageSelected,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtPdfSelected,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }
    }

    interface OnInvoiceImageClick {
        fun onImageClicked(inQueueForUploadDataModel: InQueueForUploadDataModel?)
        fun onFilterSelected(inQueueForUploadDataModel: InQueueForUploadDataModel?)
        fun onFilterDeselected(inQueueForUploadDataModel: InQueueForUploadDataModel?)
    }
}