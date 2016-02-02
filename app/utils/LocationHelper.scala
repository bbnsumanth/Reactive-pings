package utils

import scala.util.Random

object LocationHelper {
  def getLocation(longitude: Double, latitude: Double, radius: Int): (Double, Double) ={
    val random: Random = new Random
    val radiusInDegrees: Double = radius / 111000f
    val u: Double = random.nextDouble
    val v: Double = random.nextDouble
    val w: Double = radiusInDegrees * Math.sqrt (u)
    val t: Double = 2 * Math.PI * v
    val x: Double = w * Math.cos (t)
    val y: Double = w * Math.sin (t)
    val new_x: Double = x / Math.cos (latitude)
    val foundLongitude: Double = new_x + longitude
    val foundLatitude: Double = y + latitude
    (foundLongitude,foundLatitude)
  }

}
