package ejb;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import entity.Benutzer;
import entity.Passwort;


@Stateful
public class BenutzerVerwaltungEjb implements BenutzerVerwaltungInt{

	@PersistenceContext
	private EntityManager em;
	
	@Override
	public List<Benutzer> leseAlleBenutzer() {
		try {
			@SuppressWarnings("unchecked")
			List<Benutzer> list = (List<Benutzer>) em.createQuery("SELECT b from Benutzer b order by b.name").getResultList();
			return list;
		} catch (Exception e) {
			System.out.println("BenutzerVerwaltungEjb, leseAlleBenutzer: Fehler beim Lesen der Benutzer");
			return null;
		}
	}
	
	@Override
	public Benutzer leseBenutzerByName(String benutzerName) {
		
		try {
			Benutzer benutzer = (Benutzer) em.createQuery("SELECT b from Benutzer b where name =?1")
			        						 .setParameter(1, benutzerName.trim())
			        						 .getSingleResult();
			return benutzer;
		} catch (Exception e) {			
			/* Falls Query kein Resultat findet ... */
			return null;
		}
	}

	@Override
	public List<Benutzer> leseBenutzerOhneKunde() {
		
		try {
			@SuppressWarnings("unchecked")
			List<Benutzer> benutzer = em.createQuery("SELECT b from Benutzer b where fk_kundeid is null and rolle=1").getResultList();			
			return benutzer;
		} catch (Exception e) {
			System.out.println("BenutzerVerwaltungEjb.leseBenutzerOhneKunde(): Benutzer konnten nicht gelesen werden.");
			return null;
		}
		

	}	
	
	

	@Override
	public Integer benutzerAnlegen(Benutzer benutzer) {
		
		try {
			em.persist(benutzer);
			return 0;
		} catch (Exception e) {
			System.out.println("Fehler beim Speichern Enitaet Benutzer: " + benutzer.getName());
			return -1;
		}
		
	}

	@Override
	public Integer loescheBenutzer(Benutzer benutzer) {
		try {
			Benutzer zuLoeschen = em.getReference(Benutzer.class, benutzer.getId());
			em.remove(zuLoeschen);
			return 0;
		} catch (Exception e) {
			System.out.println("Fehler beim Loeschen Enitaet Benutzer:" + benutzer.getName());
			return -1;
		}
	}

	@Override
	public Integer mutiereBenutzer(Benutzer benutzer) {
		try {
			if (benutzer != null) {
				em.merge(benutzer);
				return 0;
			} else {
				return -1;
			}
		} catch (Exception e) {
			System.out.println("Fehler beim Update Enitaet Benutzer:" + benutzer.getName());
			return -1;
		}
	}

	
	@Override
	public String lesePwByBenutzerId(Integer id) {
		try {
			Passwort pw = em.find(Passwort.class, id);
			if (pw != null)
				return pw.getWort();
			else 
				return " ";
		} catch (Exception e) {
			e.printStackTrace();
			return " ";
		}
	}
	
	@Override
	public Integer mutierePasswortZuBenutzer(String benutzerName, String pw) {

		try {
			if (pw == null || pw.isEmpty())
				return -1;
			
			Benutzer benutzer = this.leseBenutzerByName(benutzerName);
			
			if (benutzer == null) {
				return -1;
			}
						
			Passwort passWort = (Passwort) em.find(Passwort.class, benutzer.getId());
			
			if (passWort != null) {
				/* update passwort */
				passWort.setWort(pw);
				em.merge(passWort);
				return 0;
			} else {
								
				/* Passwort an Benutzer haengen */
				passWort = new Passwort();
				passWort.setFkBenutzerid(benutzer.getId());
				passWort.setWort(pw);
				em.persist(passWort);
				return 0;
			}
					
		}
		
		catch (Exception e) {
		System.out.println("BenutzerVerwaltungEJB: Passwort mutieren fehlerhaft.");
		e.printStackTrace();
		return -1;
		}		
	}

	
	@Override
	public void updateLoginInfo(String benutzerName) {
		
		Benutzer benutzer = this.leseBenutzerByName(benutzerName);
		
		if (benutzer != null) {
			
			try {
				benutzer.setLoginCounter(benutzer.getLoginCounter() + 1);				
				// Login-Zeit speichern 				
				benutzer.setLoginZuletzt(getLokaleZeit());
				em.merge(benutzer);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	/* Helper-Methoden */
	
	// aktuelle lokale Zeit in UTC liefern
	private Date getLokaleZeit() {		
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		sdf.setTimeZone(TimeZone.getDefault());
		Date localDate = new Date();				
		sdf.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));	
		String dateUTCStr = sdf.format(localDate);
		sdf.setTimeZone(TimeZone.getDefault());				
		Date dateUTC;
		try {
			dateUTC = sdf.parse(dateUTCStr);
			return dateUTC;
		} catch (ParseException e) {
			System.out.println("BenutzerVerwaltungEjb.datumZeitUTC(): Datum kann nicht konvertiert werden.");
			return null;
		}				
		
	}	


}
