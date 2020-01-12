package com.kozhekin.transferring.datastore;

import com.kozhekin.transferring.AbstractTest;
import com.kozhekin.transferring.category.SlowTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class InMemoryIdGeneratorTest extends AbstractTest {

    private IdGenerator generator;
    private ExecutorService exec;

    @Before
    public void init() {
        generator = new InMemoryIdGenerator();
        exec = Executors.newFixedThreadPool(threads);
    }

    @After
    public void destroy() {
        exec.shutdown();
    }

    @Test
    @Category(SlowTest.class)
    public void next() throws InterruptedException, ExecutionException {
        Assert.assertEquals("First value is 0", 0, generator.next());
        List<Future<?>> tasks = new ArrayList<>(threads);
        for (int i = 0; i < threads; ++i) {
            tasks.add(exec.submit(() -> {
                int prev = 0;
                int next;
                while ((next = generator.next()) > 0) {
                    Assert.assertTrue("Next value greater then previous", prev < next);
                    prev = next;
                }
                Assert.assertEquals("-1 when overloaded", -1, next);
            }));
        }
        checkErrors(tasks);
    }
}