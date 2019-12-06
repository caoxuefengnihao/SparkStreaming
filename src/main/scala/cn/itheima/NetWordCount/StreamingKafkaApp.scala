/*package cn.itheima.NetWordCount
import java.sql.{Connection, DriverManager, PreparedStatement}
import kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka.{ KafkaUtils}
/**
  * SparkStreaming + flume + kafka + Logger 实现 数字相加
  *
  *
  */
object StreamingKafkaApp {
  def main(args: Array[String]): Unit = {
    val con = new SparkConf().setAppName("StreamingKafkaApp").setMaster("spark://dshuju02:7077")
    val stream = new StreamingContext(con,Seconds(5))
   /* var offsetRanges :Array[OffsetRange] = null*/
    /**
      * In the streaming application code, import KafkaUtils and create an input DStream as follows.
      *
      */
    val map = Map[String,String]("metadata.broker.list"->"dshuju02:9092")
    val set = Set("streamingtopictomysql")
    val kafkaStream: InputDStream[(String, String)] = KafkaUtils.createDirectStream[String,String,StringDecoder,StringDecoder](stream,map,set)
    //TODO  kafkaStream.map(_._2)这个为什么是第二个呢
    val value: DStream[String] = kafkaStream.map(_._2)
   /* value.transform( rdd=>{
      offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
      rdd
    })*/
      value.flatMap(_.split(" ")).map((_,1)).reduceByKey(_+_)
      .foreachRDD( rdd =>{

        //val sql = "insert into table offset values (?,?,?,?)"
       // val statement: PreparedStatement= connection.prepareStatement(sql)
       /* for (elem <- offsetRanges) {
          if (elem != null){
             statement.setString(1,elem.topic)
             statement.setInt(2,elem.partition)
             statement.setLong(3,elem.fromOffset)
             statement.setLong(4,elem.untilOffset)
            statement.executeUpdate()
            print(s"${elem.topic}  ${elem.partition} ${elem.fromOffset} ${elem.untilOffset}")
          }
        }*/
        rdd.foreachPartition( it =>{
          Class.forName("com.mysql.jdbc.Driver")
          val connection: Connection = DriverManager.getConnection("jdbc:mysql://dshuju03:3306/SparkStream","root","123456")
          val sql2 = "insert into table wordcount values(?,?)"
          val statement1 :PreparedStatement = connection.prepareStatement(sql2)
          it.foreach(x =>{
            statement1.setString(1,x._1)
            statement1.setInt(2,x._2)
            statement1.executeUpdate()
          })
        })
      })
    stream.start()
    stream.awaitTermination()
  }
}*/
