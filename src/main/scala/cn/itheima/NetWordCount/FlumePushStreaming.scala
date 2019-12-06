package cn.itheima.NetWordCount

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.flume.{FlumeUtils, SparkFlumeEvent}
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * SparkStreaming 整合Flume 实战  Push 方式
  * SparkStreaming可以有很多的源
  *
  * The General Requirements:
  * Choose a machine in your cluster such that
  * 1:When your Flume + Spark Streaming application is launched, one of the Spark workers must run on that machine.
  * 2:Flume can be configured to push data to a port on that machine
  * 3:notes:Due to the push model, the streaming application needs to be up,
  * with the receiver scheduled and listening on the chosen port,
  * for Flume to be able push data.
  */
object FlumePushStreaming {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("FlumePushStreaming").setMaster("spark://dshuju02:7077")
    val stream = new StreamingContext(conf,Seconds(10))

    /**
      * Flume 整合 SparkStreaming 用到一个工具类
      *  val flumeStream = FlumeUtils.createStream(streamingContext, [chosen machine's hostname], [chosen port])
      */
   val flumeStream: ReceiverInputDStream[SparkFlumeEvent] = FlumeUtils.createStream(stream,"192.168.2.129", 41414)

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

  /*
如果打的是瘦包，则使用–packages这个参数
但是在工作中不建议使用–packages这个参数，因为一旦使用了这个参数；
也就意味着需要去外网下(如果公司有私服还行，如果没有就会很麻烦)。注意：生产上慎用–packages
还有第2种解决方案，以该案例为例：
先去maven仓库中将spark-streaming-flume-assembly下载到本地；
然后使用spark-submit提交的时候，使用–jars参数指定相应的jar包去提交。
但是也会带来一个问题：如果jar包太多，一个一个写会很麻烦；
我们可以写一个shell脚本，将目录里的jar包给遍历出来，
然后拼成一个字符串放到–jars里去，
然后再提交( –jars xxx 指定目录 –> 不行！！)
   */
}
