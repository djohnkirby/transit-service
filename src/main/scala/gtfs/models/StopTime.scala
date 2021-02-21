package gtfs.models

import java.time.LocalDateTime

/**
  * A stop on trip at specific date and time
  *
  * @param rec GTFS stoptime record used to generate this stop
  * @param arrival date and time of arrival
  * @param departure date and time of departure
  */
class StopTime(
  rec: StopTimeRec,
  val arrival: Option[LocalDateTime],
  val departure: Option[LocalDateTime]
) {
  def stop: Stop            = rec.stop
  def stop_id: String       = rec.stop_id
  def trip_id: String       = rec.trip_id
  def sequence: Option[Int] = rec.stop_sequence
}
