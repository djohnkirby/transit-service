package gtfs.models

case class TripRec(
  trip_id: String,
  service_id: String,
  route_id: String,
  trip_headsign: String,
  stopTimes: Seq[StopTimeRec]
)
