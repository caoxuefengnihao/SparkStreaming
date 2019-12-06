package cn.itheima.NetWordCount

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.DStream
//import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * 实战之Streaming 整合 Kafka  第一种方式 Receiver-based Approach
  *
  */
object StreamingPullKafka {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("StreamingPullKafka").setMaster("spark://dshuju02:7077")
    val stream = new StreamingContext(conf,Seconds(10))

    /**
      * In the streaming application code, import KafkaUtils and create an input DStream as follows.
      * val kafkaStream = KafkaUtils.createStream(streamingContext,
      * [ZK quorum], [consumer group id], [per-topic number of Kafka partitions to consume])
      */
    /*val map = Map("kafka-stream"->2)
    val kafkaStream =KafkaUtils.createStream(stream,"dshuju01:2181","test",map)
    val unit: DStream[(String, Int)] = kafkaStream.map(_._2).flatMap(_.split(" ")).map((_,1)).reduceByKey(_+_)
    unit.print()
    stream.start()
    stream.awaitTermination()*/
  }

}
