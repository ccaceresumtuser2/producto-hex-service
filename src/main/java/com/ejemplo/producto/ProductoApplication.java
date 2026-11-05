package com.ejemplo.producto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación.
 * <p>
 * {@code @SpringBootApplication} activa la configuración automática de Spring Boot y busca componentes
 * (controladores, adaptadores, configuración) en este paquete y sus subpaquetes.
 * Los casos de uso del núcleo no se detectan solos: se registran en {@code config.BeanConfig}.
 */
@SpringBootApplication
public class ProductoApplication {

    /** Arranca el servidor web embebido (Tomcat) y el contexto de Spring. */
    public static void main(String[] args) {
        SpringApplication.run(ProductoApplication.class, args);
    }
}
