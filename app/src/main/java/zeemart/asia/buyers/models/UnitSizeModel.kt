package zeemart.asia.buyers.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import zeemart.asia.buyers.ZeemartBuyerApp
import zeemart.asia.buyers.constants.ZeemartAppConstants
import zeemart.asia.buyers.helper.SharedPref
import zeemart.asia.buyers.helper.StringHelper
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

/**
 * Created by ParulBhandari on 12/18/2017.
 */
class UnitSizeModel {
    @SerializedName("unitSize")
    @Expose
    var unitSize: String? = null

    @SerializedName("shortName")
    @Expose
    var shortNameWithoutTranslation: String? = null
        private set

    @SerializedName("isDecimalAllowed")
    @Expose
    var isDecimalAllowed: Boolean? = null

    @Expose
    var isSelected = false
    fun getShortName(): String? {
        return if (SharedPref.read(
                SharedPref.SELECTED_LANGUAGE,
                ZeemartAppConstants.Language.ENGLISH.langCode
            ) == ZeemartAppConstants.Language.CHINESE.langCode
        ) {
            chineseShortName
        } else {
            shortNameWithoutTranslation
        }
    }

    val chineseShortName: String?
        get() = if (UomType.Bag.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Bag.chinesValue + ")"
        } else if (UomType.Block.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Block.chinesValue + ")"
        } else if (UomType.Box.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Box.chinesValue + ")"
        } else if (UomType.Bottle.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Bottle.chinesValue + ")"
        } else if (UomType.Bucket.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Bucket.chinesValue + ")"
        } else if (UomType.Bunch.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Bunch.chinesValue + ")"
        } else if (UomType.Bundle.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Bundle.chinesValue + ")"
        } else if (UomType.Can.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Can.chinesValue + ")"
        } else if (UomType.Carton.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Carton.chinesValue + ")"
        } else if (UomType.Case.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Case.chinesValue + ")"
        } else if (UomType.Cask.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Cask.chinesValue + ")"
        } else if (UomType.Container.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Container.chinesValue + ")"
        } else if (UomType.Crate.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Crate.chinesValue + ")"
        } else if (UomType.Cup.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Cup.chinesValue + ")"
        } else if (UomType.Cylinder.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Cylinder.chinesValue + ")"
        } else if (UomType.Dozen.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Dozen.chinesValue + ")"
        } else if (UomType.Each.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Each.chinesValue + ")"
        } else if (UomType.Fillet.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Fillet.chinesValue + ")"
        } else if (UomType.Grams.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Grams.chinesValue + ")"
        } else if (UomType.Jar.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Jar.chinesValue + ")"
        } else if (UomType.Keg.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Keg.chinesValue + ")"
        } else if (UomType.Kilogram.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Kilogram.chinesValue + ")"
        } else if (UomType.Litre.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Litre.chinesValue + ")"
        } else if (UomType.Loaf.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Loaf.chinesValue + ")"
        } else if (UomType.Ounce.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Ounce.chinesValue + ")"
        } else if (UomType.Pack.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Pack.chinesValue + ")"
        } else if (UomType.Packet.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Packet.chinesValue + ")"
        } else if (UomType.Pail.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Pail.chinesValue + ")"
        } else if (UomType.Pair.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Pair.chinesValue + ")"
        } else if (UomType.Pieces.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Pieces.chinesValue + ")"
        } else if (UomType.Pound.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Pound.chinesValue + ")"
        } else if (UomType.Pouch.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Pouch.chinesValue + ")"
        } else if (UomType.Punnet.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Punnet.chinesValue + ")"
        } else if (UomType.Pot.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Pot.chinesValue + ")"
        } else if (UomType.Roll.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Roll.chinesValue + ")"
        } else if (UomType.Sachet.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Sachet.chinesValue + ")"
        } else if (UomType.Set.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Set.chinesValue + ")"
        } else if (UomType.Sheet.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Sheet.chinesValue + ")"
        } else if (UomType.Stack.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Stack.chinesValue + ")"
        } else if (UomType.Stick.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Stick.chinesValue + ")"
        } else if (UomType.Tin.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Tin.chinesValue + ")"
        } else if (UomType.Tray.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Tray.chinesValue + ")"
        } else if (UomType.Tub.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Tub.chinesValue + ")"
        } else if (UomType.Tube.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Tube.chinesValue + ")"
        } else if (UomType.Unit.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Unit.chinesValue + ")"
        } else if (UomType.Week.value.equals(unitSize, ignoreCase = true)) {
            shortNameWithoutTranslation + " (" + UomType.Week.chinesValue + ")"
        } else {
            shortNameWithoutTranslation
        }

