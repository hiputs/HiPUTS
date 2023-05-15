package pl.edu.agh.hiputs;

import java.util.LinkedList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class HiPUTS {

  public static final List<String> globalInitArgs = new LinkedList<>();

  public static void main(String[] args) {
    globalInitArgs.addAll(List.of(args));
    SpringApplication.run(HiPUTS.class, args);
  }
}


