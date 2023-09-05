package zeemart.asia.buyers.orders.createordersimport

import androidx.appcompat.app.AppCompatActivity
class ProductListNewOrderActivity : AppCompatActivity() { //
    //    private static final int RECENT_SEARCH_LIMIT = 20;
    //    private static final int TOP = 0;
    //    private RecyclerView lstProducts;
    //    private EditText edtSearch;
    //    private Button btnDone;
    //    private String supplierId;
    //    private String outletID;
    //    private List<ProductDetailBySupplier> products;
    //    private SupplierProductListAdapter productListAdapter;
    //    private List<Product> selectedProductList;
    //    private String showProductListCalledFrom;
    //    private String supplierName;
    //    private TextView txtHeadingAddToOrder, txt_select_all;
    //    private CustomLoadingViewWhite threeDotLoaderBlue;
    //    private DetailSupplierDataModel supplierDetails;
    //    private Map<String, Product> selectedProductMap = new HashMap<>();
    //    private TextView txtUpdateSelectedFilters;
    //    private List<String> filterSelectedOutletTags;
    //    private int counter;
    //    private RelativeLayout lytNoFilterResults;
    //    private TextView txtNoFilterResults;
    //    private TextView txtDeselectFilters;
    //    private int totalProductsSelected;
    //    private RelativeLayout lytRecentSearch;
    //    private TextView txtRecentSearchHeader;
    //    private TextView txtClearRecentSearch;
    //    private ListView lstRecentSearch;
    //    private List<String> recentSearchList = new ArrayList<>();
    //    private boolean isProductTagsVisible;
    //    private TextView txtSearchCancel;
    //    private final int edtTextMarginEndMax = 70;
    //    private final int edtTextMarginEndMin = 0;
    //    private String existingDraftOrderId;
    //    private List<OutletTags> outletTags =new ArrayList<>();
    //    private ImageView btnFilter;
    //    private RelativeLayout lytReviewItems;
    //
    //    @Override
    //    protected void onCreate(Bundle savedInstanceState) {
    //        super.onCreate(savedInstanceState);
    //        setContentView(R.layout.activity_product_list_new_order);
    //        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    //        Bundle bundle = getIntent().getExtras();
    //        if (bundle != null) {
    //            if (bundle.containsKey(ZeemartAppConstants.SUPPLIER_ID)) {
    //                supplierId = bundle.getString(ZeemartAppConstants.SUPPLIER_ID);
    //            }
    //            if (bundle.containsKey(ZeemartAppConstants.SELECTED_PRODUCT_LIST)) {
    //                String json = bundle.getString(ZeemartAppConstants.SELECTED_PRODUCT_LIST);
    //                Product[] products = ZeemartBuyerApp.gsonExposeExclusive.fromJson(json, Product[].class);
    //                selectedProductList = Arrays.asList(products);
    //                //create a selected product map
    //                if (selectedProductList != null && selectedProductList.size() > 0) {
    //                    selectedProductMap = ProductDataHelper.createSelectedProductMap(selectedProductList);
    //                }
    //            }
    //            if (bundle.containsKey(ZeemartAppConstants.SUPPLIER_NAME)) {
    //                supplierName = bundle.getString(ZeemartAppConstants.SUPPLIER_NAME);
    //            }
    //            if (bundle.containsKey(ZeemartAppConstants.SUPPLIER_DETAIL_INFO)) {
    //                supplierDetails = ZeemartBuyerApp.gsonExposeExclusive.fromJson(bundle.getString(ZeemartAppConstants.SUPPLIER_DETAIL_INFO), DetailSupplierDataModel.class);
    //            }
    //            if (bundle.containsKey(ZeemartAppConstants.CALLED_FROM)) {
    //                showProductListCalledFrom = bundle.getString(ZeemartAppConstants.CALLED_FROM);
    //            }
    //            if (bundle.containsKey(ZeemartAppConstants.OUTLET_ID)) {
    //                outletID = bundle.getString(ZeemartAppConstants.OUTLET_ID);
    //            }
    //            if (bundle.containsKey(ZeemartAppConstants.EXISTING_DRAFT_ID)) {
    //                existingDraftOrderId = bundle.getString(ZeemartAppConstants.EXISTING_DRAFT_ID);
    //            }
    //            if (!StringHelper.isStringNullOrEmpty(outletID) && !StringHelper.isStringNullOrEmpty(supplierId)) {
    //                MarketListApi.retrieveMarketListOutlet(this, false,outletID, supplierId, new ProductListBySupplierListner() {
    //                    @Override
    //                    public void onSuccessResponse(ProductListBySupplier productList) {
    //                        threeDotLoaderBlue.setVisibility(View.GONE);
    //                        edtSearch.setFocusable(true);
    //                        edtSearch.setFocusableInTouchMode(true);
    //                        if (productList != null && productList.getProducts().size() > 0) {
    //                            products = new ArrayList<>();
    //                            for (int i = 0; i < productList.getProducts().size(); i++) {
    //                                if (productList.getProducts().get(i).getPriceList().size() > 1) {
    //                                    for (int j = 0; j < productList.getProducts().get(i).getPriceList().size(); j++) {
    //                                        if (productList.getProducts().get(i).getPriceList().get(j).isStatus(ProductPriceList.UnitSizeStatus.VISIBLE)) {
    //                                            ProductDetailBySupplier productUom = ZeemartBuyerApp.fromJson(ZeemartBuyerApp.gsonExposeExclusive.toJson(productList.getProducts().get(i)),ProductDetailBySupplier.class);
    //                                            List<ProductPriceList> productPriceLists = new ArrayList<>();
    //                                            productPriceLists.add(productList.getProducts().get(i).getPriceList().get(j));
    //                                            productUom.setPriceList(productPriceLists);
    //                                            productUom.setHasMultipleUom(true);
    //                                            products.add(productUom);
    //                                        }
    //                                    }
    //                                } else {
    //                                    products.add(productList.getProducts().get(i));
    //                                }
    //                            }
    //                            //products = productList.getProducts();
    //                            //get the fav on top + sorted product list
    //                            products = ProductDataHelper.getDisplayProductList(ProductListNewOrderActivity.this, products, outletID, supplierId);
    //                            String supplierJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(supplierDetails.getSupplier());
    //                            setSupplier(supplierJson);
    //                            if (selectedProductList != null && selectedProductList.size() > 0) {
    //                                for (int i = 0; i < selectedProductList.size(); i++) {
    //                                    for (int j = 0; j < products.size(); j++) {
    //                                        if (products.get(j).getSku().equals(selectedProductList.get(i).getSku())
    //                                                && products.get(j).getPriceList().get(0).getUnitSize().equals(selectedProductList.get(i).getUnitSize())) {
    //                                            products.get(j).setSelected(true);
    //                                            totalProductsSelected = totalProductsSelected + 1;
    //                                            updateDoneButton(totalProductsSelected);
    //                                            break;
    //                                        }
    //                                    }
    //                                }
    //                            }
    //                            updateProductsAdapter();
    //
    //                            Set<String> uniqueFilterCategorySet = new HashSet<String>();
    //                            if (products != null && products.size() > 0) {
    //                                for (int i = 0; i < products.size(); i++) {
    //                                    if (products.get(i).getOutletTags() != null) {
    //                                        for (String tag : products.get(i).getOutletTags()) {
    //                                            if (!StringHelper.isStringNullOrEmpty(tag))
    //                                                uniqueFilterCategorySet.add(tag);
    //                                        }
    //
    //                                    }
    //                                }
    //                            }
    //
    //                            if (uniqueFilterCategorySet != null && uniqueFilterCategorySet.size() > 0) {
    //                                ArrayList<OutletTags> outletTagsList = new ArrayList<>();
    //                                for (String tag : uniqueFilterCategorySet) {
    //                                    OutletTags categoryObj = new OutletTags();
    //                                    categoryObj.setCategoryName(tag);
    //                                    categoryObj.setCategorySelected(false);
    //                                    outletTagsList.add(categoryObj);
    //                                }
    //                                Collections.sort(outletTagsList, new Comparator<OutletTags>() {
    //                                    @Override
    //                                    public int compare(OutletTags o1, OutletTags o2) {
    //                                        return o1.getCategoryName().compareTo(o2.getCategoryName());
    //                                    }
    //                                });
    //                                isProductTagsVisible = true;
    //                                outletTags =outletTagsList;
    //                                setProductTagsVisibility();
    //                            } else {
    //                                isProductTagsVisible = false;
    //                                setProductTagsVisibility();
    //                            }
    //                        }
    //                    }
    //
    //                    @Override
    //                    public void onErrorResponse(VolleyErrorHelper error) {
    //                        threeDotLoaderBlue.setVisibility(View.GONE);
    //                        edtSearch.setFocusable(true);
    //                        edtSearch.setFocusableInTouchMode(true);
    //                        if (error.getErrorType() != null && error.getErrorMessage() != null) {
    //                            if (error.getErrorType().equals(ServiceConstant.STATUS_CODE_403_404_405_MESSAGE)) {
    //                                ZeemartBuyerApp.getToastRed(error.getErrorMessage());
    //                            }
    //                        }
    //                    }
    //                });
    ////                new GetProductList(this,this,outletID).retrieveProductListData(supplierId);
    //            }
    //        }
    //        lytReviewItems=findViewById(R.id.lyt_review_items);
    //        txtSearchCancel = findViewById(R.id.search_btn_cancel);
    //        txtSearchCancel.setVisibility(View.GONE);
    //        lytRecentSearch = findViewById(R.id.lyt_recent_search);
    //        lytRecentSearch.setVisibility(View.GONE);
    //        lstRecentSearch = findViewById(R.id.lst_recent_search);
    //        lstRecentSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
    //            @Override
    //            public void onItemClick(AdapterView<?> arg0, View arg1,
    //                                    int position, long id) {
    //                String name = recentSearchList.get(position);
    //                if (name != null) {
    //                    edtSearch.setText(name);
    //                    edtSearch.setSelection(edtSearch.getText().length());
    //                }
    //            }
    //        });
    //        txtRecentSearchHeader = findViewById(R.id.txt_recent_search);
    //        txtClearRecentSearch = findViewById(R.id.txt_recent_search_clear);
    //        txtClearRecentSearch.setOnClickListener(new View.OnClickListener() {
    //            @Override
    //            public void onClick(View view) {
    //                recentSearchList.clear();
    //                setRecentSearchListAdapter(recentSearchList);
    //                setRecentSearchListVisibility();
    //                SharedPref.removeString(supplierId);
    //            }
    //        });
    //
    //        String jsonText = SharedPref.read(supplierId, "");
    //        String[] lstRecentSearch = ZeemartBuyerApp.gsonExposeExclusive.fromJson(jsonText, String[].class);
    //        if (lstRecentSearch != null)
    //            recentSearchList = new ArrayList<>(Arrays.asList(lstRecentSearch));
    //        setRecentSearchListAdapter(recentSearchList);
    //        threeDotLoaderBlue = findViewById(R.id.spin_kit_loader_product_list);
    //        threeDotLoaderBlue.setVisibility(View.VISIBLE);
    //        lstProducts = findViewById(R.id.lstProductList);
    //        lstProducts.setLayoutManager(new LinearLayoutManager(this));
    //        txtHeadingAddToOrder = findViewById(R.id.txt_product_list_heading);
    //        edtSearch = findViewById(R.id.edit_search);
    //        edtSearch.setHint(getResources().getString(R.string.content_product_list_new_order_edt_search_hint));
    //        edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
    //            @Override
    //            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
    //                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
    //                    String searchText = edtSearch.getText().toString();
    //                    if (!StringHelper.isStringNullOrEmpty(searchText)) {
    //                        setRecentSearchList(searchText);
    //                    }
    //                    return true;
    //                }
    //                return false;
    //            }
    //        });
    //        edtSearch.setCursorVisible(false);
    //        edtSearch.setOnClickListener(new View.OnClickListener() {
    //            @Override
    //            public void onClick(View view) {
    //                edtSearch.setCursorVisible(true);
    //                setSearchBarMargin(edtTextMarginEndMax);
    //                txtSearchCancel.setVisibility(View.VISIBLE);
    //                setRecentSearchListVisibility();
    //            }
    //        });
    //        //added to track search event
    //        edtSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
    //            @Override
    //            public void onFocusChange(View v, boolean hasFocus) {
    //                AnalyticsHelper.logAction(ProductListNewOrderActivity.this, AnalyticsHelper.TAP_ADD_TO_ORDER_SEARCH_PRODUCT);
    //            }
    //        });
    //        edtSearch.addTextChangedListener(new SearchTextWatcher());
    //        edtSearch.setFocusable(false);
    //        edtSearch.setFocusableInTouchMode(false);
    //        filterSelectedOutletTags = new ArrayList<String>();
    //        btnFilter= findViewById(R.id.btn_filter);
    //        filterSelectedOutletTags = new ArrayList<String>();
    //        btnFilter.setVisibility(View.GONE);
    //        btnFilter.setOnClickListener(new View.OnClickListener() {
    //            @Override
    //            public void onClick(View view) {
    //                if(outletTags!=null){
    //                    DialogHelper.ShowFilterTagsDialog(ProductListNewOrderActivity.this,outletTags, new ProductsFilterListingAdapter.ProductListSelectedFilterItemClickListner() {
    //                        @Override
    //                        public void onFilterSelected(OutletTags outletTags) {
    //                            counter = counter + 1;
    //                            updateFilterIcon(counter);
    //                            String categoryName = outletTags.getCategoryName();
    //                            filterSelectedOutletTags.add(categoryName);
    //                            updateProductsAdapter();
    //                        }
    //
    //                        @Override
    //                        public void onFilterDeselected(OutletTags outletTags) {
    //                            counter = counter - 1;
    //                            updateFilterIcon(counter);
    //                            String categoryName = outletTags.getCategoryName();
    //                            for (int i = 0; i < filterSelectedOutletTags.size(); i++) {
    //                                if (categoryName.equals(filterSelectedOutletTags.get(i))) {
    //                                    filterSelectedOutletTags.remove(i);
    //                                }
    //                            }
    //                            updateProductsAdapter();
    //                        }
    //                    });
    //                }
    //
    //            }
    //        });
    //        txtUpdateSelectedFilters = findViewById(R.id.txt_number_of_selected_filters);
    //        txtUpdateSelectedFilters.setVisibility(View.GONE);
    //        lytNoFilterResults = findViewById(R.id.lyt_no_filter_results);
    //        txtDeselectFilters = findViewById(R.id.txt_deselect_filters);
    //        txtNoFilterResults = findViewById(R.id.txt_no_result);
    //        txt_select_all = findViewById(R.id.txt_select_all);
    //        txt_select_all.setOnClickListener(new View.OnClickListener() {
    //            @Override
    //            public void onClick(View v) {
    //                if (txt_select_all.getText().toString().equalsIgnoreCase(getString(R.string.txt_select_all))) {
    //                    for (ProductDetailBySupplier item : products) {
    //                        item.setSelected(true);
    //                    }
    //                    lstProducts.getAdapter().notifyDataSetChanged();
    //                    txt_select_all.setText(getResources().getString(R.string.txt_deselect_all));
    //                } else {
    //                    for (ProductDetailBySupplier item : products) {
    //                        item.setSelected(false);
    //                    }
    //                    lstProducts.getAdapter().notifyDataSetChanged();
    //                    txt_select_all.setText(getResources().getString(R.string.txt_select_all));
    //                }
    //            }
    //        });
    //        setFont();
    //        btnDone = findViewById(R.id.btnProductSelectionDone);
    //        if (showProductListCalledFrom == ZeemartAppConstants.CALLED_FROM_REVIEW_ORDER) {
    //            if (selectedProductList != null && selectedProductList.size() > 0) {
    //                btnDone.setClickable(true);
    //                btnDone.setBackgroundResource(R.drawable.blue_rounded_corner);
    //            } else {
    //                btnDone.setClickable(false);
    //                btnDone.setBackgroundResource(R.drawable.dark_grey_rounded_corner);
    //            }
    //        } else {
    //            btnDone.setClickable(false);
    //            btnDone.setBackgroundResource(R.drawable.dark_grey_rounded_corner);
    //        }
    //        txtSearchCancel.setOnClickListener(new View.OnClickListener() {
    //            @Override
    //            public void onClick(View view) {
    //                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    //                edtSearch.setText("");
    //                edtSearch.setCursorVisible(false);
    //                setSearchBarMargin(edtTextMarginEndMin);
    //                txtSearchCancel.setVisibility(View.GONE);
    //                lytRecentSearch.setVisibility(View.GONE);
    ////                btnDone.setVisibility(View.VISIBLE);
    //                lstProducts.setVisibility(View.VISIBLE);
    //                lytReviewItems.setVisibility(View.VISIBLE);
    //            }
    //        });
    //        btnDone.setOnClickListener(new View.OnClickListener() {
    //            @Override
    //            public void onClick(View v) {
    //                if (products != null && products.size() != 0) {
    //                    Log.d("Product List", products + "*****");
    //                    ArrayList<Product> selectedProducts = new ArrayList<>();
    //                    for (int i = 0; i < products.size(); i++) {
    //                        if (products.get(i).isSelected()) {
    //                            Product item;
    //                            String key = ProductDataHelper.getKeyProductMap(products.get(i).getSku(), products.get(i).getPriceList().get(0).getUnitSize());
    //                            if (selectedProductMap.containsKey(key)) {
    //                                item = selectedProductMap.get(products.get(i).getSku() + products.get(i).getPriceList().get(0).getUnitSize());
    //                            } else {
    //                                item = ProductDataHelper.createSelectedProductObject(products.get(i));
    //                            }
    //                            if (item != null) {
    //                                selectedProducts.add(item);
    //                            }
    //                        }
    //                    }
    //                    if (selectedProducts.size() > 0) {
    //                        if (showProductListCalledFrom.equals(ZeemartAppConstants.CALLED_FROM_REVIEW_ORDER)) {
    //                            Intent intent = getIntent();
    //                            String selectedProductJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(selectedProducts);
    //                            intent.putExtra(ZeemartAppConstants.SELECTED_PRODUCT_LIST, selectedProductJson);
    //                            if(!StringHelper.isStringNullOrEmpty(existingDraftOrderId))
    //                                intent.putExtra(ZeemartAppConstants.EXISTING_DRAFT_ID,existingDraftOrderId);
    //                            setResult(RESULT_OK, intent);
    //                            finish();
    //                        } else if (showProductListCalledFrom.equals(ZeemartAppConstants.CALLED_FROM_CREATE_NEW_ORDER)) {
    //                            Intent newIntent = new Intent(ProductListNewOrderActivity.this, ReviewOrderActivity.class);
    //                            ArrayList<Orders> orderList = new ArrayList<>();
    //                            Orders newOrder = new Orders();
    //                            Supplier supplier = new Supplier();
    //                            supplier.setSupplierId(supplierId);
    //                            supplier.setSupplierName(supplierName);
    //                            if (supplierDetails != null) {
    //                                supplier.setLogoURL(supplierDetails.getSupplier().getLogoURL());
    //                            }
    //                            newOrder.setSupplier(supplier);
    //                            newOrder.setProducts(selectedProducts);
    //                            Outlet outlet = new Outlet();
    //                            outlet.setOutletName(SharedPref.read(SharedPref.SELECTED_OUTLET_NAME, ""));
    //                            outlet.setOutletId(SharedPref.read(SharedPref.SELECTED_OUTLET_ID, ""));
    //                            newOrder.setOutlet(outlet);
    //                            orderList.add(newOrder);
    //                            UserDetails createdByUserDetails = new UserDetails();
    //                            createdByUserDetails.setFirstName(SharedPref.read(SharedPref.USER_FIRST_NAME, ""));
    //                            createdByUserDetails.setId(SharedPref.read(SharedPref.USER_ID, ""));
    //                            createdByUserDetails.setImageURL(SharedPref.read(SharedPref.USER_IMAGE_URL, ""));
    //                            createdByUserDetails.setLastName(SharedPref.read(SharedPref.USER_LAST_NAME, ""));
    //                            createdByUserDetails.setPhone(SharedPref.read(SharedPref.USER_PHONE_NUMBER, ""));
    //                            newOrder.setCreatedBy(createdByUserDetails);
    //                            String orderJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(orderList);
    //                            newIntent.putExtra(ZeemartAppConstants.REVIEW_ORDER_LIST, orderJson);
    //                            String supplierJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(orderList.get(0).getSupplier());
    //                            String outletJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(orderList.get(0).getOutlet());
    //                            newIntent.putExtra(ZeemartAppConstants.SUPPLIER_DETAILS, supplierJson);
    //                            newIntent.putExtra(ZeemartAppConstants.OUTLET_DETAILS, outletJson);
    //                            if(!StringHelper.isStringNullOrEmpty(existingDraftOrderId))
    //                                newIntent.putExtra(ZeemartAppConstants.EXISTING_DRAFT_ID,existingDraftOrderId);
    //                            startActivity(newIntent);
    //                            finish();
    //                        }
    //                    } else {
    //                        ZeemartBuyerApp.getToastRed(getString(R.string.txt_no_products_selected));
    //                    }
    //                } else {
    //                    ZeemartBuyerApp.getToastRed(getString(R.string.txt_no_products_selected));
    //                }
    //            }
    //        });
    //    }
    //
    //    private void setFont() {
    //        ZeemartBuyerApp.setTypefaceView(edtSearch, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR);
    //        ZeemartBuyerApp.setTypefaceView(txtUpdateSelectedFilters, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR);
    //        ZeemartBuyerApp.setTypefaceView(txtNoFilterResults, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD);
    //        ZeemartBuyerApp.setTypefaceView(txtDeselectFilters, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR);
    //        ZeemartBuyerApp.setTypefaceView(txtHeadingAddToOrder, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD);
    //        ZeemartBuyerApp.setTypefaceView(btnDone, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD);
    //        ZeemartBuyerApp.setTypefaceView(txtRecentSearchHeader, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD);
    //        ZeemartBuyerApp.setTypefaceView(txtClearRecentSearch, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_SEMI_BOLD);
    //        ZeemartBuyerApp.setTypefaceView(txtSearchCancel, ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR);
    //    }
    //
    //    private void updateDoneButton(int counter) {
    //        if (counter == 0) {
    //            btnDone.setBackgroundResource(R.drawable.dark_grey_rounded_corner);
    //            btnDone.setClickable(false);
    //        } else {
    //            btnDone.setBackgroundResource(R.drawable.blue_rounded_corner);
    //            btnDone.setClickable(true);
    //        }
    //    }
    //
    //    private void updateProductsAdapter() {
    //
    //        if (filterSelectedOutletTags != null && filterSelectedOutletTags.size() > 0) {
    //            List<ProductDetailBySupplier> filteredProductsByTagList = new ArrayList<>();
    //            for (int i = 0; i < products.size(); i++) {
    //                for (int j = 0; j < filterSelectedOutletTags.size(); j++) {
    //                    if (products.get(i).getOutletTags() != null)
    //                        if ((products.get(i).getOutletTags()).containsAll(filterSelectedOutletTags)) {
    //                            if (!(filteredProductsByTagList.contains(products.get(i))))
    //                                filteredProductsByTagList.add(products.get(i));
    //                        }
    //                }
    //            }
    //            if (filteredProductsByTagList.size() == 0) {
    //                lstProducts.setVisibility(View.GONE);
    //                lytNoFilterResults.setVisibility(View.VISIBLE);
    //                btnDone.setVisibility(View.GONE);
    //            } else {
    //                lytNoFilterResults.setVisibility(View.GONE);
    //                lstProducts.setVisibility(View.VISIBLE);
    //                btnDone.setVisibility(View.VISIBLE);
    //            }
    //
    //            if (edtSearch.getText().toString().length() == 0) {
    //                productListAdapter = new SupplierProductListAdapter(false,this, filteredProductsByTagList, outletID,ZeemartAppConstants.CALLED_FROM_SUPPLIER_PRODUCT_LIST, supplierName, supplierId, supplierDetails, selectedProductMap, null,null , new SupplierProductListAdapter.SelectedProductsListener() {
    //                    @Override
    //                    public void onProductSelected(ProductDetailBySupplier productDetailBySupplier) {
    //                        totalProductsSelected = totalProductsSelected + 1;
    //                        updateDoneButton(totalProductsSelected);
    //                    }
    //
    //                    @Override
    //                    public void onProductDeselected(ProductDetailBySupplier productDetailBySupplier) {
    //                        totalProductsSelected = totalProductsSelected - 1;
    //                        updateDoneButton(totalProductsSelected);
    //                    }
    //
    //                    @Override
    //                    public void onProductSelectedForRecentSearch(String productName) {
    //                        if (!StringHelper.isStringNullOrEmpty(productName) && !StringHelper.isStringNullOrEmpty(edtSearch.getText().toString())) {
    //                            setRecentSearchList(productName);
    //                        }
    //                    }
    //                });
    //                lstProducts.setAdapter(productListAdapter);
    //            } else {
    //                filterProductsOnSearchedText(filteredProductsByTagList);
    //            }
    //        } else {
    //            if (edtSearch.getText().toString().length() == 0) {
    //                productListAdapter = new SupplierProductListAdapter(false,this, products, outletID,ZeemartAppConstants.CALLED_FROM_SUPPLIER_PRODUCT_LIST ,supplierName, supplierId, supplierDetails, selectedProductMap, null, null ,new SupplierProductListAdapter.SelectedProductsListener() {
    //                    @Override
    //                    public void onProductSelected(ProductDetailBySupplier productDetailBySupplier) {
    //                        totalProductsSelected = totalProductsSelected + 1;
    //                        updateDoneButton(totalProductsSelected);
    //                    }
    //
    //                    @Override
    //                    public void onProductDeselected(ProductDetailBySupplier productDetailBySupplier) {
    //                        totalProductsSelected = totalProductsSelected - 1;
    //                        updateDoneButton(totalProductsSelected);
    //                    }
    //
    //                    @Override
    //                    public void onProductSelectedForRecentSearch(String productName) {
    //                        if (!StringHelper.isStringNullOrEmpty(productName) && !StringHelper.isStringNullOrEmpty(edtSearch.getText().toString())) {
    //                            setRecentSearchList(productName);
    //                        }
    //                    }
    //                });
    //                lstProducts.setAdapter(productListAdapter);
    //            } else {
    //                filterProductsOnSearchedText(products);
    //            }
    //        }
    //    }
    //
    //    private void filterProductsOnSearchedText(List<ProductDetailBySupplier> products) {
    //        String searchedString = edtSearch.getText().toString();
    //        String[] searchStringArray = searchedString.split(" ");
    //        List<ProductDetailBySupplier> filteredList = new ArrayList<>();
    //        if (products != null) {
    //            for (int i = 0; i < products.size(); i++) {
    //                String productCodeTag = getProductCodeAndTag(products.get(i).getSupplierProductCode(), products.get(i).getTags());
    //                boolean searchStringFound = false;
    //                for (int j = 0; j < searchStringArray.length; j++) {
    //                    if (products.get(i).getProductName().toLowerCase().contains(searchStringArray[j].toLowerCase()) || productCodeTag.toLowerCase().contains(searchStringArray[j].toLowerCase()) || (products.get(i).productCustomName != null && products.get(i).productCustomName.toLowerCase().contains(searchStringArray[j].toLowerCase()))) {
    //                        searchStringFound = true;
    //                    } else {
    //                        searchStringFound = false;
    //                    }
    //                }
    //                if (searchStringFound) {
    //                    if (!(filteredList.contains(products.get(i))))
    //                        filteredList.add(products.get(i));
    //                }
    //            }
    //
    //            Collections.sort(filteredList, new Comparator<ProductDetailBySupplier>() {
    //                @Override
    //                public int compare(ProductDetailBySupplier o1, ProductDetailBySupplier o2) {
    //                    return o1.getProductName().compareTo(o2.getProductName());
    //                }
    //            });
    //        }
    //
    //        if (filteredList.size() == 0) {
    //            lstProducts.setVisibility(View.INVISIBLE);
    //            lytNoFilterResults.setVisibility(View.VISIBLE);
    //            btnDone.setVisibility(View.GONE);
    //        } else {
    //            lytNoFilterResults.setVisibility(View.GONE);
    //            lstProducts.setVisibility(View.VISIBLE);
    //            btnDone.setVisibility(View.VISIBLE);
    //        }
    //        productListAdapter = new SupplierProductListAdapter(false,this, filteredList, outletID, ZeemartAppConstants.CALLED_FROM_SUPPLIER_PRODUCT_LIST,supplierName,supplierId, supplierDetails, selectedProductMap, searchStringArray,null , new SupplierProductListAdapter.SelectedProductsListener() {
    //            @Override
    //            public void onProductSelected(ProductDetailBySupplier productDetailBySupplier) {
    //                totalProductsSelected = totalProductsSelected + 1;
    //                updateDoneButton(totalProductsSelected);
    //            }
    //
    //            @Override
    //            public void onProductDeselected(ProductDetailBySupplier productDetailBySupplier) {
    //                totalProductsSelected = totalProductsSelected - 1;
    //                updateDoneButton(totalProductsSelected);
    //            }
    //
    //            @Override
    //            public void onProductSelectedForRecentSearch(String productName) {
    //                if (!StringHelper.isStringNullOrEmpty(productName) && !StringHelper.isStringNullOrEmpty(edtSearch.getText().toString())) {
    //                    setRecentSearchList(productName);
    //                }
    //            }
    //        });
    //        lstProducts.setAdapter(productListAdapter);
    //    }
    //
    //    private String getProductCodeAndTag(String productCode, List<String> tags) {
    //        String productCodeTag = "";
    //        if (!StringHelper.isStringNullOrEmpty(productCode)) {
    //            productCodeTag = productCode;
    //        }
    //        if (tags != null && tags.size() > 0) {
    //            String tagValue = "";
    //            for (int i = 0; i < tags.size(); i++) {
    //                tagValue = tagValue + tags.get(i) + ",";
    //            }
    //            if (tagValue.length() > 0) {
    //                String attributeValuesAll = tagValue.substring(0, tagValue.length() - 1);
    //                if (StringHelper.isStringNullOrEmpty(productCodeTag)) {
    //                    productCodeTag = attributeValuesAll;
    //                } else {
    //                    productCodeTag = productCodeTag + " • " + attributeValuesAll;
    //                }
    //            }
    //        }
    //        return productCodeTag;
    //    }
    //
    //    private void updateFilterIcon(int counter) {
    //        String totalSelectedFilters = "" + counter;
    //        if (counter != 0) {
    //            txtUpdateSelectedFilters.setVisibility(View.VISIBLE);
    //            txtUpdateSelectedFilters.setText(totalSelectedFilters);
    //        } else {
    //            txtUpdateSelectedFilters.setVisibility(View.GONE);
    //        }
    //    }
    //
    //    class SearchTextWatcher implements TextWatcher {
    //        @Override
    //        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    //        }
    //
    //        @Override
    //        public void onTextChanged(CharSequence s, int start, int before, int count) {
    ////          ProductListNewOrderActivity.this.productListAdapter.getFilter().filter(s);
    //
    //            setRecentSearchListVisibility();
    //            updateProductsAdapter();
    //
    //        }
    //
    //        @Override
    //        public void afterTextChanged(Editable s) {
    //
    //        }
    //    }
    //
    //    private void setRecentSearchListVisibility() {
    //        if (StringHelper.isStringNullOrEmpty(edtSearch.getText().toString())) {
    //            if (recentSearchList != null && recentSearchList.size() > 0) {
    //                lytRecentSearch.setVisibility(View.VISIBLE);
    //            } else {
    //                lytRecentSearch.setVisibility(View.GONE);
    //            }
    //            lstProducts.setVisibility(View.GONE);
    //            lytReviewItems.setVisibility(View.GONE);
    //            lytNoFilterResults.setVisibility(View.GONE);
    //            threeDotLoaderBlue.setVisibility(View.GONE);
    //        } else {
    //            lytRecentSearch.setVisibility(View.GONE);
    //            setProductTagsVisibility();
    //            lstProducts.setVisibility(View.VISIBLE);
    //            setSearchBarMargin(edtTextMarginEndMax);
    //            txtSearchCancel.setVisibility(View.VISIBLE);
    //            lytReviewItems.setVisibility(View.VISIBLE);
    //        }
    //    }
    //
    //    private void setSupplier(String supplierJson) {
    //        Supplier suppliers = ZeemartBuyerApp.gsonExposeExclusive.fromJson(supplierJson, Supplier.class);
    //        for (int i = 0; i < products.size(); i++) {
    //            products.get(i).setSupplier(suppliers);
    //        }
    //    }
    //
    //    private void setProductTagsVisibility() {
    //        if (isProductTagsVisible) {
    //            btnFilter.setVisibility(View.VISIBLE);
    //        } else {
    //            btnFilter.setVisibility(View.GONE);
    //            txtUpdateSelectedFilters.setVisibility(View.GONE);
    //        }
    //    }
    //
    //    private void setRecentSearchListAdapter(List<String> recentSearchLst) {
    //        if (recentSearchLst != null && recentSearchLst.size() > 0)
    //            for (int i = 0; i < recentSearchLst.size(); i++) {
    //                if (recentSearchLst.size() > RECENT_SEARCH_LIMIT) {
    //                    recentSearchLst.remove(recentSearchLst.size() - 1);
    //                }
    //            }
    //        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
    //                android.R.layout.simple_list_item_1, recentSearchLst) {
    //            @Override
    //            public View getView(int position, View convertView, ViewGroup parent) {
    //                View view = super.getView(position, convertView, parent);
    //                Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/" + ZeemartAppConstants.TYPEFACE_SOURCE_SANS_PRO_REGULAR);
    //                TextView text = (TextView) view.findViewById(android.R.id.text1);
    //                text.setTypeface(typeface);
    //                return view;
    //            }
    //        };
    //        lstRecentSearch.setAdapter(adapter);
    //    }
    //
    //    private void setRecentSearchList(String name) {
    //        if (!recentSearchList.contains(name)) {
    //            recentSearchList.add(TOP, name);
    //        }
    //        saveRecentSearchInSharedPref(recentSearchList);
    //        setRecentSearchListAdapter(recentSearchList);
    //    }
    //
    //    private void saveRecentSearchInSharedPref(List<String> recentSearchLst) {
    //        for (int i = 0; i < recentSearchLst.size(); i++) {
    //            if (recentSearchLst.size() > RECENT_SEARCH_LIMIT) {
    //                recentSearchLst.remove(recentSearchLst.size() - 1);
    //            }
    //        }
    //        String recentSearchJson = ZeemartBuyerApp.gsonExposeExclusive.toJson(recentSearchList);
    //        SharedPref.write(supplierId, recentSearchJson);
    //    }
    //
    //    private void setSearchBarMargin(int edtTextWidth) {
    //        int marginRightInDimensionDP = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, edtTextWidth, getResources().getDisplayMetrics());
    //        int marginTopInDimensionDP = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
    //        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) edtSearch.getLayoutParams();
    //        params.setMargins(0, marginTopInDimensionDP, marginRightInDimensionDP, 0);
    //        edtSearch.setLayoutParams(params);
    //    }
}