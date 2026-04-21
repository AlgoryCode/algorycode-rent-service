package com.algorycode.rent.service;

import com.algorycode.rent.api.dto.CreateHandoverLocationRequest;
import com.algorycode.rent.api.dto.HandoverLocationDto;
import com.algorycode.rent.api.dto.UpdateHandoverLocationRequest;
import com.algorycode.rent.api.error.BadRequestException;
import com.algorycode.rent.api.error.ResourceNotFoundException;
import com.algorycode.rent.api.mapper.HandoverLocationMapper;
import com.algorycode.rent.domain.location.City;
import com.algorycode.rent.domain.location.HandoverLocation;
import com.algorycode.rent.domain.location.HandoverLocationKind;
import com.algorycode.rent.repository.CityRepository;
import com.algorycode.rent.repository.HandoverLocationRepository;
import com.algorycode.rent.service.readmodel.FeHandoverSnapshotJson;
import com.algorycode.rent.service.support.Text;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class HandoverLocationService {

  private final HandoverLocationRepository handoverLocationRepository;
  private final CityRepository cityRepository;

  public HandoverLocationService(
      HandoverLocationRepository handoverLocationRepository, CityRepository cityRepository) {
    this.handoverLocationRepository = handoverLocationRepository;
    this.cityRepository = cityRepository;
  }

  @Transactional(readOnly = true)
  public List<HandoverLocationDto> list(HandoverLocationKind kind, boolean includeInactive) {
    return listEntities(kind, includeInactive).stream().map(HandoverLocationMapper::toDto).toList();
  }

  private List<HandoverLocation> listEntities(HandoverLocationKind kind, boolean includeInactive) {
    if (kind != null) {
      return includeInactive
          ? handoverLocationRepository.findByKindOrderByActiveDescLineOrderAscNameAsc(kind)
          : handoverLocationRepository.findByKindAndActiveTrueOrderByLineOrderAscNameAsc(kind);
    }
    return includeInactive
        ? handoverLocationRepository.findAllByOrderByKindAscActiveDescLineOrderAscNameAsc()
        : handoverLocationRepository.findByActiveTrueOrderByKindAscLineOrderAscNameAsc();
  }

  @Transactional(readOnly = true)
  public HandoverLocation requireForAssignment(Long id, HandoverLocationKind expectedKind) {
    return requireLoaded(id, expectedKind);
  }

  /** Tür kontrolü olmadan yalnızca aktif kayıt (araç varsayılanı / kiralama sonrası güncelleme için). */
  @Transactional(readOnly = true)
  public HandoverLocation requireActive(Long id) {
    return requireLoaded(id, null);
  }

  private HandoverLocation requireLoaded(Long id, HandoverLocationKind expectedKind) {
    if (id == null) {
      return null;
    }
    HandoverLocation loc =
        handoverLocationRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Alış/teslim noktası bulunamadı: " + id));
    if (!loc.isActive()) {
      throw new BadRequestException("Seçilen alış/teslim noktası artık kullanılamaz.");
    }
    if (expectedKind != null && loc.getKind() != expectedKind) {
      throw new BadRequestException(
          "Seçilen nokta türü uyuşmuyor (beklenen: " + expectedKind + ", kayıt: " + loc.getKind() + ").");
    }
    return loc;
  }

  @Transactional
  public HandoverLocationDto create(CreateHandoverLocationRequest req) {
    HandoverLocation e = new HandoverLocation();
    e.setKind(req.kind());
    e.setName(req.name().trim());
    e.setDescription(Text.trimOrNull(req.description()));
    e.setAddressLine(Text.trimOrNull(req.addressLine()));
    e.setCity(resolveCity(req.cityId()));
    e.setActive(req.active() == null || Boolean.TRUE.equals(req.active()));
    e.setLineOrder(req.lineOrder());
    e.setSurchargeEur(
        req.surchargeEur() != null
            ? req.surchargeEur().setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO);
    HandoverLocation saved = handoverLocationRepository.save(e);
    saved.setFeHandoverSnapshot(FeHandoverSnapshotJson.forRow(saved));
    return HandoverLocationMapper.toDto(handoverLocationRepository.save(saved));
  }

  @Transactional
  public HandoverLocationDto update(Long id, UpdateHandoverLocationRequest req) {
    HandoverLocation e =
        handoverLocationRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Alış/teslim noktası bulunamadı: " + id));
    if (req.kind() != null) {
      e.setKind(req.kind());
    }
    if (req.name() != null) {
      e.setName(req.name().trim());
    }
    if (req.description() != null) {
      e.setDescription(Text.trimOrNull(req.description()));
    }
    if (req.addressLine() != null) {
      e.setAddressLine(Text.trimOrNull(req.addressLine()));
    }
    if (Boolean.TRUE.equals(req.clearCity())) {
      e.setCity(null);
    } else if (req.cityId() != null) {
      e.setCity(resolveCity(req.cityId()));
    }
    if (req.active() != null) {
      e.setActive(req.active());
    }
    if (req.lineOrder() != null) {
      e.setLineOrder(req.lineOrder());
    }
    if (req.surchargeEur() != null) {
      e.setSurchargeEur(req.surchargeEur().setScale(2, RoundingMode.HALF_UP));
    }
    HandoverLocation saved = handoverLocationRepository.save(e);
    saved.setFeHandoverSnapshot(FeHandoverSnapshotJson.forRow(saved));
    return HandoverLocationMapper.toDto(handoverLocationRepository.save(saved));
  }

  @Transactional
  public void deactivate(Long id) {
    HandoverLocation e =
        handoverLocationRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Alış/teslim noktası bulunamadı: " + id));
    e.setActive(false);
    handoverLocationRepository.save(e);
  }

  private City resolveCity(Long cityId) {
    if (cityId == null) {
      return null;
    }
    return cityRepository
        .findById(cityId)
        .orElseThrow(() -> new ResourceNotFoundException("Şehir bulunamadı: " + cityId));
  }
}
