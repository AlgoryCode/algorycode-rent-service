-- Araç listesi / vitrin için önbelleklenmiş JSON (FeFleetSnapshotBuilder çıktısı).
ALTER TABLE vehicles ADD COLUMN fe_fleet_snapshot JSON NULL;
