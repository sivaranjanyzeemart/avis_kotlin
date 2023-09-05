package zeemart.asia.buyers.adapter

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import zeemart.asia.buyers.R
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.AnalyticsHelper
import zeemart.asia.buyers.helper.ProductDataHelper
import zeemart.asia.buyers.helper.ProductQuantityChangeDialogHelper
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import zeemart.asia.buyers.helperimportimport.CustomChangeQuantityDialog
import zeemart.asia.buyers.interfaces.ReviewOrderItemChangeListener
import zeemart.asia.buyers.models.PriceDetails
import zeemart.asia.buyers.models.ProductPriceList
import zeemart.asia.buyers.models.UnitSizeModel
import zeemart.asia.buyers.models.marketlist.DetailSupplierDataModel
import zeemart.asia.buyers.models.marketlist.Inventory
import zeemart.asia.buyers.models.marketlist.Product
import zeemart.asia.buyers.models.marketlist.ProductDetailBySupplier
import zeemart.asia.buyers.models.orderimportimport.ErrorModel
import zeemart.asia.buyers.orders.createorders.ReviewOrderActivity

/**
 * Created by ParulBhandari on 12/15/2017.
 */
class ReviewOrderProductsListAdapter(
    private val context: Context,
    listener: ReviewOrderItemChangeListener,
    private val products: MutableList<Product>,
    lstProductListSupplier: Map<String?, ProductDetailBySupplier>,
    supplierDetail: DetailSupplierDataModel,
    errorModelList: List<ErrorModel>?
) : RecyclerView.Adapter<ReviewOrderProductsListAdapter.ViewHolder?>(),
    ProductQuantityChangeDialogHelper.ProductDataChangeListener {
    private val lstProductListSupplier: Map<String?, ProductDetailBySupplier>
    private val reviewOrderItemChangeListener: ReviewOrderItemChangeListener
    private val supplierDetails: DetailSupplierDataModel
    private var dialog: CustomChangeQuantityDialog? = null
    private val errorModelList: List<ErrorModel>?

    init {
        this.lstProductListSupplier = lstProductListSupplier
        reviewOrderItemChangeListener = listener
        supplierDetails = supplierDetail
        this.errorModelList = errorModelList
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView: View = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.layout_review_order_product_row, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentPosition = holder.absoluteAdapterPosition
        if (SharedPref.readBool(
                SharedPref.USER_INVENTORY_SETTING_STATUS,
                false
            ) == true && !StringHelper.isStringNullOrEmpty(
                products[currentPosition].customName
            )
        ) {
            holder.txtProductCustomName.setVisibility(View.VISIBLE)
            holder.txtProductCustomName.setText(products[currentPosition].productName)
            holder.txtItemName.setText(products[currentPosition].customName)
        } else {
            holder.txtItemName.setText(products[currentPosition].productName)
            holder.txtProductCustomName.setVisibility(View.GONE)
        }
        if (products[currentPosition].isProductManuallyAdded) {
            holder.txtItemPerPrice.setText(R.string.txt_manually_added_check_price_with_supplier)
            ZeemartBuyerApp.setTypefaceView(
                holder.txtItemPerPrice,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_ITALICS
            )
            holder.txtItemPerPrice.setTextColor(context.resources.getColor(R.color.black))
            holder.txtTotalPrice.setVisibility(View.GONE)
            holder.edtProductQuantity.setText(products[currentPosition].quantityDisplayValue + " ")
            holder.btnReduceQuantity.setVisibility(View.INVISIBLE)
            holder.btnIncreaseQuantity.setVisibility(View.INVISIBLE)
        } else {
            holder.btnReduceQuantity.setVisibility(View.VISIBLE)
            holder.btnIncreaseQuantity.setVisibility(View.VISIBLE)
            setItemPriceText(currentPosition, holder)
            //            if (products.get(position).quantity == null) {
//                products.get(position).quantity = (0.00);
//            }
            holder.edtProductQuantity.setBackgroundResource(R.color.faint_grey)
            holder.edtProductQuantity.setTextColor(context.resources.getColor(R.color.black))

//            try {
//                Double quantityLocal = ZeemartBuyerApp.getPriceDouble(products.get(position).quantity, 2);
//                products.get(position).quantity = (quantityLocal);
//            }catch (NumberFormatException e) {
//                Log.d("", context.getString(R.string.txt_enter_valid_quantity));
//            }
            val measure: String? =
                UnitSizeModel.returnShortNameValueUnitSize(products[position].unitSize)
            //            if (UnitSizeModel.isDecimalAllowed(products.get(position).getUnitSize())) {
//                holder.edtProductQuantity.setText(ZeemartBuyerApp.getTwoDecimalQuantity(products.get(position).getQuantity()) + " ");
//            } else {
//                int intQuantity = new Double(ZeemartBuyerApp.getTwoDecimalQuantity(products.get(position).getQuantity())).intValue();
//                holder.edtProductQuantity.setText(intQuantity + " ");
//            }
            holder.edtProductQuantity.setText(products[currentPosition].quantityDisplayValue + " ")
            holder.edtProductQuantity.append(
                changeTextFontSize(
                    measure!!,
                    products[currentPosition].quantity
                )
            )
            if (products[currentPosition].totalPrice == null) {
                calculateTotalPriceQuantityChanged(currentPosition)
                setTotalPriceText(currentPosition, holder)
            } else {
                setTotalPriceText(currentPosition, holder)
            }
            if (lstProductListSupplier.containsKey(products[currentPosition].sku)) {
                //Set the MOQ for each Product
                val productPriceList: List<ProductPriceList> =
                    lstProductListSupplier[products[currentPosition].sku]?.priceList!!
                if (productPriceList != null) {
                    for (i in productPriceList.indices) {
                        if (productPriceList[i].unitSize == products[currentPosition].unitSize) {
                            if (productPriceList[i].moq != null) {
                                val moq: Double = productPriceList[i].moq!!
                                products[currentPosition].moq = (moq)
                            } else {
                                products[currentPosition].moq = (0.0)
                            }
                        }
                    }
                }
            }
            holder.btnIncreaseQuantity.setOnClickListener(View.OnClickListener {
                val quantity = products[currentPosition].quantity + 1
                products[currentPosition].quantity = (quantity)
                incDecQuantityClick(currentPosition, holder, quantity)
            })
            holder.btnReduceQuantity.setOnClickListener(View.OnClickListener {
                var quantity = products[currentPosition].quantity - 1
                if (quantity < 0) {
                    quantity = 0.0
                }
                products[currentPosition].quantity = (quantity)
                incDecQuantityClick(currentPosition, holder, quantity)
            })
            //check for the quantity
            if (products[currentPosition].quantity == 0.0) {
                holder.btnDelete.setImageResource(R.drawable.delete_blue)
                holder.btnReduceQuantity.setVisibility(View.INVISIBLE)
                holder.edtProductQuantity.setTextColor(context.resources.getColor(R.color.pinky_red))

                //display quantity cannot be 0 message
                val message = context.getString(R.string.txt_bold_quantity_cannot_be_0)
                val quantityProduct = products[currentPosition].quantityDisplayValue
                displayErrorMessages(holder, message, currentPosition, false)
            } else if (products[currentPosition].quantity < products[currentPosition].moq!!) {
                holder.btnDelete.setImageResource(R.drawable.trash_grey)
                holder.btnReduceQuantity.setVisibility(View.VISIBLE)
                //used to display the moq
                val moqVal = products[currentPosition].moqDisplayValue
                val txtStockMessageText = String.format(
                    context.getString(R.string.format_moq_is),
                    moqVal,
                    UnitSizeModel.returnShortNameValueUnitSize(
                        products[currentPosition].unitSize
                    )
                )
                val quantityProduct = products[currentPosition].quantityDisplayValue
                displayErrorMessages(holder, txtStockMessageText, currentPosition, false)
            } else {
                holder.btnDelete.setImageResource(R.drawable.trash_grey)
                holder.btnReduceQuantity.setVisibility(View.VISIBLE)
                holder.btnReduceQuantity.setVisibility(View.VISIBLE)
                //hide MOQ message
                val quantityProduct = products[currentPosition].quantityDisplayValue
                hideErrorMessages(
                    holder,
                    quantityProduct,
                    products[currentPosition].quantity,
                    products[currentPosition].unitSize,
                    currentPosition
                )
            }

            //setting for the stocks for products
            setUIMessagesIfForStockConditions(holder, currentPosition)
            setUIFont(holder)
        }
        holder.btnDelete.setOnClickListener(View.OnClickListener {
            AnalyticsHelper.logAction(context, AnalyticsHelper.TAP_ORDER_REVIEW_DELETE_PRODUCT)
            val builder = AlertDialog.Builder(
                context
            )
            builder.setPositiveButton(
                context.getString(R.string.txt_yes),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, which: Int) {
                        dialog.dismiss()
                        if (!products[currentPosition].isProductManuallyAdded) {
                            if (lstProductListSupplier.containsKey(products[currentPosition].sku)) {
                                val priceListProduct: List<ProductPriceList> =
                                    lstProductListSupplier[products[currentPosition].sku]?.priceList!!
                                if (priceListProduct != null) {
                                    for (i in priceListProduct.indices) {
                                        priceListProduct[i].selected = false
                                    }
                                }
                            }
                        }
                        products.removeAt(currentPosition)
                        notifyDataSetChanged()
                        //push the data to the review order activity
                        reviewOrderItemChangeListener.onItemQuantityChanged(products)
                    }
                })
            builder.setNegativeButton(
                context.getString(R.string.dialog_cancel_button_text),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, which: Int) {
                        dialog.dismiss()
                    }
                })
            val dialog = builder.create()
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setTitle(context.getString(R.string.txt_delete_product))
            dialog.setMessage(context.getString(R.string.txt_delete_the_product))
            dialog.show()
        })

        //call the changeQuantity dialog
        holder.edtProductQuantity.setOnClickListener(View.OnClickListener {
            if (!products[currentPosition].isProductManuallyAdded) {
                if (dialog == null || dialog != null && !dialog!!.isShowing()) {
                    val createOrderHelperDialog = ProductQuantityChangeDialogHelper(
                        products[currentPosition],
                        context,
                        supplierDetails,
                        lstProductListSupplier[products[currentPosition].sku]!!,
                        this@ReviewOrderProductsListAdapter
                    )
                    dialog = createOrderHelperDialog.createChangeQuantityDialog()
                    dialog!!.getWindow()
                        ?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                }
            }
        })
        holder.imgNotes.setOnClickListener(View.OnClickListener { createAddNoteDialog(products[currentPosition]) })
        holder.txtAddNotes.setOnClickListener(View.OnClickListener {
            AnalyticsHelper.logAction(context, AnalyticsHelper.TAP_ORDER_REVIEW_ADD_NOTES)
            createAddNoteDialog(products[currentPosition])
        })
        if (StringHelper.isStringNullOrEmpty(products[currentPosition].notes)) {
            holder.imgNotes.setImageResource(R.drawable.notesgrey)
            holder.txtAddNotes.setText(context.getString(R.string.txt_add_notes))
            holder.txtAddNotes.setTextColor(context.resources.getColor(R.color.grey_medium))
        } else {
            holder.imgNotes.setImageResource(R.drawable.notesblue)
            holder.txtAddNotes.setText(context.getString(R.string.notes))
            holder.txtAddNotes.setTextColor(context.resources.getColor(R.color.text_blue))
        }
        if (errorModelList != null) for (i in errorModelList.indices) {
            if (errorModelList[i].data != null && errorModelList[i].data?.sku.equals(
                    products[currentPosition].sku
                )
            ) {
                displayErrorMessages(holder, errorModelList[i].message!!, currentPosition, true)
            }
        }
    }

    private fun incDecQuantityClick(position: Int, holder: ViewHolder, quantity: Double) {
        if (quantity == 0.0) {
            holder.btnDelete.setImageResource(R.drawable.delete_blue)
            holder.btnReduceQuantity.setVisibility(View.INVISIBLE)
            //display quantity cannot be 0 message
            val message = context.getString(R.string.txt_bold_quantity_cannot_be_0)
            val quantityProduct = products[position].quantityDisplayValue
            displayErrorMessages(holder, message, position, false)
        } else if (quantity < products[position].moq!!) {
            holder.btnReduceQuantity.setVisibility(View.VISIBLE)
            holder.btnDelete.setImageResource(R.drawable.trash_grey)
            //used to display the moq
            val moqVal = products[position].moqDisplayValue
            val txtStockMessageText = String.format(
                context.getString(R.string.format_moq_is),
                moqVal,
                UnitSizeModel.returnShortNameValueUnitSize(
                    products[position].unitSize
                )
            )
            val quantityProduct = products[position].quantityDisplayValue
            displayErrorMessages(holder, txtStockMessageText, position, false)
        } else {
            holder.btnDelete.setImageResource(R.drawable.trash_grey)
            holder.btnReduceQuantity.setVisibility(View.VISIBLE)
            //hide MOQ message
            val quantityProduct = products[position].quantityDisplayValue
            hideErrorMessages(
                holder,
                quantityProduct,
                products[position].quantity,
                products[position].unitSize,
                position
            )
        }
        calculateTotalPriceQuantityChanged(position)
        holder.txtTotalPrice.setText(products[position].totalPrice!!.displayValue)
        //push the data to the review order activity
        reviewOrderItemChangeListener.onItemQuantityChanged(products)
        //check for stock requirement
        setUIMessagesIfForStockConditions(holder, position)
    }

    private fun setTotalPriceText(position: Int, holder: ViewHolder) {
        if (!StringHelper.isStringNullOrEmpty(products[position].totalPrice!!.displayValue)) {
            holder.txtTotalPrice.setVisibility(View.VISIBLE)
            holder.txtTotalPrice.setText(products[position].totalPrice!!.displayValue)
        } else {
            holder.txtTotalPrice.setVisibility(View.GONE)
        }
    }

    private fun setItemPriceText(position: Int, holder: ViewHolder) {
        val itemPrice = products[position].unitPrice?.getDisplayValueWithUomAndBullet(
            UnitSizeModel.returnShortNameValueUnitSize(
                products[position].unitSize
            )
        )
        if (!StringHelper.isStringNullOrEmpty(itemPrice)) {
            holder.txtItemPerPrice.setVisibility(View.VISIBLE)
            holder.txtItemPerPrice.text = (itemPrice)
        } else {
            holder.txtItemPerPrice.setVisibility(View.GONE)
        }
    }

    /**
     * calculates the total price by multiplying unitPrice and quantity
     * round up the totalprice if in decimal
     * save the rounded cent value to the total Price object
     *
     * @param position
     */
    private fun calculateTotalPriceQuantityChanged(position: Int) {
        val totalPrice =
            products[position].unitPrice?.amount!! * products[position].quantity
        //Create new total PriceDetail Object
        val totPrice = PriceDetails()
        totPrice.currencyCode = (products[position].unitPrice?.currencyCode)
        totPrice.amount = totalPrice
        products[position].totalPrice = totPrice
    }

    override fun getItemCount(): Int {
        return products.size
    }

    override fun onDataChange(product: Product?) {
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtItemName: TextView
        var txtItemPerPrice: TextView
        var btnReduceQuantity: ImageButton
        var btnIncreaseQuantity: ImageButton
        var txtTotalPrice: TextView
        var edtProductQuantity: TextView
        var btnDelete: ImageButton

        //TextView txtMoq;
        var imgNotes: ImageButton
        var txtAddNotes: TextView
        var lytStockMessage: LinearLayout
        var txtStockMessage: TextView
        var imgWarning: ImageView
        var txtProblematicProductIndicator: TextView
        var txtProductCustomName: TextView

        init {
            txtItemName = itemView.findViewById<TextView>(R.id.txt_product_name)
            btnDelete = itemView.findViewById<ImageButton>(R.id.btn_delete)
            txtTotalPrice = itemView.findViewById<TextView>(R.id.txt_per_price)
            btnReduceQuantity = itemView.findViewById<ImageButton>(R.id.btn_reduce_quantity)
            btnIncreaseQuantity = itemView.findViewById<ImageButton>(R.id.btn_inc_quantity)
            edtProductQuantity = itemView.findViewById<TextView>(R.id.txt_quantity_value)
            txtItemPerPrice = itemView.findViewById<TextView>(R.id.txt_price_product)
            //txtMoq = itemView.findViewById(R.id.txt_moq_text);
            imgNotes = itemView.findViewById<ImageButton>(R.id.img_add_notes)
            txtAddNotes = itemView.findViewById<TextView>(R.id.txt_add_notes)
            lytStockMessage = itemView.findViewById<LinearLayout>(R.id.lyt_stock_message)
            txtStockMessage = itemView.findViewById<TextView>(R.id.txt_stock_message)
            imgWarning = itemView.findViewById(R.id.img_warning)
            txtProblematicProductIndicator =
                itemView.findViewById<TextView>(R.id.problematic_product_indicator)
            txtProductCustomName = itemView.findViewById<TextView>(R.id.txt_item_custom_name)
            ZeemartBuyerApp.setTypefaceView(
                txtAddNotes,
                ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
            )
        }
    }

    private fun changeTextFontSize(text: String, quantity: Double): SpannableString {
        val unitSize: String = UnitSizeModel.returnShortNameValueUnitSizeForQuantity(text)!!
        val ss1 = SpannableString(unitSize)
        ss1.setSpan(AbsoluteSizeSpan(12, true), 0, ss1.length, 0)
        if (quantity == 0.0) {
            ss1.setSpan(
                ForegroundColorSpan(context.resources.getColor(R.color.pinky_red)),
                0,
                ss1.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } else {
            ss1.setSpan(
                ForegroundColorSpan(context.resources.getColor(R.color.text_blue)),
                0,
                ss1.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return ss1
    }

    private fun changeTextFontSize(text: String, isError: Boolean): SpannableString {
        val unitSize: String = UnitSizeModel.returnShortNameValueUnitSizeForQuantity(text)!!
        val ss1 = SpannableString(unitSize)
        ss1.setSpan(AbsoluteSizeSpan(12, true), 0, ss1.length, 0)
        if (isError) {
            ss1.setSpan(
                ForegroundColorSpan(context.resources.getColor(R.color.pinky_red)),
                0,
                ss1.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } else {
            ss1.setSpan(
                ForegroundColorSpan(context.resources.getColor(R.color.text_blue)),
                0,
                ss1.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return ss1
    }

    private fun setUIFont(holder: ViewHolder) {
        ZeemartBuyerApp.setTypefaceView(
            holder.edtProductQuantity,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            holder.txtItemName,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        ZeemartBuyerApp.setTypefaceView(
            holder.txtItemPerPrice,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            holder.txtTotalPrice,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        //ZeemartBuyerApp.setTypefaceView(holder.txtMoq,ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR);
        ZeemartBuyerApp.setTypefaceView(
            holder.txtStockMessage,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        ZeemartBuyerApp.setTypefaceView(
            holder.txtProductCustomName,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_ITALICS
        )
    }

    private fun createAddNoteDialog(product: Product) {
        val dialog = Dialog(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)
        dialog.window!!.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
        dialog.setContentView(R.layout.layout_add_product_notes)
        val edtNotes: EditText = dialog.findViewById<EditText>(R.id.edt_notes_product)
        ZeemartBuyerApp.setTypefaceView(
            edtNotes,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR
        )
        val txtNotesHeading: TextView = dialog.findViewById<TextView>(R.id.textView5)
        ZeemartBuyerApp.setTypefaceView(
            txtNotesHeading,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        if (product.notes != null && product.notes!!.length > 0) {
            edtNotes.setText(product.notes)
        }
        val btnDone = dialog.findViewById<Button>(R.id.btn_add_notes_done)
        ZeemartBuyerApp.setTypefaceView(
            btnDone,
            ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD
        )
        btnDone.setOnClickListener {
            product.notes = edtNotes.getText().toString()
            dialog.dismiss()
            notifyDataSetChanged()
        }
        dialog.show()
    }

    /**
     * checks for all the stock conditions and display the corresponding messages
     *
     * @param holder
     * @param position
     */
    private fun setUIMessagesIfForStockConditions(holder: ViewHolder, position: Int) {
        //checks if the supplier status is active and the allow negitive is false then only check for the stocks
        if (supplierDetails.supplier.settings != null && supplierDetails.supplier.settings!!.inventory != null
            && supplierDetails.supplier.settings!!.inventory?.status != null &&
            supplierDetails.supplier.settings!!.inventory?.isStatus(Inventory.Status.ACTIVE) == true
            && supplierDetails.supplier.settings!!.inventory?.allowNegative == false
        ) {
            if (lstProductListSupplier.containsKey(products[position].sku)) {
                val priceListwithStocksMap: Map<String?, ProductDataHelper.PriceListwithStock> =
                    ProductDataHelper.getStocksAvailableUOM(
                        lstProductListSupplier[products[position].sku]!!
                    )
                if (priceListwithStocksMap.size == 0) {
                    //display message stock unavailable()
                    val message = context.getString(R.string.txt_bold_item_unavailable)
                    val quantity = products[position].quantityDisplayValue
                    displayErrorMessages(holder, message, position, false)
                } else {
                    //stock available
                    if (priceListwithStocksMap.containsKey(products[position].unitSize)) {
                        val priceListwithStockUOM: ProductDataHelper.PriceListwithStock? =
                            priceListwithStocksMap[products[position].unitSize]
                        if (products[position].quantity == 0.0) {
                            //display the message that the quantity selected cannot be 0
                            val message = context.getString(R.string.txt_bold_quantity_cannot_be_0)
                            val quantity = products[position].quantityDisplayValue
                            displayErrorMessages(holder, message, position, false)
                        } else if (products[position].quantity < products[position].moq!!) {
                            val moqVal = products[position].moqDisplayValue
                            val txtStockMessageText = String.format(
                                context.getString(R.string.format_moq_is),
                                moqVal,
                                UnitSizeModel.returnShortNameValueUnitSize(
                                    products[position].unitSize
                                )
                            )
                            val quantity = products[position].quantityDisplayValue
                            displayErrorMessages(holder, txtStockMessageText, position, false)
                        } else if (products[position].quantity > priceListwithStockUOM?.stocksAmountAvailable!!) {
                            //display message not enough stock
                            val stockAvailableAmount: String =
                                UnitSizeModel.getValueDecimalAllowedCheck(
                                    products[position].unitSize,
                                    priceListwithStockUOM?.stocksAmountAvailable
                                )
                            val txtStockMessageText = String.format(
                                context.getString(R.string.format_not_enough_stock),
                                stockAvailableAmount,
                                UnitSizeModel.returnShortNameValueUnitSize(
                                    products[position].unitSize
                                )
                            )
                            val quantity = products[position].quantityDisplayValue
                            displayErrorMessages(holder, txtStockMessageText, position, false)
                        } else {
                            //do nothing
                            val quantity = products[position].quantityDisplayValue
                            hideErrorMessages(
                                holder,
                                quantity,
                                products[position].quantity,
                                products[position].unitSize,
                                position
                            )
                        }
                    } else {
                        //display message the product in current UOM is unavailable please select another UOM
                        val txtStockMessageText = String.format(
                            context.getString(R.string.format_item_unavailable),
                            UnitSizeModel.returnShortNameValueUnitSize(
                                products[position].unitSize
                            )
                        )
                        val quantity = products[position].quantityDisplayValue
                        displayErrorMessages(holder, txtStockMessageText, position, false)
                    }
                }
            } else {
                //product is no longer available in supplier list
                val message = context.getString(R.string.txt_bold_item_unavailable)
                val quantity = products[position].quantityDisplayValue
                displayErrorMessages(holder, message, position, false)
            }
        }
    }

    /**
     * hides the error messages
     *
     * @param holder
     * @param quantity
     * @param unitSize
     */
    private fun hideErrorMessages(
        holder: ViewHolder,
        quantityDisplay: String,
        quantity: Double?,
        unitSize: String,
        position: Int
    ) {
        holder.lytStockMessage.setVisibility(View.GONE)
        holder.txtProblematicProductIndicator.setVisibility(View.INVISIBLE)
        setItemPriceText(position, holder)
        setTotalPriceText(position, holder)
        holder.edtProductQuantity.setBackgroundResource(R.color.faint_grey)
        holder.edtProductQuantity.setTextColor(context.resources.getColor(R.color.black))
        holder.edtProductQuantity.setText("$quantityDisplay ")
        holder.edtProductQuantity.append(changeTextFontSize(unitSize, false))
        if (quantity != null) {
            if (quantity == 0.0) {
                holder.btnReduceQuantity.setVisibility(View.GONE)
            } else {
                holder.btnReduceQuantity.setVisibility(View.VISIBLE)
            }
        }
        products[position].isItemAvailable = true
        setPlaceOrderButtonState()
    }

    /**
     * set the place order button status
     */
    private fun setPlaceOrderButtonState() {
        var isActive = true
        for (i in products.indices) {
            if (!products[i].isItemAvailable) {
                isActive = false
            }
        }
        if (isActive) {
            //calls the review order method which checks for various supplier settings
            (context as ReviewOrderActivity).calculateTotalPrice()
        } else {
            (context as ReviewOrderActivity).calculateTotalPrice()
        }
    }

    /**
     * display the error message
     *
     * @param holder
     * @param message
     */
    private fun displayErrorMessages(
        holder: ViewHolder,
        message: String,
        position: Int,
        isForErrorMessage: Boolean
    ) {
        holder.lytStockMessage.setVisibility(View.VISIBLE)
        holder.txtProblematicProductIndicator.setVisibility(View.VISIBLE)
        holder.txtItemPerPrice.setVisibility(View.GONE)
        holder.txtTotalPrice.setVisibility(View.GONE)
        holder.txtStockMessage.setText(Html.fromHtml(message))
        holder.edtProductQuantity.setBackgroundResource(R.drawable.pinky_rect_border)
        holder.edtProductQuantity.setTextColor(context.resources.getColor(R.color.pinky_red))
        holder.edtProductQuantity.setText(products[position].quantityDisplayValue + " ")
        holder.edtProductQuantity.append(changeTextFontSize(products[position].unitSize, true))
        if (products[position].quantity != null) {
            val quantityDouble = products[position].quantity
            if (quantityDouble == 0.0) {
                holder.btnReduceQuantity.setVisibility(View.GONE)
            } else {
                holder.btnReduceQuantity.setVisibility(View.VISIBLE)
            }
        }
        if (!isForErrorMessage) {
            products[position].isItemAvailable = false
        }
        setPlaceOrderButtonState()
    }
}