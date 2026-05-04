package com.algorycode.rent.domain.country;

import com.algorycode.rent.domain.AbstractAuditableLongEntity;
import com.algorycode.rent.domain.location.City;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** Ülke kodu (benzersiz; en fazla 64 karakter) ve arayüz için renk (örn. {@code #E30A17}). */
@Getter
@Setter
@Entity
@Table(name = "countries")
public class Country extends AbstractAuditableLongEntity {

  @Column(nullable = false, unique = true, length = 64)
  private String code;

  @Column(nullable = false, length = 128)
  private String name;

  @Column(name = "color_code", nullable = false, length = 16)
  private String colorCode;

  @OneToMany(mappedBy = "country")
  private List<City> cities = new ArrayList<>();
}
