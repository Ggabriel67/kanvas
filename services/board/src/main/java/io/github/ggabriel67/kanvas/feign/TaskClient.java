package io.github.ggabriel67.kanvas.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "task-service")
public interface TaskClient
{
    @GetMapping("/api/v1/columns/{boardId}")
    List<ColumnDto> getAllBoardColumns(@PathVariable("boardId") Integer boardId);
}
