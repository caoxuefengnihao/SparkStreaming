package cn.itheima.NetWordCount


import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.{SparkConf, TaskContext}
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010._

object SparkAndKafka01 {


  def main(args: Array[String]): Unit = {
    val context = new StreamingContext(new SparkConf().setMaster("local[8]").setAppName("SparkAndKafka01"),Seconds(5))
    //消费者配置
    val kafkaParam = Map(
      "bootstrap.servers" ->"dshuju01:9092,dshuju02:9092,dshuju03:9092",//用于初始化链接到集群的地址
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      //用于标识这个消费者属于哪个消费团体
      "group.id" -> "sparkafkaGroup",
      //如果没有初始化偏移量或者当前的偏移量不存在任何服务器上，可以使用这个配置属性
      //可以使用这个配置，latest自动重置偏移量为最新的偏移量
      "auto.offset.reset" -> "latest",
      //如果是true，则这个消费者的偏移量会在后台自动提交
      "enable.auto.commit" -> (false: java.lang.Boolean)
    );
    /**
      * K：type of Kafka message key
      * V：type of Kafka message value
      */
     val value: ConsumerStrategy[String, String] = ConsumerStrategies.Subscribe[String,String](Array("sourceSpark"),kafkaParam)
     val inputStream: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream[String,String](context,LocationStrategies.PreferConsistent,value)
     val unit: DStream[String] = inputStream.map(x=>{x.value()})
     unit.print()
    /**
      * 消费完之后不要忘记手动提交offset
      */
    unit.foreachRDD { rdd =>
      val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
      rdd.foreachPartition { iter =>
        val o: OffsetRange = offsetRanges(TaskContext.get.partitionId)
        println(s"${o.topic} ${o.partition} ${o.fromOffset} ${o.untilOffset}")
      }
      unit.asInstanceOf[CanCommitOffsets].commitAsync(offsetRanges)
    }
    context.start()
    context.awaitTermination()
  }

}
