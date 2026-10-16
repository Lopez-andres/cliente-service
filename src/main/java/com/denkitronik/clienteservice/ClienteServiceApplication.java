package com.denkitronik.clienteservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
// Serializa Page como { content: [...], page: { size, number, totalElements, totalPages } }
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class ClienteServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClienteServiceApplication.class, args);
    }

}
