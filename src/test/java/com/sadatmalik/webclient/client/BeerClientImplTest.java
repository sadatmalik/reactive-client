package com.sadatmalik.webclient.client;

import com.sadatmalik.webclient.config.WebClientConfig;
import com.sadatmalik.webclient.domain.Beer;
import com.sadatmalik.webclient.domain.BeerPagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BeerClientImplTest {

    BeerClientImpl beerClient;

    @BeforeEach
    void setUp() {
        beerClient = new BeerClientImpl(new WebClientConfig().webClient());
    }

    @Test
    void listBeers() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(null, null,
                null, null, null);

        BeerPagedList beerPagedList = beerPagedListMono.block();

        assertThat(beerPagedList).isNotNull();
        assertThat(beerPagedList.getContent().size()).isGreaterThan(0);

        System.out.println(beerPagedList.toList());
    }

    @Test
    void listBeersPageSize10() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(1, 10,
                null, null, null);

        BeerPagedList beerPagedList = beerPagedListMono.block();

        assertThat(beerPagedList).isNotNull();
        assertThat(beerPagedList.getContent().size()).isEqualTo(10);
    }

    @Test // only have about 30 beers loaded so page 10 at 20 a page should not have any records
    void listBeersNoRecords() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(10, 20,
                null, null, null);

        BeerPagedList beerPagedList = beerPagedListMono.block();

        assertThat(beerPagedList).isNotNull();
        assertThat(beerPagedList.getContent().size()).isEqualTo(0);
    }

    @Disabled("API returning inventory when should not be")
    @Test
    void getBeerById() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(null, null,
                null, null, null);

        BeerPagedList beerPagedList = beerPagedListMono.block();
        Optional<Beer> beerOptional = beerPagedList.stream().findFirst();
        UUID beerId = beerOptional.get().getId();

        Mono<Beer> beerMono = beerClient.getBeerById(beerId, false);
        Beer beer = beerMono.block();

        assertThat(beer).isNotNull();
        assertThat(beer.getId()).isEqualTo(beerId);
        assertThat(beer.getQuantityOnHand()).isNull();
        System.out.println(beer);
    }

    @Test
    void functionalTestGetBeerById() throws InterruptedException {
        AtomicReference<String> beerName = new AtomicReference<>();
        CountDownLatch countDownLatch = new CountDownLatch(1); // very useful in testing - instead of Thread.sleep

        beerClient.listBeers(null, null,
                null, null, null)
                .map(beerPagedList -> beerPagedList.stream().findFirst().get().getId())
                .map(beerId -> beerClient.getBeerById(beerId, false))
                .flatMap(mono -> mono)
                .subscribe(beer -> {
                    System.out.println(beer.getBeerName());
                    beerName.set(beer.getBeerName());
                    countDownLatch.countDown();
                });

        countDownLatch.await();
        assertThat(beerName.get()).isEqualTo("Mango Bobs");
    }

        @Test
    void getBeerByIdShowInventoryTrue() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(null, null,
                null, null, true);

        BeerPagedList beerPagedList = beerPagedListMono.block();
        Optional<Beer> beerOptional = beerPagedList.stream().findFirst();
        UUID beerId = beerOptional.get().getId();

        Mono<Beer> beerMono = beerClient.getBeerById(beerId, true);
        Beer beer = beerMono.block();

        assertThat(beer).isNotNull();
        assertThat(beer.getId()).isEqualTo(beerId);
        assertThat(beer.getQuantityOnHand()).isNotNull();
        System.out.println(beer);
    }

    @Test
    void getBeerByUpc() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(null, null,
                null, null, null);

        BeerPagedList beerPagedList = beerPagedListMono.block();
        Optional<Beer> beerOptional = beerPagedList.stream().findFirst();
        String beerUpc = beerOptional.get().getUpc();

        Mono<Beer> beerMono = beerClient.getBeerByUpc(beerUpc);
        Beer beer = beerMono.block();

        assertThat(beer).isNotNull();
        assertThat(beer.getUpc()).isEqualTo(beerUpc);
        System.out.println(beer);
    }

    @Test
    void createNewBeer() {
        Beer beer = Beer.builder()
                .beerName("Dogfishhead 90 Min IPA")
                .beerStyle("IPA")
                .upc("234848549559")
                .price(new BigDecimal("10.99"))
                .build();

        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.createNewBeer(beer);

        ResponseEntity responseEntity = responseEntityMono.block();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void updateBeerById() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(null, null,
                null, null, null);

        BeerPagedList beerPagedList = beerPagedListMono.block();
        Optional<Beer> beerOptional = beerPagedList.stream().findFirst();
        Beer beer = beerOptional.get();

        Beer updatedBeer = Beer.builder()
                .beerName("Really Good Beer")
                .beerStyle(beer.getBeerStyle())
                .price(beer.getPrice())
                .upc(beer.getUpc())
                .build();

        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.updateBeerById(beer.getId(), updatedBeer);
        ResponseEntity<Void> responseEntity = responseEntityMono.block();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void deleteBeerByIdHandleException() {
        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.deleteBeerById(UUID.randomUUID());
        ResponseEntity<Void> responseEntity = responseEntityMono.onErrorResume(throwable -> {
            if (throwable instanceof WebClientResponseException) {
                WebClientResponseException exception = (WebClientResponseException) throwable;
                return Mono.just(ResponseEntity.status(exception.getStatusCode()).build());
            } else {
                throw new RuntimeException(throwable);
            }
        }).block();

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

        @Test
    void deleteBeerByIdNotFound() {
        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.deleteBeerById(UUID.randomUUID());
        assertThrows(WebClientResponseException.class, () -> {
            ResponseEntity<Void> responseEntity = responseEntityMono.block();
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        });
    }

    @Test
    void deleteBeerById() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(null, null,
                null, null, null);

        BeerPagedList beerPagedList = beerPagedListMono.block();
        Optional<Beer> beerOptional = beerPagedList.stream().findFirst();
        Beer beer = beerOptional.get();

        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.deleteBeerById(beer.getId());
        ResponseEntity<Void> responseEntity = responseEntityMono.block();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}