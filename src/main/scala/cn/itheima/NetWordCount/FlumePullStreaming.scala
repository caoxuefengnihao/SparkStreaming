package cn.itheima.NetWordCount

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.flume.{FlumeUtils, SparkFlumeEvent}

/**
  * Streaming 整合 Flume Pull方式
  * 导入相关的依赖 具体看官网
  *
  *
  */
object FlumePullStreaming {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("FlumePullStreaming").setMaster("spark://dshuju02:7077")
    val stream = new StreamingContext(conf,Seconds(10))

    /**
      * Flume 整合 SparkStreaming 用到一个工具类
      * val flumeStream = FlumeUtils.createPollingStream(streamingContext, [sink machine hostname], [sink port])
      */
    val flumeStream: ReceiverInputDStream[SparkFlumeEvent] = FlumeUtils.createPollingStream(stream,"192.168.2.129", 41414)

    /**
      * flume 的传输数据的基本单元是events
      * A Flume event is defined as a unit of data flow having a byte payload
      * and an optional set of string attributes.
      */
    val dstream: DStream[(String, Int)] = flumeStream.map(x =>new String(x.event.getBody.array()).trim).flatMap(x=>x.split(" ")).map((_,1)).reduceByKey(_+_)
    dstream.print()
    stream.start()
    stream.awaitTermination()


  }

}
