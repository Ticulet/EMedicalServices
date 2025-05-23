package model;

public class Clinic {
    private int id;
    private String numeClinica;
    private String detalii;

    public Clinic() {
    }

    // Constructor used when creating a new clinic before saving to DB (ID is unknown)
    public Clinic(String numeClinica, String detalii) {
        this.numeClinica = numeClinica;
        this.detalii = detalii;
    }

    // Constructor used when fetching from DB (ID is known)
    public Clinic(int id, String numeClinica, String detalii) {
        this.id = id;
        this.numeClinica = numeClinica;
        this.detalii = detalii;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNumeClinica() { return numeClinica; }
    public void setNumeClinica(String numeClinica) { this.numeClinica = numeClinica; }
    public String getDetalii() { return detalii; }
    public void setDetalii(String detalii) { this.detalii = detalii; }

    @Override
    public String toString() {
        return numeClinica;
    }
}