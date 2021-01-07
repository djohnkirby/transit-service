package gtfs.parser

import gtfs.models._

trait GtfsReader {
  def getStops: Iterator[Stop]
  def getStopTimes: Iterator[StopTimeRec]
  //def getTrips: Iterator[TripRec]
  //def getRoutes: Iterator[Route]
  //def getFrequencies: Iterator[Frequency]
  //def getCalendar: Iterator[CalendarRec]
  //def getCalendarDates: Iterator[CalendarDateRec]

  //ef toGtfsData:GtfsData = new GtfsData(this)
}