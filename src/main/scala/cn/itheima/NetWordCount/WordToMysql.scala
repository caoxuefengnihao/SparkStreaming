package cn.itheima.NetWordCount

import java.sql.{Connection, DriverManager, PreparedStatement}

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}

object WordToMysql {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("WordToMysql").setMaster("spark://dshuju02:7077")
    val streamcontex = new StreamingContext(conf,Seconds(10))
    val dstream: ReceiverInputDStream[String] = streamcontex.socketTextStream("192.168.2.129",41414)
    val unit: DStream[(String, Int)] = dstream.flatMap(_.split(" ")).map((_,1)).reduceByKey(_+_)
    unit.foreachRDD( rdd =>{
      rdd.foreachPartition( partition =>{
        Class.forName("com.mysql.jdbc.Driver")
        val connection: Connection = DriverManager.getConnection("jdbc:mysql://192.168.2.130:3306/SparkStream","root","123456")
        val sql = "insert into wordcount values(?,?)"
        val statement: PreparedStatement =connection.prepareStatement(sql)
        partition.foreach(x => {
          statement.setString(1,x._1)
          statement.setInt(2,x._2)
          statement.executeUpdate()
        })
        connection.close()
      })
    })
    /*unit.foreachRDD { rdd =>
      rdd.foreachPartition { partitionOfRecords =>{   //partitionOfRecords是整个分区的数据
        Class.forName("com.mysql.jdbc.Driver")
        val connection = DriverManager.getConnection("jdbc:mysql://dshuju03:3306/SparkStream","root","123456")
        partitionOfRecords.foreach(record =>{     //record这个record才是每一条数据
          val sql=" insert into wordcount values('" + record._1 + "'," + record._2 + ")"
          connection.createStatement().execute(sql)
        })
        connection.close()
        }
      }
    }*/
    unit.print()
    streamcontex.start()
    streamcontex.awaitTermination()
  }

}
