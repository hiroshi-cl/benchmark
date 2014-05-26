package com.github.hiroshi_cl.benchmark.sample;

import com.github.hiroshi_cl.benchmark.BenchMark;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.concurrent.*;

public class JavaSamples {
    public static abstract class RepeatingBenchMark extends BenchMark {
        protected int rep = 1;

        public void setRep(final int rep) {
            this.rep = rep;
        }
    }

    public static class Sqrt extends RepeatingBenchMark {
        @Override
        public int testCase(int nxt) {
            double d = nxt;
            for (int i = 0; i < rep; i++)
                d = Math.sqrt(d);

            return Double.hashCode(d);
        }
    }

    public static class SqrtBoxed extends RepeatingBenchMark {
        @Override
        public int testCase(int nxt) {
            Double d = (double) nxt;
            for (int i = 0; i < rep; i++)
                d = Math.sqrt(d);

            return Double.hashCode(d);
        }
    }

    public static class SqrtMethodHandle extends RepeatingBenchMark {

        @Override
        public void setRep(final int rep) {
            super.setRep(rep);

            try {
                final MethodHandle sqrt = MethodHandles.lookup().findStatic
                        (Math.class, "sqrt", MethodType.methodType(double.class, double.class));
                MethodHandle mh = sqrt;
                for (int i = Integer.highestOneBit(rep) >> 1; i > 0; i >>= 1) {
                    mh = MethodHandles.filterReturnValue(mh, mh);
                    if ((rep | i) > 0)
                        mh = MethodHandles.filterReturnValue(mh, sqrt);
                }
                task = mh;
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }

        private MethodHandle task;

        @Override
        public int testCase(int nxt) {
            try {
                final double d = (Double) task.invoke((double) nxt);
                return Double.hashCode(d);
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }
    }

    public static class SqrtCompletableFuture extends RepeatingBenchMark {
        @Override
        public int testCase(int nxt) {
            try {
                CompletableFuture<Double> f = CompletableFuture.completedFuture((double) nxt);
                for (int i = 0; i < rep; i++)
                    f = f.thenApplyAsync(d -> Math.sqrt(d));
                return f.get().hashCode();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class SqrtExecutorServise extends RepeatingBenchMark {
        @Override
        public int testCase(int nxt) {
            try {
                final ExecutorService ex = getEs();
                double d = nxt;
                for (int i = 0; i < rep; i++) {
                    final double dd = d;
                    final Future<Double> f = ex.submit(new Callable<Double>() {
                        @Override
                        public Double call() throws Exception {
                            return Math.sqrt(dd);
                        }
                    });
                    d = f.get();
                }
                return Double.hashCode(d);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class SqrtThread extends RepeatingBenchMark {

        private double res = 0.;

        @Override
        public int testCase(int nxt) {
            try {
                double d = nxt;
                for (int i = 0; i < rep; i++) {
                    final double dd = d;
                    final Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            res = Math.sqrt(dd);
                        }
                    });
                    t.start();
                    t.join();
                    d = res;
                }
                return Double.hashCode(d);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class Square extends RepeatingBenchMark {
        @Override
        public int testCase(int nxt) {
            for (int i = 0; i < rep; i++)
                nxt *= nxt;
            return nxt;
        }
    }
}
