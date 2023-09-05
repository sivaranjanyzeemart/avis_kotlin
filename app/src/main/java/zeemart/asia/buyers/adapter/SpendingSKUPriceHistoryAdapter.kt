package zeemart.asia.buyers.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.models.invoiceimport.PriceUpdateModelBaseResponse

class SpendingSKUPriceHistoryAdapter(
    private val context: Context,
    priceUpdateList1: ArrayList<PriceUpdateModelBaseResponse.PriceDetailModel>
) : RecyclerView.Adapter<SpendingSKUPriceHistoryAdapter.ViewHolder?>() {
    private var priceUpdateList1: ArrayList<PriceUpdateModelBaseResponse.PriceDetailModel> = ArrayList<PriceUpdateModelBaseResponse.PriceDetailModel>()
    private lateinit var selectedUOM: SelectedUOM

    init {
        this.priceUpdateList1 = priceUpdateList1
        selectedUOM = selectedUOM
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.getContext())
        val listItem: View =
            layoutInflater.inflate(R.layout.list_item_uom, parent, false)
        return ViewHolder(listItem)
    }

    override fun getItemCount(): Int {
       return priceUpdateList1.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txt_product_uom.setText(priceUpdateList1[position].unitSize)
    }

//    val itemCount: Int
//        get() = priceUpdateList1.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txt_product_uom: TextView

        init {
            txt_product_uom = itemView.findViewById<View>(R.id.textView) as TextView
        }
    }

    interface SelectedUOM {
        fun onSelected()
    }
}