    enum class UomType(val value: String, val chinesValue: String) {
        Bag("Bag", "包"), Block("Block", "块"), Bottle("Bottle", "瓶"), Box(
            "Box",
            "盒"
        ),
        Bucket("Bucket", "桶"), Bunch("Bunch", "串"), Bundle("Bundle", "束"), Can(
            "Can",
            "罐"
        ),
        Carton("Carton", "箱"), Case("Case", "箱"), Cask("Cask", "桶"), Container(
            "Container",
            "盒/箱"
        ),
        Crate("Crate", "箱"), Cup("Cup", "杯"), Cylinder("Cylinder", "缸"), Dozen(
            "Dozen",
            "打"
        ),
        Each("Crate", "个"), Fillet("Crate", "块"), Grams("Grams", "克"), Jar("Jar", "罐"), Keg(
            "Keg",
            "桶"
        ),
        Kilogram("Kilogram", "公斤"), Litre("Litre", "公升"), Loaf("Loaf", "条"), Ounce(
            "Ounce",
            "盎司"
        ),
        Pack("Pack", "包"), Packet("Packet", "包"), Pail("Pail", "桶"), Pair(
            "Pair",
            "對"
        ),
        Pieces("Pieces", "个"), Pouch("Pouch", "袋"), Pound("Pound", "磅"), Pot(
            "Pot",
            "锅"
        ),
        Punnet("Punnet", "筐"), Roll("Roll", "卷"), Sachet("Sachet", "袋"), Set(
            "Set",
            "套"
        ),
        Sheet("Sheet", "张"), Stack("Stack", "堆"), Stick("Stick", "根"), Tin("Tin", "罐"), Tray(
            "Tray",
            "盘"
        ),
        Tub("Tub", "桶"), Tube("Tube", "管"), Unit("Unit", "单"), Week("Week", "週");

        override fun toString(): String {
            return value
        }
    }

    fun setShortName(shortName: String?) {
        shortNameWithoutTranslation = shortName
    }

    companion object {
        @JvmStatic
        fun getunitSizeMap(): Map<String, UnitSizeModel>? {
            val unitSizeData = SharedPref.read(SharedPref.UNIT_SIZE_MAP, "")
            return ZeemartBuyerApp.gsonExposeExclusive.fromJson(
                unitSizeData,
                object : TypeToken<Map<String?, UnitSizeModel?>?>() {}.type
            )
        }

        /**
         * checks for the unit size in th Unit Size Map and returns the value
         * if the key does not exists returns the Key as the value
         *
         * @param key
         * @return
         */
        @JvmStatic
        fun returnShortNameValueUnitSize(key: String?): String? {
            var key = key
            if (key == null) {
                key = ""
                return key
            }
            return if (getunitSizeMap() != null && getunitSizeMap()!!
                    .containsKey(key)
            ) {
                getunitSizeMap()!![key]!!.getShortName()
            } else {
                key
            }
        }

        /**
         * checks for the unit size in th Unit Size Map and returns the value
         * if the key does not exists returns the Key as the value
         *
         * @param key
         * @return
         */
        @JvmStatic
        fun returnShortNameValueUnitSizeForQuantity(key: String?): String? {
            var key = key
            if (key == null) {
                key = ""
                return key
            }
            return if (getunitSizeMap() != null && getunitSizeMap()!!
                    .containsKey(key)
            ) {
                getunitSizeMap()!![key]!!.shortNameWithoutTranslation
            } else {
                key
            }
        }

        /**
         * checks for the unit size in th Unit Size Map
         * and return boolean is the unit size should have decimal values or not
         *
         * @param key
         * @return boolean
         */
        @JvmStatic
        fun isDecimalAllowed(key: String): Boolean {
            return if (!StringHelper.isStringNullOrEmpty(key) && getunitSizeMap()!!
                    .containsKey(key) && getunitSizeMap()!![key]!!.isDecimalAllowed!!
            ) {
                true
            } else {
                false
            }
        }

        /**
         * check if the decimal is allowed for the particular UOM
         *
         * @return quantity/MOQ with decimal or without decimal
         */
        @JvmStatic
        fun getValueDecimalAllowedCheck(unitSize: String, data: Double?): String {
            var data = data
            if (data == null) {
                data = 0.0
            }
            val value: String
            //        DecimalFormatSymbols symbols = new DecimalFormatSymbols(ZeemartAppConstants.Market.VIETNAM.getLocale());
            val symbols = DecimalFormatSymbols(ZeemartAppConstants.Market.`this`.locale)
            value = if (isDecimalAllowed(unitSize)) {
                DecimalFormat("###,###,###.###", symbols).format(data)
            } else {
                DecimalFormat("###,###,###", symbols).format(data)
            }
            return value
        }

        fun getValueDecimalAllowedCheckWithoutThousandSeparator(
            unitSize: String,
            data: Double?
        ): String {
            var data = data
            if (data == null) {
                data = 0.0
            }
            val value: String
            //        DecimalFormatSymbols symbols = new DecimalFormatSymbols(ZeemartAppConstants.Market.VIETNAM.getLocale());
            val symbols = DecimalFormatSymbols(ZeemartAppConstants.Market.`this`.locale)
            value = if (isDecimalAllowed(unitSize)) {
                DecimalFormat("#########.##", symbols).format(data)
            } else {
                DecimalFormat("#########", symbols).format(data)
            }
            return value
        }

        @JvmStatic
        fun getValueDecimal(data: Double?): String {
            var data = data
            if (data == null) {
                data = 0.0
            }
            val value: String
            //        DecimalFormatSymbols symbols = new DecimalFormatSymbols(ZeemartAppConstants.Market.VIETNAM.getLocale());
            val symbols = DecimalFormatSymbols(ZeemartAppConstants.Market.`this`.locale)
            value = DecimalFormat("###,###,###.##", symbols).format(data)
            return value
        }
    }
}