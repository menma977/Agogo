package id.co.agogo.config

/**
 * class MapToJson
 * convert MAP to JSONObject
 */
class MapToJson {
  fun map(hashMap: HashMap<String, String>): String {
    return hashMap.toString().replace(", ", "&").replace("{", "").replace("}", "")
  }
}