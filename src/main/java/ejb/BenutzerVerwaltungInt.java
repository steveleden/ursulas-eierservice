package ejb;

import java.util.List;

import javax.ejb.Local;

import entity.Benutzer;

@Local
public interface BenutzerVerwaltungInt {
	
	public List<Benutzer> 	leseAlleBenutzer ();
	public Benutzer			leseBenutzerByName(String benutzerName);
	public String	 		lesePwByBenutzerId(Integer id);
	public List<Benutzer>	leseBenutzerOhneKunde();

	public Integer 			benutzerAnlegen(Benutzer benutzer);
	public Integer			loescheBenutzer (Benutzer benutzer);
	public Integer			mutiereBenutzer (Benutzer benutzer);
	public Integer			mutierePasswortZuBenutzer(String benutzerName, String pw);

	public void 			updateLoginInfo(String benutzerName);

}
