package model; // Package declaration

import java.sql.Date;

public class Doctor {
    private int id;
    private String nume;
    private String prenume;
    private Date dataNastere;
    private String cnp;
    private String adresaDomiciliu;
    private String numarTelefon;
    private String adresaEmail;
    private String specializarea;
    private int clinicId;

    public Doctor() {
    }

    public Doctor(int id, String nume, String prenume, Date dataNastere, String cnp, String adresaDomiciliu, String numarTelefon, String adresaEmail, String specializarea, int clinicId) {
        this.id = id;
        this.nume = nume;
        this.prenume = prenume;
        this.dataNastere = dataNastere;
        this.cnp = cnp;
        this.adresaDomiciliu = adresaDomiciliu;
        this.numarTelefon = numarTelefon;
        this.adresaEmail = adresaEmail;
        this.specializarea = specializarea;
        this.clinicId = clinicId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNume() { return nume; }
    public void setNume(String nume) { this.nume = nume; }
    public String getPrenume() { return prenume; }
    public void setPrenume(String prenume) { this.prenume = prenume; }
    public Date getDataNastere() { return dataNastere; }
    public void setDataNastere(Date dataNastere) { this.dataNastere = dataNastere; }
    public String getCnp() { return cnp; }
    public void setCnp(String cnp) { this.cnp = cnp; }
    public String getAdresaDomiciliu() { return adresaDomiciliu; }
    public void setAdresaDomiciliu(String adresaDomiciliu) { this.adresaDomiciliu = adresaDomiciliu; }
    public String getNumarTelefon() { return numarTelefon; }
    public void setNumarTelefon(String numarTelefon) { this.numarTelefon = numarTelefon; }
    public String getAdresaEmail() { return adresaEmail; }
    public void setAdresaEmail(String adresaEmail) { this.adresaEmail = adresaEmail; }
    public String getSpecializarea() { return specializarea; }
    public void setSpecializarea(String specializarea) { this.specializarea = specializarea; }
    public int getClinicId() { return clinicId; }
    public void setClinicId(int clinicId) { this.clinicId = clinicId; }

    @Override
    public String toString() {
        return "Doctor{" + "id=" + id + ", nume='" + nume + '\'' + ", prenume='" + prenume + '\'' + ", cnp='" + cnp + '\'' + ", specializarea='" + specializarea + '\'' + '}';
    }
}