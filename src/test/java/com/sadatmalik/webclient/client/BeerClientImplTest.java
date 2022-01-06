package com.sadatmalik.webclient.client;

import com.sadatmalik.webclient.config.WebClientConfig;
import com.sadatmalik.webclient.domain.Beer;
import com.sadatmalik.webclient.domain.BeerPagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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