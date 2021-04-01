package gtfs

object Station extends Enumeration {
  type Station = Value
  val Hoboken       = Value("Hoboken")
  val Newport       = Value("Newport")
  val ThirtyThird   = Value("33rd")
  val TwentyThird   = Value("23rd")
  val Fourteenth    = Value("14th")
  val Ninth         = Value("9th")
  val Christopher   = Value("Christopher")
  val Grove         = Value("Grove")
  val WTC           = Value("WTC")
  val ExchangePlace = Value("Exchange")
  val JournalSquare = Value("JSQ")
  val Newark        = Value("Newark")
  val Harrison      = Value("Harrison")

  val stationIdMap = Map[Station, Set[String]](
    Hoboken       -> Set("26730", "781715", "781716", "781717", "782499", "782500"),
    Newport       -> Set("26732", "781728", "781729", "782498"),
    ThirtyThird   -> Set("781740", "781741", "782508", "782509", "782510", "782511"),
    TwentyThird   -> Set("26723", "781738", "781739", "782507"),
    Fourteenth    -> Set("26722", "781736", "781737", "782505", "782506"),
    Ninth         -> Set("26725", "781734", "781735", "782504"),
    Christopher   -> Set("26726", "781732", "781733", "782503"),
    Grove         -> Set("26728", "781726", "781727", "782496", "782497"),
    WTC           -> Set("26734", "781763", "782512", "794724"),
    ExchangePlace -> Set("26727", "781730", "781731", "782501"),
    JournalSquare -> Set("26731", "781722", "781723", "781724", "781725", "782494", "782495"),
    Newark        -> Set("26733", "781718", "781719", "782490", "782491"),
    Harrison      -> Set("26729", "781720", "781721", "782492", "782493")
  )

  val stationHeadSignMap = Map[String, Station](
    "33rd via Hoboken"           -> ThirtyThird,
    "Journal Square via Hoboken" -> JournalSquare,
    "Hoboken"                    -> Hoboken,
    "33rd Street"                -> ThirtyThird,
    "World Trade Center"         -> WTC,
    "Newark"                     -> Newark
  )
}
