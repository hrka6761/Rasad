package ir.srp.rasad.core.utils

import com.google.gson.Gson
import java.lang.reflect.Type
import javax.inject.Inject

class JsonConverter @Inject constructor(private val gson: Gson) {

    fun convertObjectToJsonString(obj: Any): String = gson.toJson(obj)

    fun convertJsonStringToObject(jsonString: String, clazz: Type): Any = gson.fromJson(jsonString, clazz)
}