package gtfs

object HeadSign {

  val headSignToShortHeadsignMap: Map[String, String] = Map(
    "33rd via Hoboken"           -> "33rd/HOB",
    "Journal Square via Hoboken" -> "JSQ/HOB",
    "Hoboken"                    -> "HOB",
    "33rd Street"                -> "33rd",
    "World Trade Center"         -> "WTC",
    "Newark"                     -> "NWK"
  )
}
