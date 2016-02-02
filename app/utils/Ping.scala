package utils


case class Ping(id:Long,
                driver_id:Int,
                location:(Double,Double),
                time:Long
               )