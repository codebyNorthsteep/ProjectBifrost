package org.example.projectbifrost;

import org.springframework.boot.SpringApplication;

public class TestProjectBifrostApplication {

    public static void main(String[] args) {
        SpringApplication.from(ProjectBifrostApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
