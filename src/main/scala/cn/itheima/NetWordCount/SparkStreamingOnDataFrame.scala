package cn.itheima.NetWordCount

import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * DataFrame and SQL Operations
  * Each RDD is converted to a DataFrame, registered as a temporary table and then queried using SQL.
  *
  *
  */
object SparkStreamingOnDataFrame {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("SparkStreamingOnDataFrame").setMaster("spark://dshuju02:7077")
    val stream = new StreamingContext(conf,Seconds(30))
    val value: ReceiverInputDStream[String] = stream.socketTextStream("192.168.2.129",9999)
    val value1: DStream[String] = value.flatMap(_.split(" "))
    value1.foreachRDD(rdd =>{
      val spark  =  SparkSession.builder().appName("SparkStreamingOnDataFrame").master("spark://dshuju02:7077").getOrCreate()
      import spark.implicits._
      val frame: DataFrame = rdd.toDF("word")
      frame.createOrReplaceTempView("wordcount")
      val frame1: DataFrame = spark.sql("select word,count(*) as counts from wordcount group by word")
      frame1.show()
    })
    stream.start()
    stream.awaitTermination()
  }
}
