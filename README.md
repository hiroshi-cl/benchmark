benchmark
=========

概要
---------

* Java または Scala でベンチマークができます。
* 実時間、CPU時間、ユーザー時間、システム時間、スレッドプールの合計CPU時間、ピーク消費メモリ等が測れます。
* 反復測定にも対応しています。
* JIT が安定するまでの最初の数回を捨てる機能もついています。
* JIT に消されることを防止するため、入力を擬似乱数列を用いて変化させることと、返り値を受け取ることを必須としています。

注意点
---------
* Summary で表示されるのは平均値ではなく、**中央値**です。
* 時間が短すぎると、ピーク消費メモリは 0 にしかならない可能性が高いです。(精度自体あまりよくないです。)

サンプル
---------

* Math.sqrt を用いたオーバーヘッド測定
* URL: https://github.com/hiroshi-cl/benchmark/tree/master/com/github/hiroshi_cl/benchmark/sample
* 上を用いた参考測定データ

|言語  |項目                           |測定値 [ns]|
|------|-------------------------------|----------:|
|Java  |(x*x)                          |          1|
|Java  |直接計算                       |          3|
|Scala |直接計算                       |          3|
|Java  |MethodHandle                   |          5|
|Java  |Double (boxed)                 |          5|
|Java  |CompletableFuture              |        400|
|Scala |concurrent.Future (map)        |        750|
|Scala |concurrent.Future (flatMap)    |        750|
|Scala |concurrent.Future (同期で待つ) |     13,000|
|Java  |ExecutorService                |     13,000|
|Java  |Thread                         |     50,000|
