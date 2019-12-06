package cn.itheima.NetWordCount

import java.sql.{DriverManager, PreparedStatement}

import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}


/**
  * 我们想要实时的过滤一些在黑名单里面的一些用户  那么就涉及到Dstream与Spark内核中的Rdd 相互转化
  * 的问题 那么这么相互转化呢  这里就用到了 Dstream中的一个算子
  *
  * SparkStreaming + flume + kafka + Logger 实现黑名单过滤
  */
object BlocksApp {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("BlocksApp").master("spark://dshuju02:7077").getOrCreate()
    val sc = spark.sparkContext
    val unit: RDD[String] = sc.parallelize(List("zs","ls"))
    val tuples: RDD[(String, Boolean)] = unit.map((_,true))
    val stream = new StreamingContext(sc,Seconds(5))
    /*val map = Map("metadata.broker.list"->"dshuju01:9092")
    val set = Set("blocks-topic")
    val kafkaStream: InputDStream[(String, String)] = KafkaUtils.createDirectStream[String,String,StringDecoder,StringDecoder](stream,map,set)*/
    val kafkaStream: ReceiverInputDStream[String] = stream.socketTextStream("192.168.2.129",9999)
   /* val value: DStream[String] = kafkaStream.map(_._2).map(x => {
      val strings: Array[String] = x.split(":")
      (strings(1), x)
    })*/
    val value: DStream[String] = kafkaStream.map(x => {
      val strings: Array[String] = x.split(":")
      (strings(1), x)
    }).transform(rdd => {
      /**
        * 对于scala中的option这个还不是运用的很熟练 复习
        */
      val value: RDD[(String, (String, Option[Boolean]))] = rdd.leftOuterJoin(tuples)
      value.filter(_._2._2.getOrElse(false) != true).map(x => x._2._1)
    })
    value.foreachRDD(rdd=>{
      rdd.foreachPartition(pratiton=>{
        if (pratiton.size>0){
          Class.forName("com.mysql.jdbc.Driver")
          val connect = DriverManager.getConnection("jdbc:mysql://192.168.2.130:3306/SparkStream","root","123456")
          val sql = "insert into blocksname values (?)"
          val statement: PreparedStatement = connect.prepareStatement(sql)
          pratiton.foreach( x=> {
            statement.setString(1,x)
            statement.executeLargeUpdate()
          })
          connect.close()
        }
      })
    })

    /*val sc = new SparkContext(conf)
    /**
      * 先处理 规则 黑名单 将其转化为("zs",true) 并将其广播出去
      */
    val unit: RDD[String] = sc.parallelize(List("zs","ls"))
    val tuples: Array[(String, Boolean)] = unit.map((_,true)).collect()
    val broadcast: Broadcast[Array[(String, Boolean)]] = sc.broadcast(tuples)
    /**
      * 开始流式处理数据  通过消费kafka的传来的信息
      * 传过来的数据格式为 2018,zs
      * 我们将其转换为(zs,(2018,zs))
      */
    val map = Map("metadata.broker.list"->"dshuju01:9092")
    val set = Set("blocks-topic")
    val stream = new StreamingContext(sc,Seconds(5))
    val kafkaStream: InputDStream[(String, String)] = KafkaUtils.createDirectStream[String,String,StringDecoder,StringDecoder](stream,map,set)
    kafkaStream.map(_._2)foreachRDD(rdd =>{
     rdd.foreachPartition(p => {
       Class.forName("com.mysql.jdbc.Driver")
       val connection = DriverManager.getConnection("jdbc:mysql://dshuju03:3306/SparkStream","root","123456")
       val sql = "insert into blocksname values(?)"
       val statement: PreparedStatement =connection.prepareStatement(sql)
       p.foreach(x=>{
         val strings: Array[String] = x.split(",")
         val value: Array[(String, Boolean)] = broadcast.value
         if(strings(1).equals(value(0)._1)){
         }else{
           statement.setString(1,x)
           statement.executeUpdate()
         }
       })
       connection.close()
     })
    })*/
    stream.start()
    stream.awaitTermination()
  }
}
/*
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
object kafka{


  def main(args: Array[String]): Unit = {


    val conf = new SparkConf().setAppName("UpdateStatusDemo").setMaster("spark://dshuju02:7077")
    val  stream = new StreamingContext(conf,Seconds(5))
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "localhost:9092,anotherhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "use_a_separate_group_id_for_each_stream",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val topics = Array("topicA", "topicB")
    val stream = KafkaUtils.createDirectStream[String, String](
      stream,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )
    stream.map(record => (record.key, record.value))
  }
}
*/
