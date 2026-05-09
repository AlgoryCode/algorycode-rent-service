package com.algorycode.rent.entity;

import com.algorycode.rent.entity.AbstractAuditableLongEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "accident_photos")
public class AccidentPhoto extends AbstractAuditableLongEntity {

  @Column(name = "accident_report_id", nullable = false)
  private Long accidentReportId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "accident_report_id", nullable = false, insertable = false, updatable = false)
  private AccidentReport accidentReport;

  @Column(nullable = false, length = 4096)
  private String url;

  @Column(length = 512)
  private String caption;

  @PrePersist
  @PreUpdate
  void syncAccidentPhotoFk() {
    if (accidentReport != null) {
      accidentReportId = accidentReport.getId();
    }
  }
}
