package gtfs

import gtfs.ScheduleFinder.Dependencies
import gtfs.models.{Calendar, StopTime}
import gtfs.parser.GtfsFileReader

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalDateTime}

class ScheduleFinder(dependencies: Dependencies) {
  val today          = LocalDate.now()
  val now            = LocalDateTime.now()
  val gtfsFileReader = dependencies.gtfsfileReader

  val stopTimeRecs       = gtfsFileReader.getStopTimes.toList
  val trips              = gtfsFileReader.getTrips.toList
  val calendar           = gtfsFileReader.getCalendar.toList
  val calendarExceptions = gtfsFileReader.getCalendarDates.toList
  val services           = new Calendar(calendar.to(Iterable), calendarExceptions.to(Iterable))

  def isThisStation(stopTime: StopTime, stationIds: Set[String]) =
    stationIds.contains(stopTime.stop_id)

  def isUpcomingStopTime(stopTime: StopTime) =
    stopTime.departure
      .exists(departureTime => departureTime.isAfter(now) && departureTime.isBefore(now.plusHours(2)))

  def isRunningToday(stopTime: StopTime) = {
    val date = LocalDate.from(stopTime.departure.getOrElse(today))
    trips
      .find(_.trip_id == stopTime.trip_id)
      .fold(false)(
        tripRec => services.getServiceFor(date).contains(tripRec.service_id)
      )
  }

  def createUpcomingDeparturesMap(stopTimes: List[StopTime]) = {
    var destinationsToTimesMap: Map[String, Set[Long]] = Map.empty
    stopTimes
      .foreach(
        stopTime => {
          trips
            .find(_.trip_id == stopTime.trip_id)
            .foreach(tripRec => {
              val shortHeadSign = HeadSign.headSignToShortHeadsignMap(tripRec.trip_headsign) //TODO: handle not finding this
              val minutes       = ChronoUnit.MINUTES.between(now, stopTime.departure.get)
              val set =
                if (destinationsToTimesMap.contains(shortHeadSign))
                  destinationsToTimesMap(shortHeadSign)
                else
                  Set.empty[Long]
              val newSet = set + minutes
              destinationsToTimesMap += (shortHeadSign -> newSet)
            })
        }
      )

    destinationsToTimesMap
  }

  def reduceUpcomingDeparturesMapToFrameText(destinationsToTimesMap: Map[String, Set[Long]]) =
    if (destinationsToTimesMap.isEmpty)
      "There are no upcoming departures from this stop"
    else
      destinationsToTimesMap.keys
        .map(
          shortHeadSign => {
            shortHeadSign + ": " + destinationsToTimesMap(shortHeadSign).toList.sorted
              .take(4)
              .map(_.toString)
              .reduce((acc, str) => s"$acc, $str")
          }
        )
        .toList
        .reduce(
          (a, b) => s"$a, $b"
        )

  def findSchedule(stationIds: Set[String], walkTime: Long): String = {
    val stopTimes = stopTimeRecs
      .map(_.toStopTime(today, walkTime))
    val upcomingStopTimesForThisStop = stopTimes
      .filter(stopTime => isThisStation(stopTime, stationIds))
      .filter(isRunningToday)
      .filter(isUpcomingStopTime)
    reduceUpcomingDeparturesMapToFrameText(createUpcomingDeparturesMap(upcomingStopTimesForThisStop))
  }

}

object ScheduleFinder {

  trait Dependencies {
    val gtfsfileReader: GtfsFileReader
  }
}
