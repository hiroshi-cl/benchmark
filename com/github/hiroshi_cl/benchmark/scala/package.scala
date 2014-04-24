package com.github.hiroshi_cl.benchmark

import _root_.scala.concurrent.ExecutionContext

package object scala {

  import com.github.hiroshi_cl.benchmark._

  def benchmark(f: Int => Int): (Double, Double, Double, Double, Double, Double) = {
    val bm = new BenchMark() {
      override def testCase(nxt: Int): Int = f(nxt)
    }
    bm.run
    (bm.getTime, bm.getSysTime, bm.getUsrTime, bm.getMemory, bm.getESysTime, bm.getEUsrTime)
  }

  def benchmark(f: (ExecutionContext, Int) => Int): (Double, Double, Double, Double, Double, Double) = {
    val bm = new BenchMark() {
      override def testCase(nxt: Int): Int = f(ExecutionContext.fromExecutorService(getEs), nxt)
    }
    bm.run
    (bm.getTime, bm.getSysTime, bm.getUsrTime, bm.getMemory, bm.getESysTime, bm.getEUsrTime)
  }

}
