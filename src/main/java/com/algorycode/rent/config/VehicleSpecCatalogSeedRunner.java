package com.algorycode.rent.config;

import com.algorycode.rent.entity.VehicleBodyStyleKind;
import com.algorycode.rent.entity.VehicleFuelKind;
import com.algorycode.rent.entity.VehicleTransmissionKind;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
@RequiredArgsConstructor
public class VehicleSpecCatalogSeedRunner implements ApplicationRunner {

  private final DataSource dataSource;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    try (Connection c = dataSource.getConnection()) {
      c.setAutoCommit(true);
      seedTransmissionTypes(c);
      seedFuelTypes(c);
      seedBodyStyles(c);
      syncSequence(c, "vehicle_transmission_types");
      syncSequence(c, "vehicle_fuel_types");
      syncSequence(c, "vehicle_body_styles");
    }
  }

  private static void seedTransmissionTypes(Connection c) throws SQLException {
    String sql =
        "INSERT INTO vehicle_transmission_types (id, code, label_tr, sort_order) "
            + "SELECT ?, ?, ?, ? WHERE NOT EXISTS (SELECT 1 FROM vehicle_transmission_types WHERE id = ? OR lower(code) = lower(?))";
    try (PreparedStatement ps = c.prepareStatement(sql)) {
      for (VehicleTransmissionKind k : VehicleTransmissionKind.values()) {
        ps.setInt(1, k.getStableId());
        ps.setString(2, k.persistenceCode());
        ps.setString(3, k.getLabelTr());
        ps.setInt(4, k.getSortOrder());
        ps.setInt(5, k.getStableId());
        ps.setString(6, k.persistenceCode());
        ps.executeUpdate();
      }
    }
  }

  private static void seedFuelTypes(Connection c) throws SQLException {
    String sql =
        "INSERT INTO vehicle_fuel_types (id, code, label_tr, sort_order) "
            + "SELECT ?, ?, ?, ? WHERE NOT EXISTS (SELECT 1 FROM vehicle_fuel_types WHERE id = ? OR lower(code) = lower(?))";
    try (PreparedStatement ps = c.prepareStatement(sql)) {
      for (VehicleFuelKind k : VehicleFuelKind.values()) {
        ps.setInt(1, k.getStableId());
        ps.setString(2, k.persistenceCode());
        ps.setString(3, k.getLabelTr());
        ps.setInt(4, k.getSortOrder());
        ps.setInt(5, k.getStableId());
        ps.setString(6, k.persistenceCode());
        ps.executeUpdate();
      }
    }
  }

  private static void seedBodyStyles(Connection c) throws SQLException {
    String sql =
        "INSERT INTO vehicle_body_styles (id, code, label_tr, sort_order) "
            + "SELECT ?, ?, ?, ? WHERE NOT EXISTS (SELECT 1 FROM vehicle_body_styles WHERE id = ? OR lower(code) = lower(?))";
    try (PreparedStatement ps = c.prepareStatement(sql)) {
      for (VehicleBodyStyleKind k : VehicleBodyStyleKind.values()) {
        ps.setInt(1, k.getStableId());
        ps.setString(2, k.persistenceCode());
        ps.setString(3, k.getLabelTr());
        ps.setInt(4, k.getSortOrder());
        ps.setInt(5, k.getStableId());
        ps.setString(6, k.persistenceCode());
        ps.executeUpdate();
      }
    }
  }

  private static void syncSequence(Connection c, String table) throws SQLException {
    String seqSql =
        "SELECT setval(pg_get_serial_sequence('" + table + "', 'id'), "
            + "(SELECT COALESCE(MAX(id), 1) FROM " + table + "))";
    try (Statement st = c.createStatement()) {
      st.execute(seqSql);
    } catch (SQLException ignored) {
    }
  }
}
