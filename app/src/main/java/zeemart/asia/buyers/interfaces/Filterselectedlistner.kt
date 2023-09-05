package zeemart.asia.buyers.interfaces

import zeemart.asia.buyers.models.OutletTags

interface Filterselectedlistner {
    fun onItemfiltermarketClick(
        categorylist: List<OutletTags?>?,  /* List<OutletTags> supplierlist,*/
        tagslist: List<OutletTags?>?,
        certificationlist: List<OutletTags?>?
    )
}