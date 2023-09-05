package zeemart.asia.buyers.helper

class DialogHelperNew { /* public void ShowDialog(final Context context) {
        final Dialog dl = new Dialog(context, R.style.CustomDialogForTagsTheme);
        dl.setContentView(R.layout.custom_dialog_upload_invoice_email);


        dl.getWindow().setGravity(Gravity.BOTTOM);
        dl.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        TextView txt_other_upload_options = dl.findViewById(R.id.txt_other_upload_options);
        TextView txt_upload_via_email = dl.findViewById(R.id.txt_upload_via_email);
        TextView txt_send_invoices_email = dl.findViewById(R.id.txt_send_invoices_email);
        TextView txt_email_show = dl.findViewById(R.id.txt_email_show);
        TextView txt_copy_email_address = dl.findViewById(R.id.txt_copy_email_address);
        TextView txt_other_uploads = dl.findViewById(R.id.txt_other_uploads);
        TextView txt_for_non_zm_order_uploads = dl.findViewById(R.id.txt_for_non_zm_order_uploads);
        TextView txt_copied_email = dl.findViewById(R.id.txt_copied_email);
        LinearLayout linear_email_copied = dl.findViewById(R.id.linear_email_copied);
        LinearLayout linear_other_uploads = dl.findViewById(R.id.linear_other_uploads);

        ZeemartBuyerApp.setTypefaceView(txt_other_upload_options, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_BOLD);
        ZeemartBuyerApp.setTypefaceView(txt_upload_via_email, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD);
        ZeemartBuyerApp.setTypefaceView(txt_send_invoices_email, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR);
        ZeemartBuyerApp.setTypefaceView(txt_for_non_zm_order_uploads, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR);
        ZeemartBuyerApp.setTypefaceView(txt_copied_email, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR);
        ZeemartBuyerApp.setTypefaceView(txt_other_uploads, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR);
        ZeemartBuyerApp.setTypefaceView(txt_email_show, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD);
        ZeemartBuyerApp.setTypefaceView(txt_copy_email_address, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR);


        myClipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        linear_email_copied.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = "hello";
                myClip = ClipData.newPlainText("text", text);
                txt_copied_email.setVisibility(View.VISIBLE);
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txt_copied_email.setVisibility(View.GONE);
                            }
                        });
                    }
                };
                thread.start();

            }
        });

        linear_other_uploads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, UploadInvoicesForNonZmOrders.class);
                context.startActivity(intent);
                dl.dismiss();

            }
        });

        dl.setCanceledOnTouchOutside(true);
        dl.setCancelable(true);
        dl.show();

    }*/
}