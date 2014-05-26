package com.github.hiroshi_cl.benchmark.sample

import JavaSamples._
import ScalaSamples._

object Main {
  def main(a: Array[String]) {
    val rep_small_start = 30
    val rep_small_goal = 50
    val rep_large_start = 60
    val rep_large_goal = 80
    var isSmall = false
    var bench: RepeatingBenchMark = null
    a(0) match {
      case "Sqrt" =>
        bench = new Sqrt()
      case "SqrtBoxed" =>
        bench = new SqrtBoxed()
      case "SqrtMethodHandle" =>
        bench = new SqrtMethodHandle()
      case "SqrtCompletableFuture" =>
        isSmall = true
        bench = new SqrtCompletableFuture()
      case "SqrtExecutorServise" =>
        isSmall = true
        bench = new SqrtExecutorServise()
      case "SqrtThread" =>
        isSmall = true
        bench = new SqrtThread()
      case "Square" =>
        bench = new Square()
      case "SqrtScala" =>
        bench = SqrtScala
      case "SqrtScalaFuture" =>
        isSmall = true
        bench = SqrtScalaFuture
      case "SqrtScalaFutureFlatMap" =>
        isSmall = true
        bench = SqrtScalaFutureFlatMap
      case "SqrtScalaFutureAwait" =>
        isSmall = true
        bench = SqrtScalaFutureAwait
      case _ =>
        throw null
    }
    println(a(0))
    val start = if (isSmall) rep_small_start else rep_large_start
    val goal = if (isSmall) rep_small_goal else rep_large_goal
    for (i <- start to goal) yield {
      bench.setRep(Math.pow(10.0, i * .1).round.asInstanceOf[Int])
      bench.run()
      printf("%.2f\t%.2f\t%.2f\t%.2f\t%.2f\n", i * .1,
        Math.log10(bench.getTime), Math.log10(bench.getSysTime), Math.log10(bench.getUsrTime),
        Math.log10(bench.getMemory))
    }
  }
}