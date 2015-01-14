package ejb;

import java.util.List;

import javax.ejb.Local;

import entity.Benutzer;
import entity.Kunde;

@Local
public interface KundenVerwaltungInt {

	Kunde       leseKundeById(Integer kundenId);
	Kunde       leseKundeByBenutzerName(String benutzerName);
	List<Kunde> leseAlleKunden();
	List<Benutzer>	leseBenutzerZuKundeById(Integer kundeId);
	
	Kunde       kreiereKunde(Kunde kunde);
	Integer     mutiereKunde(Kunde kunde);
	Integer     loescheKunde(Kunde kunde);
	Integer     verknuepfeBenutzerMitKunde(String benutzerName, Kunde kunde);
	
}
