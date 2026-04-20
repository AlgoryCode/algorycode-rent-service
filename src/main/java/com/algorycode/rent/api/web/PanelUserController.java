package com.algorycode.rent.api.web;

import com.algorycode.rent.api.dto.PanelUserDto;
import com.algorycode.rent.service.PanelUserService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/panel-users")
public class PanelUserController {

  private final PanelUserService panelUserService;

  public PanelUserController(PanelUserService panelUserService) {
    this.panelUserService = panelUserService;
  }

  @GetMapping
  public List<PanelUserDto> list() {
    return panelUserService.listAll();
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    panelUserService.deleteById(id);
  }
}
