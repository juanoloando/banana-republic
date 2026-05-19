package model.building;

// contract untuk bangunan yang ditempatkan di intersection
public interface Building {

    String getOwnerName();
    int getVictoryPoints();

    // jlh kartu sumber data yg diterima per produksi, settlement = 1, lab = 2
    int getResourceYield();

    String getBuildingTypeName();
}
