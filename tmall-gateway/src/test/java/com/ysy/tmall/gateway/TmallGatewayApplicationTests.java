package com.ysy.tmall.gateway;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootTest
class TmallGatewayApplicationTests {

    @Test
    void contextLoads() {
        CompletableFuture<String> stringCompletableFuture = CompletableFuture.supplyAsync(() -> 1).handle((a,b) -> "1");

        CompletableFuture<String> stringCompletableFuture2 = CompletableFuture.supplyAsync(() -> 1).thenApply((a) -> "1");

        CompletableFuture.supplyAsync(() -> 1).thenAccept((a) -> System.out.println(1));
        List<Integer> together = Stream.of(Arrays.asList(1, 2), Arrays.asList(3, 4)) // Stream of List<Integer>
                .flatMap(List::stream)
                .map(integer -> integer + 1)
                .collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(2, 3, 4, 5), together);


        Stream<Stream<Integer>> streamStream = Stream.of(Arrays.asList(1, 2), Arrays.asList(3, 4)) // Stream of List<Integer>
                .map(List::stream);

        streamStream.forEach(s -> s.forEach(s1 -> System.out.println(s1)));

    }

}
