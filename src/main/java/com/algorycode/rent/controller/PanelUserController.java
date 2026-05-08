package com.algorycode.rent.controller;

import com.algorycode.rent.dto.PanelUserDto;
import com.algorycode.rent.service.PanelUserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/panel-users")
@RequiredArgsConstructor
public class PanelUserController {

  private final PanelUserService panelUserService;

  @GetMapping
  public List<PanelUserDto> list() {
    return panelUserService.listAll();
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    panelUserService.deleteById(id);
  }
}
