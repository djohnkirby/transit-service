package gtfs

import gtfs.ScheduleFinder.Dependencies
import gtfs.Station.{stationHeadSignMap, stationIdMap, Station}
import gtfs.models.{Calendar, StopTime}
import gtfs.parser.GtfsFileReader

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalDateTime, ZoneId}

class ScheduleFinder(dependencies: Dependencies) {
  val etZoneId: ZoneId = ZoneId.of("America/New_York")
  val today            = LocalDate.now()
  val now              = LocalDateTime.now(etZoneId)
  val gtfsFileReader   = dependencies.gtfsfileReader

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

  def isTerminalStop(stopTime: StopTime) =
    trips
      .find(_.trip_id == stopTime.trip_id)
      .fold(false)(
        tripRec => {
          stationIdMap(Station.stationHeadSignMap(tripRec.trip_headsign)).contains(stopTime.stop_id)
        }
      )

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

  def findSchedule(station: Station, walkTime: Long): String =
    findSchedule(Station.stationIdMap(station), walkTime)

  def findSchedule(stationIds: Set[String], walkTime: Long): String = {

    if (stationIds.isEmpty)
      return "Error, no stationIDs found for this station"

    val stopTimes = stopTimeRecs
      .map(_.toStopTime(today, walkTime))
    val upcomingStopTimesForThisStop = stopTimes
      .filter(stopTime => isThisStation(stopTime, stationIds))
      .filter(isRunningToday)
      .filter(isUpcomingStopTime)
      .filter(stopTime => { !isTerminalStop(stopTime) })

    reduceUpcomingDeparturesMapToFrameText(createUpcomingDeparturesMap(upcomingStopTimesForThisStop))
  }

}

object ScheduleFinder {

  trait Dependencies {
    val gtfsfileReader: GtfsFileReader
  }
}
