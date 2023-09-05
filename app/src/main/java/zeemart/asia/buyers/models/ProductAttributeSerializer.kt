package zeemart.asia.buyers.modelsimport

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import zeemart.asia.buyers.models.ProductAttributeModel
import java.lang.reflect.Type

/**
 * Created by ParulBhandari on 5/9/2018.
 * Attribute serializer created so that dynamic attribute names can be handled.
 */
class ProductAttributeSerializer : JsonDeserializer<ProductAttributeModel> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ProductAttributeModel {
        val jsonObject = json.asJsonObject
        val entrySet = jsonObject.entrySet()
        val productAttributeModel = ProductAttributeModel()
        for ((key, value) in entrySet) {
            productAttributeModel.specificAttribute = key
            val data: MutableList<String> = ArrayList()
            val valArray = value.asJsonArray
            if (valArray != null && valArray.size() > 0) {
                for (i in 0 until valArray.size()) {
                    if (!valArray[i].isJsonNull) {
                        data.add(valArray[i].asString)
                    }
                }
            }
            productAttributeModel.specificAttributeValue = data
        }
        return productAttributeModel
    }
}