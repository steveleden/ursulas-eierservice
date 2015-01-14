package entity;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the kunde database table.
 * 
 */
@Entity
@Table(name="kunde")
@NamedQuery(name="Kunde.findAll", query="SELECT k FROM Kunde k")
public class Kunde implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(unique=true, nullable=false)
	private int id;

	@Column(length=254)
	private String email;

	@Column(nullable=false, length=45)
	private String nachname;

	@Column(nullable=false, length=45)
	private String vorname;

	//bi-directional many-to-one association to Benutzer
	@OneToMany(mappedBy="kunde")
	private List<Benutzer> benutzers;

	//bi-directional many-to-one association to Bestellung
	@OneToMany(mappedBy="kunde")
	private List<Bestellung> bestellungs;

	public Kunde() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNachname() {
		return this.nachname;
	}

	public void setNachname(String nachname) {
		this.nachname = nachname;
	}

	public String getVorname() {
		return this.vorname;
	}

	public void setVorname(String vorname) {
		this.vorname = vorname;
	}

	public List<Benutzer> getBenutzers() {
		return this.benutzers;
	}

	public void setBenutzers(List<Benutzer> benutzers) {
		this.benutzers = benutzers;
	}

	public Benutzer addBenutzer(Benutzer benutzer) {
		getBenutzers().add(benutzer);
		benutzer.setKunde(this);

		return benutzer;
	}

	public Benutzer removeBenutzer(Benutzer benutzer) {
		getBenutzers().remove(benutzer);
		benutzer.setKunde(null);

		return benutzer;
	}

	public List<Bestellung> getBestellungs() {
		return this.bestellungs;
	}

	public void setBestellungs(List<Bestellung> bestellungs) {
		this.bestellungs = bestellungs;
	}

	public Bestellung addBestellung(Bestellung bestellung) {
		getBestellungs().add(bestellung);
		bestellung.setKunde(this);

		return bestellung;
	}

	public Bestellung removeBestellung(Bestellung bestellung) {
		getBestellungs().remove(bestellung);
		bestellung.setKunde(null);

		return bestellung;
	}

}