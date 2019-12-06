/*
package cn.itheima.NetWordCount

import kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka.KafkaUtils

/**
  * 实战之Streaming 整合 Kafka  第二种方式 Direct Approach (No Receivers)
  *
  */
object StreamingPullKafa02 {
  def main(args: Array[String]): Unit = {
    val con = new SparkConf().setAppName("StreamingPullKafa02").setMaster("spark://dshuju02:7077")
    val stream = new StreamingContext(con,Seconds(10))
    /**
      * In the streaming application code, import KafkaUtils and create an input DStream as follows.
      *
      */
    val map = Map[String,String]("metadata.broker.list"->"dshuju01:9092")
    val set = Set("three")
    val kafkaStream: InputDStream[(String, String)] = KafkaUtils.createDirectStream[String,String,StringDecoder,StringDecoder](stream,map,set)
    val unit: DStream[(String, Int)] = kafkaStream.map(_._2).flatMap(_.split(" ")).map((_,1)).reduceByKey(_+_)
    unit.print()
    stream.start()
    stream.awaitTermination()
  }
}
*/
