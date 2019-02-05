package stroom.benchmark;

import stroom.job.api.ScheduledJobsModule;
import stroom.job.api.TaskConsumer;
import stroom.pipeline.destination.RollingDestinations;

import javax.inject.Inject;

import static stroom.job.api.Schedule.ScheduleType.CRON;
import static stroom.job.api.Schedule.ScheduleType.PERIODIC;

public class BenchmarkJobsModule extends ScheduledJobsModule {
    @Override
    protected void configure() {
        super.configure();
        bindJob()
                .name("XX Benchmark System XX")
                .description("Job to generate data in the system in order to benchmark it's performance (do not run in live!!)")
                .schedule(CRON, "* * *")
                .to(BenchmarkSystem.class);
    }

    private static class BenchmarkSystem extends TaskConsumer {
        @Inject
        BenchmarkSystem(final BenchmarkClusterExecutor benchmarkClusterExecutor) {
            super(benchmarkClusterExecutor::exec);
        }
    }
}
