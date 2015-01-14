package ejb;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import entity.Benutzer;
import entity.Kunde;

@Stateless
public class KundenVerwaltungEjb implements KundenVerwaltungInt {

	@PersistenceContext
	private EntityManager em;
	
	@Override
	public Kunde leseKundeById(Integer kundenId) {
		
		try {
			Kunde kunde = em.find(Kunde.class, kundenId);
			return kunde;
		} catch (Exception e) {
			System.out.println("KundenVerwaltungEjb: Fehler beim Lesen Kunde mit Id = " + kundenId);
		}
		return null;
	}

	@Override
	public Kunde leseKundeByBenutzerName(String benutzerName) {
		
		try {
			Benutzer benutzer = (Benutzer) em.createQuery("SELECT b FROM Benutzer b where name=?1")
									.setParameter(1, benutzerName.trim())
									.getSingleResult();
			Kunde kunde = benutzer.getKunde();
			if (kunde == null) 
				System.out.println("KundenVerwaltungEjb: Benutzer gefunden, Kunde nicht gefunden. Benutzername: " + benutzer.getName());				
			return kunde;
		} catch (Exception e) {
			System.out.println("KundenVerwaltungEjb: Benutzer/Kunde nicht gefunden fuer Benutzername: " + benutzerName);
			return null;
		}

	}
	
	
	@Override
	public List<Benutzer> leseBenutzerZuKundeById(Integer kundeId) {
		
		try {
			@SuppressWarnings("unchecked")
			List<Benutzer> benutzer = em.createQuery("SELECT b from Benutzer b where fk_kundeid=?1").setParameter(1, kundeId).getResultList();
			return benutzer;
		} catch (Exception e) {
			
			System.out.println("KundenVerwaltungEjb, leseBenutzerZuKundeById: Benutzer kann nicht gelesen werden");
			return null;

		}

	}		
	

	@Override
	public List<Kunde> leseAlleKunden() {

		try {
			@SuppressWarnings("unchecked")
			List<Kunde> kundenListe = em.createQuery("SELECT k from Kunde k order by k.nachname").getResultList();
			return kundenListe;
		} catch (Exception e) {
			return null;
		}
	}
	
	@Override
	public Kunde kreiereKunde(Kunde kunde) {

		try {
			em.persist(kunde);
			Integer kid = (Integer) em.createQuery("SELECT max(k.id) from Kunde k").getSingleResult();
			Kunde k = em.find(Kunde.class, kid);
			return k;
		} catch (Exception e) {

			System.out.println("KundenVerwaltungEjb, kreiereKunde: Kunde kann nicht kreiert werden. Vorname/Nachname = " 
							   + kunde.getVorname() + " " + kunde.getNachname());			
			return null;
		}

	}

	@Override
	public Integer mutiereKunde(Kunde kunde) {
		
		try {
			em.merge(kunde);
			return 0;
		} catch (Exception e) {
			
			System.out.println("KundenVerwaltungEjb: Kunde kann nicht gemerged werden. Id = " + kunde.getId());		
			return -1;
		}

	}

	@Override
	public Integer loescheKunde(Kunde kunde) {

		try {
			Kunde zuLoeschen = em.getReference(Kunde.class, kunde.getId());
			em.remove(zuLoeschen);
			return 0;
		} catch (Exception e) {
			
			System.out.println("KundenVerwaltungEjb: Kunde kann nicht geloescht werden. Id = " + kunde.getId());		
			return -1;
		}
	}

	@Override
	public Integer verknuepfeBenutzerMitKunde(String benutzerName, Kunde kunde) {
		
		try {
			Benutzer benutzer = (Benutzer) em.createQuery("SELECT b FROM Benutzer b where name=?1")
									.setParameter(1, benutzerName.trim())
									.getSingleResult();
			benutzer.setKunde(kunde);
			em.merge(benutzer);
			return 0;
			
		} catch (Exception e) {
			
			System.out.println("KundenVerwaltungEjb: Benutzer kann nicht mit Kunde verknuepft werden. Kunde.Id = " + kunde.getId());		
			e.printStackTrace();
			return -1;

		}
		
	}


}
