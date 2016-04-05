package ejb;

import entity.Benutzer;

import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateful
/* Falls authorisiert: Gibt die Rolle (1 = Benutzer, 2 = Administrator) zurueck. */
/* Falls nicht authorisiert: 0                                                  */
public class AuthorisierungEjb implements AuthorisierungInt{

	@PersistenceContext
	private EntityManager em;
	
	@EJB
	BenutzerVerwaltungInt benutzerVerwaltung;
	
	@Override
	public Integer authorisiert(String benutzerName, String pw)  {
	
		Benutzer benutzer = benutzerVerwaltung.leseBenutzerByName(benutzerName);

		if (benutzer != null) {
			/* Passwort lesen */
		 	String wort = benutzerVerwaltung.lesePwByBenutzerId(benutzer.getId());

			if (pw.trim().matches(wort.trim())) {
				/* Passwort OK */
				return benutzer.getRolle();
			}
			else {
				/* Passwort falsch */
				return 0;
			}
		}		
		return 0;
	}


}
