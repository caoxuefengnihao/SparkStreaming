package cn.itheima.NetWordCount

import org.apache.spark.{HashPartitioner,SparkConf}
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

object UpdateStatusDemo {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("UpdateStatusDemo").setMaster("spark://dshuju02:7077")
    val  stream = new StreamingContext(conf,Seconds(5))
    /**
      * 在使用updatestateBykey的方式 必须设置 checkpoint  可以用来恢复数据
      *
      */
      stream.checkpoint("hdfs://dshuju01:9000/checkpoint")
   val value: ReceiverInputDStream[String] = stream.socketTextStream("192.168.2.129",9999)
    /**
      * String:这个是单词
      * Seq[Int] 这个是单词出现的次数
      * Option[Int] 这个是以前这个单词出现的次数
      *
      * stream.sparkContext.defaultParallelism
      */

      /*
      def updateStateByKey[S: ClassTag](
      updateFunc: (Iterator[(K, Seq[V], Option[S])]) => Iterator[(K, S)],
      partitioner: Partitioner,
      rememberPartitioner: Boolean): DStream[(K, S)] = ssc.withScope {
      val cleanedFunc = ssc.sc.clean(updateFunc)
      val newUpdateFunc = (_: Time, it: Iterator[(K, Seq[V], Option[S])]) => {
      cleanedFunc(it)
     }
      new StateDStream(self, newUpdateFunc, partitioner, rememberPartitioner, None)
   }

       */
    val updateFunc = (it: (Iterator[(String, Seq[Int], Option[Int])]))=>{
     it.map(x=>{
        val m = x._2.sum + x._3.getOrElse(0)
       (x._1,m)
     })
    }
    value.flatMap(_.split(" ")).map((_,1)).updateStateByKey(updateFunc,new HashPartitioner(stream.sparkContext.defaultParallelism),true).print()
    stream.start()
    stream.awaitTermination()
  }
}
