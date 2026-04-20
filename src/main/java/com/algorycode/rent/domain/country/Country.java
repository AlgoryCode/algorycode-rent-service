package com.algorycode.rent.domain.country;

import com.algorycode.rent.domain.AbstractAuditableLongEntity;
import com.algorycode.rent.domain.location.City;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** Ülke kodu (genelde ISO 3166-1 alpha-2; en fazla 5 harf) ve arayüz için renk (örn. {@code #E30A17}). */
@Getter
@Setter
@Entity
@Table(name = "countries")
public class Country extends AbstractAuditableLongEntity {

  @Column(nullable = false, unique = true, length = 5)
  private String code;

  @Column(nullable = false, length = 128)
  private String name;

  @Column(name = "color_code", nullable = false, length = 16)
  private String colorCode;

  @OneToMany(mappedBy = "country")
  private List<City> cities = new ArrayList<>();
}
