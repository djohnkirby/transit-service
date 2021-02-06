package gtfs

import gtfs.ScheduleFinder.Dependencies
import gtfs.models.{StopTime, StopTimeRec}
import gtfs.parser.GtfsFileReader

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalDateTime}

class ScheduleFinder(dependencies: Dependencies) {
  val today          = LocalDate.now()
  val now            = LocalDateTime.now()
  val gtfsFileReader = dependencies.gtfsfileReader

  val stopTimeRecs: Iterator[StopTimeRec] = gtfsFileReader.getStopTimes
  val trips                               = gtfsFileReader.getTrips
  val calendar                            = gtfsFileReader.getCalendar

  var destinationsToTimesMap: Map[String, Set[Long]] = Map.empty //Might wanna move this

  def findSchedule(stationIds: Set[String], walkTime: Long): String =
    destinationsToTimesMap.keys
      .map(
        shortHeadSign => {
          shortHeadSign + " : " + destinationsToTimesMap(shortHeadSign).toList.sorted
            .map(_.toString)
            .reduce((acc, str) => s"$acc, $str")
        }
      )
      .toList
      .reduce(
        (a, b) => s"$a, $b"
      )

  //get upcoming stop times for a particular station
  def stopTimes(walkTime: Long, stationIds: Set[String]) =
    stopTimeRecs
      .map(_.toStopTime(today, walkTime))
      .filter(stopTime => stationIds.contains(stopTime.stop_id))
      .filter(
        stopTime =>
          stopTime.departure
            .exists(departureTime => departureTime.isAfter(now) && departureTime.isBefore(now.plusHours(2)))
      ) //DanielTODO: break these down into smaller and smaller methods so we can get them under test
      .foreach(
        stopTime => {
          trips
            .filter(_.trip_id == stopTime.trip_id)
            .toList
            .headOption
            .foreach(tripRec => {
              val shortHeadSign = HeadSign.headSignToShortHeadsignMap(tripRec.trip_headsign)
              val minutes       = ChronoUnit.MINUTES.between(now, stopTime.departure.get)
              val set           = destinationsToTimesMap(shortHeadSign)
              val newSet        = set + minutes
              destinationsToTimesMap += (shortHeadSign -> newSet)
            })
        }
      )
}

object ScheduleFinder {

  trait Dependencies {
    val gtfsfileReader: GtfsFileReader
  }
}
