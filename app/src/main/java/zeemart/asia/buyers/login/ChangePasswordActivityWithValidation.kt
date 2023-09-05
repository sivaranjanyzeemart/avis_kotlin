package zeemart.asia.buyers.login

//public class ChangePasswordActivityWithValidation extends AppCompatActivity implements GetRequestStatusResponseListener {
//    private EditText edtEnterNewPassword;
//    private EditText edtReenterPassword;
//    private RelativeLayout lytChangePassword;
//    private TextView txtPasswordValidation;
//    private TextView txtPasswordDoNotMatch;
//    private static final int PASSWORD_LENGTH = 8;
//    private long delay = 1000;
//    private long lastEnterPasswordTextEdit = 0;
//    private long lastReenterPasswordTextEdit = 0;
//    private Handler handler = new Handler();
//    private String zeemartId;
//    private String passcode;
//    private ImageButton imageButtonClose;
//    private ProgressBar progressBarChangePassword;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_change_password);
//        zeemartId = getIntent().getExtras().getString(ZeemartAppConstants.RESET_PWD_ZEEMART_ID);
//        passcode = getIntent().getExtras().getString(ZeemartAppConstants.VERIFICATION_PASSCODE);
//        imageButtonClose = (ImageButton) findViewById(R.id.imageButtonClose);
//        imageButtonClose.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent newIntent = new Intent(ChangePasswordActivityWithValidation.this, BuyerLoginActivity.class);
//                newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(newIntent);
//            }
//        });
//        progressBarChangePassword = findViewById(R.id.progressBarChangePassword);
//        edtEnterNewPassword = findViewById(R.id.edtEnterNewPassword);
//        edtReenterPassword =  findViewById(R.id.edtReEnterNewPassword);
//        txtPasswordValidation = findViewById(R.id.txtPasswordValidation);
//        txtPasswordDoNotMatch = findViewById(R.id.txtPasswordNoMatch);
//        lytChangePassword = findViewById(R.id.lytSubmitNewPassword);
//        lytChangePassword.setEnabled(false);
//
//        setFontforViews();
//
//        edtReenterPassword.setEnabled(false);
//        edtEnterNewPassword.addTextChangedListener(new EnterPasswordTextWatcher() );
//        edtReenterPassword.addTextChangedListener(new ReenterPasswordTextWatcher());
//        lytChangePassword.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //hide keyboard
//                InputMethodManager inputManager = (InputMethodManager)
//                        getSystemService(ChangePasswordActivityWithValidation.this.INPUT_METHOD_SERVICE);
//
//                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
//                        InputMethodManager.HIDE_NOT_ALWAYS);
//                progressBarChangePassword.setVisibility(View.VISIBLE);
//               new ResetNewPassword(zeemartId,passcode,edtEnterNewPassword.getText().toString(), ChangePasswordActivityWithValidation.this,ChangePasswordActivityWithValidation.this).callResetPasswordAPI();
//            }
//        });
//
//    }
//
//    /**
//     * set the font for the respective views
//     */
//    private void setFontforViews()
//    {
//        ZeemartBuyerApp.setTypefaceView(edtEnterNewPassword, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR);
//        ZeemartBuyerApp.setTypefaceView(edtReenterPassword, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR);
//        ZeemartBuyerApp.setTypefaceView(txtPasswordValidation, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD);
//        ZeemartBuyerApp.setTypefaceView(txtPasswordDoNotMatch, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD);
//        ZeemartBuyerApp.setTypefaceView(lytChangePassword, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD);
//    }
//
//    /**
//     * checks the passsword for validation rules and resturn the response message accordingly
//     * @param password
//     * @return
//     */
//    private String checkForPasswordValidationRules(String password)
//    {
//        if(!StringHelper.isStringNullOrEmpty(edtReenterPassword.getText().toString()))
//        {
//            edtReenterPassword.setText("");
//        }
//
//        if(password.length() < PASSWORD_LENGTH)
//        {
//            edtReenterPassword.setEnabled(false);
//            txtPasswordValidation.setTextColor(getResources().getColor(R.color.pinkyRed));
//            return getString(R.string.to_short_password);
//        }
//        else
//        {
//            if(!isAlphanumeric(password))
//            {
//                edtReenterPassword.setEnabled(false);
//                txtPasswordValidation.setTextColor(getResources().getColor(R.color.pinkyRed));
//                return getString(R.string.letters_and_numbers_password);
//            }
//            else
//            {
//                edtReenterPassword.setEnabled(true);
//                txtPasswordValidation.setTextColor(getResources().getColor(R.color.green));
//                return getString(R.string.type_new_pwd_again);
//            }
//        }
//
//    }
//
//    /**
//     * checks if the enter password and reenter password fields are equal
//     * @return
//     */
//    private boolean checkForPasswordEquality()
//    {
//
//            if(edtEnterNewPassword.getText().toString().equals(edtReenterPassword.getText().toString())){
//                lytChangePassword.setEnabled(true);
//                return true;
//            }
//            else
//            {
//                return false;
//            }
//
//    }
//
//    @Override
//    public void onSuccessResponse(String status) {
//        progressBarChangePassword.setVisibility(View.GONE);
//        if(!StringHelper.isStringNullOrEmpty(status) && status.equals(ServiceConstant.SUCCESS_RESPONSE))
//        {
//            createPasswordChangedAlert();
//        }
//    }
//
//    @Override
//    public void onErrorResponse(VolleyErrorHelper error) {
//        progressBarChangePassword.setVisibility(View.GONE);
//        String errorMessage = error.getErrorMessage();
//        String errorType = error.getErrorType();
//
//        if(errorType.equals(ServiceConstant.RETRY_API_CALL__500_MESSAGE))
//        {
//
//        }
//        else if(errorType.equals(ServiceConstant.STATUS_CODE_403_404_405_MESSAGE))
//        {
//            ZeemartBuyerApp.getToastRed(errorMessage);
//        }
//        else
//        {
//            ZeemartBuyerApp.getToastRed(getString(R.string.txt_request_error));
//        }
//
//    }
//
//    /**
//     * runnable to be executed after user inactivity on the enter edit text after mentioned delay
//     */
//    private Runnable enterPasswordInputChecker = new Runnable() {
//        public void run() {
//
//
//            if (System.currentTimeMillis() > (lastEnterPasswordTextEdit + delay - 500)) {
//                txtPasswordValidation.setText(checkForPasswordValidationRules(edtEnterNewPassword.getText().toString()));
//            }
//        }
//    };
//
//
//    /**
//     * text watcher for the enter password edit text,
//     * so as to listen to events when text changes in the enter edit text
//     */
//    private class EnterPasswordTextWatcher implements TextWatcher
//    {
//
//        @Override
//        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//        }
//
//        @Override
//        public void onTextChanged(CharSequence s, int start, int before, int count) {
//            //You need to remove this to run only once
//            handler.removeCallbacks(enterPasswordInputChecker);
//
//        }
//
//        @Override
//        public void afterTextChanged(Editable s) {
//            if (s.length() > 0) {
//                lastEnterPasswordTextEdit = System.currentTimeMillis();
//                handler.postDelayed(enterPasswordInputChecker, delay);
//            } else {
//                txtPasswordValidation.setTextColor(getResources().getColor(R.color.black));
//                txtPasswordValidation.setText(getResources().getString(R.string.password_validation_text));
//                txtPasswordDoNotMatch.setVisibility(View.GONE);
//                edtReenterPassword.setEnabled(false);
//                edtReenterPassword.setText("");
//                txtPasswordDoNotMatch.setVisibility(View.GONE);
//            }
//
//        }
//    }
//
//    /**
//     * runnable to be executed after user inactivity on the re enter edit text after mentioned delay
//     */
//    private Runnable reenterPasswordInputChecker = new Runnable() {
//        public void run() {
//
//            if (System.currentTimeMillis() > (lastReenterPasswordTextEdit + delay - 500)) {
//                if(checkForPasswordEquality())
//                {
//                    txtPasswordDoNotMatch.setVisibility(View.GONE);
//                    lytChangePassword.setEnabled(true);
//                }
//                else
//                {
//                    txtPasswordDoNotMatch.setVisibility(View.VISIBLE);
//                }
//
//            }
//        }
//    };
//
//    /**
//     * text watcher for the Re-enter password edit text,
//     * so as to listen to events when text changes in the reenter edit text
//     */
//    private class ReenterPasswordTextWatcher implements TextWatcher
//    {
//
//        @Override
//        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//        }
//
//        @Override
//        public void onTextChanged(CharSequence s, int start, int before, int count) {
//            //You need to remove this to run only once
//            handler.removeCallbacks(reenterPasswordInputChecker);
//
//        }
//
//        @Override
//        public void afterTextChanged(Editable s) {
//            if (s.length() > 0) {
//                lastReenterPasswordTextEdit = System.currentTimeMillis();
//                handler.postDelayed(reenterPasswordInputChecker, delay);
//            } else {
//                txtPasswordDoNotMatch.setVisibility(View.GONE);
//            }
//        }
//    }
//
//    /**
//     * checks if the string contains both alphabets and numbers
//     * @param password
//     * @return
//     */
//    private boolean isAlphanumeric(String password)
//    {
//        Pattern p = Pattern.compile("(([A-Z].*[0-9])|([0-9].*[A-Z])|([0-9].*[a-z])|([a-z].*[0-9]))");
//        Matcher m = p.matcher(password);
//        boolean b = m.find();
//        return b;
//    }
//
//    /**
//     * Creates the Password reset Successful Alert dialog
//     */
//    private void createPasswordChangedAlert()
//    {
//        AlertDialog.Builder builder = new AlertDialog.Builder(ChangePasswordActivityWithValidation.this);
//        builder.setPositiveButton(getString(R.string.txt_login_now_button), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                Intent newIntent = new Intent(ChangePasswordActivityWithValidation.this, BuyerLoginActivity.class);
//                newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(newIntent);
//
//            }
//        });
//        AlertDialog dialog = builder.create();
//        dialog.setCancelable(false);
//        dialog.setCanceledOnTouchOutside(false);
//        dialog.setMessage(Html.fromHtml(getString(R.string.password_reset_successful)));
//        dialog.show();
//    }
//}
