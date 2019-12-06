package cn.itheima.NetWordCount

import java.sql.{Connection, DriverManager, PreparedStatement}

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}

object TextToMysql {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("spark://dshuju02:7077").setAppName("TextToMysql")
    val streamcontex = new StreamingContext(conf,Seconds(10))
    val dstream: ReceiverInputDStream[String] = streamcontex.socketTextStream("192.168.2.129",8888)
    val unit: DStream[(String, Int)] = dstream.flatMap(_.split(" ")).map((_,1)).reduceByKey(_+_)
    unit.foreachRDD( rdd =>{
      rdd.foreachPartition( partition =>{
          Class.forName("com.mysql.jdbc.Driver")
          val connection: Connection = DriverManager.getConnection("jdbc:mysql://192.168.2.129:3306/SparkStream","root","123456")
          val sql = "insert into wordcount values(?,?)"
          var statement: PreparedStatement =null
          partition.foreach(x => {
            statement = connection.prepareStatement(sql)
            statement.setString(1,x._1)
            statement.setInt(2,x._2)
            statement.executeUpdate()
          })
          connection.close()
      })
    })
    StreamingContext.getOrCreate()
    streamcontex.start()
    streamcontex.awaitTermination()
  }
}
