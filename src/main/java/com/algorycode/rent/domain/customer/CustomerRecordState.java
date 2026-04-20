package com.algorycode.rent.domain.customer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Müşteri kaydı durumu. Birincil anahtar sayısal IDENTITY değil; tablo doğal anahtar {@code record_key}
 * (tc:/ph: önekli dize) ile tanımlanır.
 */
@Getter
@Setter
@Entity
@Table(name = "customer_record_states")
public class CustomerRecordState {

  @Id
  @Column(name = "record_key", nullable = false, length = 255)
  private String recordKey;

  /** {@code false} = pasif; yeni kiralama / talep ve müşteri güncellemeleri engellenir. */
  @Column(nullable = false)
  private boolean active = true;

  /** Yumuşak silinmiş müşteri kayıtları listelerde dönmez. */
  @Column(name = "is_deleted", nullable = false)
  private boolean deleted;
}
