package com.paymentchain.customer.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.paymentchain.customer.entities.Customer;
import com.paymentchain.customer.entities.CustomerProduct;
import com.paymentchain.customer.repository.CustomerRepository;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/customer")
public class CustomerRestController {

    @Autowired
    CustomerRepository customerRepository;

    private final WebClient.Builder webClientBuilder;

    public CustomerRestController(WebClient.Builder webClientBuilder){
        this.webClientBuilder=webClientBuilder;
    }

    HttpClient client= HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000)
            .option(ChannelOption.SO_KEEPALIVE,true)
            .option(EpollChannelOption.TCP_KEEPIDLE,300)
            .option(EpollChannelOption.TCP_KEEPINTVL,60)
            .responseTimeout(Duration.ofSeconds(1))
            .doOnConnected(connected->{
                        connected.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
                        connected.addHandlerLast(new WriteTimeoutHandler(5000,TimeUnit.MILLISECONDS));
            });



    @GetMapping()
    public List<Customer> findAll(){
        return customerRepository.findAll();
    }

    @GetMapping("/{id}")
    public Customer get(@PathVariable(name = "id") long id) {
        return customerRepository.findById(id).get();

    }

    @PutMapping("/{id}")
    public ResponseEntity<?> put(@PathVariable long id, @RequestBody Customer input){
        Customer save= customerRepository.save(input);
        return ResponseEntity.ok(save);
    }

    @PostMapping()
    public ResponseEntity<?> post(@RequestBody Customer input){
        input.getProducts().forEach(x-> x.setCustomer(input));
        Customer save=customerRepository.save(input);
        return ResponseEntity.ok(save);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable long id){
        Optional<Customer> findById = customerRepository.findById(id);
        if (findById.get() != null){
            customerRepository.delete(findById.get());
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/full")
    public Customer getByCode(@RequestParam(name = "code") String code){
        Customer customer=customerRepository.findByCode(code);
        List<CustomerProduct> products = customer.getProducts();
        products.forEach( x->{
            String productName = getProductName(x.getId());
            x.setProductName(productName);
        });
        return customer;
    }

    private String getProductName(long id){
        WebClient build = webClientBuilder.clientConnector(new ReactorClientHttpConnector(client))
                .baseUrl("http://localhost:8083/product")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultUriVariables(Collections.singletonMap("url","http://localhost:8083/product"))
                .build();
        JsonNode block = build.method(HttpMethod.GET).uri("/"+id)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        String name = block.get("name").asText();
        return name;
    }

}
