package id.co.agogo.config

class MapToJson {
  fun map(hashMap: HashMap<String, String>): String {
    return hashMap.toString().replace(", ", "&").replace("{", "").replace("}", "")
  }
}