package com.paymentchain.product.controller;

import com.paymentchain.product.entities.Product;
import com.paymentchain.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/product")
public class ProductRestController {
    @GetMapping("/hello")
    public String welcome(){
        return "Hello product";
    }

    @Autowired
    ProductRepository productRepository;

    @GetMapping()
    public List<Product> findAll(){
        return productRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable long id){
        Optional<Product> customer = productRepository.findById(id);
        if (customer.isPresent()){
            return new ResponseEntity<>(customer.get(), HttpStatus.OK);
        }else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> put(@PathVariable long id,@RequestBody Product input){
        Optional<Product> optionalCustomer = productRepository.findById(id);
        if(optionalCustomer.isPresent()){
            Product newCustomer = optionalCustomer.get();
            newCustomer.setName(input.getName());
            newCustomer.setCode(input.getCode());
            Product save = productRepository.save(newCustomer);
            return new ResponseEntity<>(save, HttpStatus.OK);
        }else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<?> post(@RequestBody Product input){
        Product save=productRepository.save(input);
        return ResponseEntity.ok(save);
    }
}
