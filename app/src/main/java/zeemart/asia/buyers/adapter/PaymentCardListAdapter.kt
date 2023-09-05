package zeemart.asia.buyers.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.models.paymentimport.PaymentCardDetails

/**
 * Created by RajPrudhviMarella on 04/03/2020.
 */
class PaymentCardListAdapter(
    private val context: Context,
    cardsList: List<PaymentCardDetails.PaymentResponse>,
    mListener: PayCardSelectedListener
) : RecyclerView.Adapter<PaymentCardListAdapter.ViewHolder?>() {
    private val lstPaymentsCards: List<PaymentCardDetails.PaymentResponse>
    private val mListener: PayCardSelectedListener

    init {
        lstPaymentsCards = cardsList
        this.mListener = mListener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v: View = LayoutInflater.from(viewGroup.getContext())
            .inflate(R.layout.lyt_pament_cards_list, viewGroup, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val cardNumber =
            context.resources.getString(R.string.txt_card_bullet) + " " + lstPaymentsCards[position].customerCardData
                ?.cardLast4Digits
        viewHolder.txtCardNumber.setText(cardNumber)
        if (lstPaymentsCards[position].customerCardData
                ?.isCardType(PaymentCardDetails.CardType.VISA) == true
        ) {
            viewHolder.imgCard.setImageResource(R.drawable.visa)
            viewHolder.txtCardName.setText(
                lstPaymentsCards[position].customerCardData?.cardType
            )
        } else if (lstPaymentsCards[position].customerCardData
                ?.isCardType(PaymentCardDetails.CardType.MASTERCARD) == true
        ) {
            viewHolder.imgCard.setImageResource(R.drawable.master_card)
            viewHolder.txtCardName.setText(
                lstPaymentsCards[position].customerCardData?.cardType
            )
        } else {
            viewHolder.imgCard.setImageResource(R.drawable.amex)
            viewHolder.txtCardName.setText("Amex")
        }
        viewHolder.imgCard.setBackgroundResource(R.drawable.grey_rounded_corner_border_square)
        viewHolder.lytRow.setOnClickListener(View.OnClickListener {
            viewHolder.imgTick.visibility = View.VISIBLE
            mListener.onPayCardItemSelected(lstPaymentsCards[position])
        })
        if (lstPaymentsCards[position].isDefaultCard) {
            viewHolder.imgTick.visibility = View.VISIBLE
        } else {
            viewHolder.imgTick.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return lstPaymentsCards.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtCardNumber: TextView
        var txtCardName: TextView
        var txtDividerLine: TextView
        var imgCard: ImageView
        var imgTick: ImageView
        var lytRow: RelativeLayout

        init {
            txtCardNumber = itemView.findViewById<TextView>(R.id.txt_card_number)
            txtCardName = itemView.findViewById<TextView>(R.id.txt_card_name)
            txtDividerLine = itemView.findViewById<TextView>(R.id.txt_divider)
            imgCard = itemView.findViewById(R.id.img_card)
            imgTick = itemView.findViewById(R.id.img_tick)
            lytRow = itemView.findViewById<RelativeLayout>(R.id.lyt_card_detail)
            ZeemartBuyerApp.setTypefaceView(
                txtCardNumber,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
            ZeemartBuyerApp.setTypefaceView(
                txtCardName,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
            )
        }
    }

    interface PayCardSelectedListener {
        fun onPayCardItemSelected(selectedCardDetails: PaymentCardDetails.PaymentResponse?)
    }
}