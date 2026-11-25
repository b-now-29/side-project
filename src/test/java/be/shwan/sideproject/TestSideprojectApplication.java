package be.shwan.sideproject;

import org.springframework.boot.SpringApplication;

public class TestSideprojectApplication {

  public static void main(String[] args) {
    SpringApplication.from(SideprojectApplication::main)
        .with(TestcontainersConfiguration.class)
        .run(args);
  }
}
