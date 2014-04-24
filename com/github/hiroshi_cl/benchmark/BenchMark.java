package com.github.hiroshi_cl.benchmark;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.ThreadMXBean;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public abstract class BenchMark implements Runnable {
    private static final byte[] DefaultBytes = "csg".getBytes();
    private static final int DefaultWarmupRun = 5;
    private static final int DefaultMeasuringRun = 10;
    private static final int DefaultWait = 100;

    private final byte[] bytes;
    private final int warmupRun;
    private final int measuringRun;
    private final int wait;

    private static final ThreadMXBean threadMXB = ManagementFactory.getThreadMXBean();
    private static final MemoryPoolMXBean memoryPoolMXB = ManagementFactory.getMemoryPoolMXBeans().get(0);

    static {
        threadMXB.setThreadCpuTimeEnabled(true);
    }

    public BenchMark() {
        this(DefaultBytes, DefaultWarmupRun, DefaultMeasuringRun, DefaultWait);
    }

    public BenchMark(final byte[] bytes, final int warmupRun, final int measuringRun, final int wait) {
        this.bytes = bytes;
        this.warmupRun = warmupRun;
        this.measuringRun = measuringRun;
        this.wait = wait;
    }

    // template
    public abstract int testCase(final int nxt);

    private Double time = null; // actual time
    private Double memory = null; // memory (not so precise)
    private Double sysTime = null; // system time
    private Double usrTime = null; // user time
    private Double eSysTime = null; // system time of the executor
    private Double eUsrTime = null; // user time of the executor

    public final double getTime() {
        return time;
    }

    public final double getMemory() {
        return memory;
    }

    public final double getSysTime() {
        return sysTime;
    }

    public final double getUsrTime() {
        return usrTime;
    }

    public final double getEUsrTime() {
        return eUsrTime;
    }

    public final double getESysTime() {
        return eSysTime;
    }

    // multi thread support
    private static long thID = -1;
    private static final ExecutorService es;

    static {
        es = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                final Thread th = Executors.defaultThreadFactory().newThread(r);
                th.setDaemon(true);
                th.setName("bench");
//                thID = th.getId();
                return th;
            }
        });
        es.submit(new Runnable() {
            @Override
            public void run() {

            }
        });
        for (final long id : threadMXB.getAllThreadIds())
            if (threadMXB.getThreadInfo(id).getThreadName().equals("bench"))
                thID = id;
    }

    protected final ExecutorService getEs() {
        return es;
    }


    // test run
    @Override
    public final void run() {
        final Random rnd = random();

        // warm up
        int tmp = 0;
        for (int i = 0; i < warmupRun; i++) {
            gc();
            final int nxt = rnd.nextInt();
            final int res = testCase(nxt);
            tmp ^= res;
        }

        // measuring
        final double[] times = new double[measuringRun];
        final double[] syss = new double[measuringRun];
        final double[] usrs = new double[measuringRun];
        final double[] mems = new double[measuringRun];
        final double[] esyss = new double[measuringRun];
        final double[] eusrs = new double[measuringRun];
        for (int i = 0; i < measuringRun; i++) {
            gc();
            final int nxt = rnd.nextInt();
            memoryPoolMXB.resetPeakUsage();
            final long startMem = memoryPoolMXB.getPeakUsage().getUsed();
            final long startEcpu = threadMXB.getThreadCpuTime(thID);
            final long startEusr = threadMXB.getThreadUserTime(thID);
            final long startCpu = threadMXB.getCurrentThreadCpuTime();
            final long startUsr = threadMXB.getCurrentThreadUserTime();
            final long start = System.nanoTime();
            final int res = testCase(nxt);
            final long time = System.nanoTime() - start;
            final long cpu = threadMXB.getCurrentThreadCpuTime() - startCpu;
            final long usr = threadMXB.getCurrentThreadUserTime() - startUsr;
            final long ecpu = threadMXB.getThreadCpuTime(thID) - startEcpu;
            final long eusr = threadMXB.getThreadUserTime(thID) - startEusr;
            final long mem = memoryPoolMXB.getPeakUsage().getUsed() - startMem;
            times[i] = time;
            syss[i] = cpu - usr;
            usrs[i] = usr;
            mems[i] = mem;
            esyss[i] = ecpu - eusr;
            eusrs[i] = eusr;
            tmp ^= res;
        }

        // summary
        System.err.println("\t\tmin\t\tmed\t\tmax");
        // print and substitute medians
        this.time = print(times, "Act(s)", 1e-9);
        this.sysTime = print(syss, "Sys(s)", 1e-9);
        this.usrTime = print(usrs, "Usr(s)", 1e-9);
        this.eSysTime = print(esyss, "ESys(s)", 1e-9);
        this.eUsrTime = print(eusrs, "EUsr(s)", 1e-9);
        this.memory = print(mems, "Mem(MB)", 1e-6);
        System.err.println(tmp);
    }

    private void gc() {
        System.gc();

        // wait for gc
        try {
            Thread.sleep(wait);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private double print(final double[] a, final String name, final double scale) {
        Arrays.sort(a);
        final double min = a[0] * scale;
        final double med = (a[measuringRun / 2] + a[(measuringRun + 1) / 2]) * .5 * scale;
        final double max = a[measuringRun - 1] * scale;
        System.err.printf("%s\t%.3f\t%.3f\t%.3f\n", name, min, med, max);
        return med;
    }

    private Random random() {
        try {
            final SecureRandom sc = SecureRandom.getInstance("SHA1PRNG");
            sc.setSeed(bytes);
            return sc;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
