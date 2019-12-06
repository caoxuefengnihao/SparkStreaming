package demo


import org.apache.commons.codec.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

object kafka2144 {
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("kafka2144").setMaster("")
    val stream = new StreamingContext(conf,Seconds(5))

    KafkaUtils.createDirectStream[String,String,StringDecoder,StringDecoder]()





  }

}
