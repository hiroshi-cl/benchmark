package com.github.hiroshi_cl.benchmark.sample

import scala.concurrent.{ExecutionContext, Await, Future}
import scala.concurrent.duration.Duration

object ScalaSamples {

  import com.github.hiroshi_cl.benchmark.sample.JavaSamples._

  object SqrtScala extends RepeatingBenchMark {
    override def testCase(nxt: Int) = {
      var d: Double = nxt
      for (i <- 1 to rep)
        d = Math.sqrt(d)
      d.hashCode()
    }
  }

  object SqrtScalaFuture extends RepeatingBenchMark {
    override def testCase(nxt: Int) = {
      implicit val ec = ExecutionContext.fromExecutorService(getEs)
      var d = Future(nxt.asInstanceOf[Double])
      for (i <- 1 to rep)
        d = d.flatMap {
          dd => Future(Math.sqrt(dd))
        }
      Await.result(d, Duration.Inf).hashCode()
    }
  }

  object SqrtScalaFutureFlatMap extends RepeatingBenchMark {
    override def testCase(nxt: Int) = {
      implicit val ec = ExecutionContext.fromExecutorService(getEs)
      var d = Future(nxt.asInstanceOf[Double])
      for (i <- 1 to rep)
        d = d.flatMap {
          dd => Future(Math.sqrt(dd))
        }
      Await.result(d, Duration.Inf).hashCode()
    }
  }

  object SqrtScalaFutureAwait extends RepeatingBenchMark {
    override def testCase(nxt: Int) = {
      implicit val ec = ExecutionContext.fromExecutorService(getEs)
      var d = nxt.asInstanceOf[Double]
      for (i <- 1 to rep)
        d = Await.result(Future(Math.sqrt(d)), Duration.Inf)
      d.hashCode()
    }
  }

}
