package ru.job4j.waitnotify;

import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SimpleBlockingQueueTest {

    private static class ThreadProducer implements Runnable {

        private final  SimpleBlockingQueue<Integer> simpleBlockingQueue;

        private ThreadProducer(SimpleBlockingQueue<Integer> simpleBlockingQueue) {
            this.simpleBlockingQueue = simpleBlockingQueue;
        }

        @Override
        public void run() {
                try {
                    for (int i = 0; i < 3; i++) {
                        simpleBlockingQueue.offer(i);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
    }

    private static class ThreadConsumer implements Runnable {

        private final SimpleBlockingQueue<Integer> simpleBlockingQueue;
        private final List<Integer> elements = new ArrayList<>();

        private ThreadConsumer(SimpleBlockingQueue<Integer> simpleBlockingQueue) {
            this.simpleBlockingQueue = simpleBlockingQueue;
        }

        @Override
        public void run() {
                try {
                   for (int i = 0; i < 3; i++) {
                       elements.add(simpleBlockingQueue.poll());
                   }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }

        public List<Integer> getElements() {
            return elements;
        }
    }

    @Test
    public void whenTwoThreads() throws InterruptedException {
        SimpleBlockingQueue<Integer> simpleBlockingQueue = new SimpleBlockingQueue<Integer>(3);
        ThreadConsumer threadConsumer = new ThreadConsumer(simpleBlockingQueue);
        Thread producer = new Thread(new ThreadProducer(simpleBlockingQueue));
        Thread consumer = new Thread(threadConsumer);
        producer.start();
        consumer.start();
        producer.join();
        consumer.join();
        assertThat(threadConsumer.getElements().get(1), is(1));
    }

    @Test
    public void whenFetchAllThenGetIt() throws InterruptedException {
        final List<Integer> buffer = new ArrayList<>();
        final SimpleBlockingQueue<Integer> queue = new SimpleBlockingQueue<>(5);
        Thread producer = new Thread(
                () -> {
                    for (int i = 0; i < 5; i++) {
                        try {
                            queue.offer(i);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
        producer.start();
        Thread consumer = new Thread(
                () -> {
                    while (!queue.isEmpty() || !Thread.currentThread().isInterrupted()) {
                        try {
                            buffer.add(queue.poll());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            Thread.currentThread().interrupt();
                        }
                    }
                }
        );
        consumer.start();
        producer.join();
        consumer.interrupt();
        consumer.join();
        assertThat(buffer, is(Arrays.asList(0, 1, 2, 3, 4)));
    }
}