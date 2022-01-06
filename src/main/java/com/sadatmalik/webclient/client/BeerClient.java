package com.sadatmalik.webclient.client;

import com.sadatmalik.webclient.domain.Beer;
import com.sadatmalik.webclient.domain.BeerPagedList;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface BeerClient {

    Mono<Beer> getBeerById(UUID id, Boolean showInventoryOnHand);

    Mono<BeerPagedList> listBeers(Integer pageNumber, Integer pageSize, String beerName,
                                  String beerStyle, Boolean showInventoryOnHand);

    Mono<ResponseEntity<Void>> createNewBeer(Beer beer);

    Mono<ResponseEntity<Void>> updateBeerById(UUID beerId, Beer beer);

    Mono<ResponseEntity<Void>> deleteBeerById(UUID id);

    Mono<Beer> getBeerByUpc(String upc);

}
